package ennuo.craftworld.types;

import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.utilities.Images;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.enums.Crater;
import ennuo.craftworld.resources.enums.ItemType;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.ProfileItem;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.resources.structs.StringEntry;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.swing.Nodes;
import ennuo.craftworld.resources.Plan;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.serializer.Serializer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

// I really want to burn this class.
// It's so messy! Why is it reimplementing a FAR4/5!
public class BigProfile extends FileData {
  public boolean isParsed = false;
  public FileEntry profile;
  public ArrayList<FileEntry> entries;
  private byte[] integrity;
  public int revision = 1;

  public ArrayList<ProfileItem> inventoryCollection = new ArrayList<ProfileItem>();
  public ArrayList<StringEntry> stringCollection = new ArrayList<StringEntry>();
  public ArrayList<Slot> slots = new ArrayList<Slot>();
  
  public SlotID[] downloadedSlots;
  public ResourceDescriptor planets;

  private int nextIndex = 0;
  private int lastOffset = 0;
  
  private boolean[] usedSlots = new boolean[82];
  

  public boolean fromProductionBuild = true;
  private boolean isFAR5 = false;

  public BigProfile(File file) {
    this.path = file.getAbsolutePath();
    this.name = file.getName();
    type = "Big Profile";
    byte[] data = FileIO.read(path);
    if (data != null) process(new Data(data), false);
  }
  
  public BigProfile(File file, boolean isStreamingChunk) {
    this.path = file.getAbsolutePath();
    this.name = file.getName();
    if (isStreamingChunk)
        type = "Streaming Chunk";
    else type = "Big Profile";
    byte[] data = FileIO.read(path);
    if (data != null) process(new Data(data), isStreamingChunk);
  }

  public BigProfile(Data data) {
    process(data, false);
  }
  
  public BigProfile(Data data, boolean isStreamingChunk) {
      process(data, isStreamingChunk);
  }
  
  private int getNextSlot() {
      for (int i = 0; i < usedSlots.length; ++i) {
          if (usedSlots[i]) continue;
          return i;
      }
      return -1;
  }
  
  private Crater getCrater(int crater) {
      usedSlots[crater] = true;
      return Crater.valueOf("SLOT_" + crater + "_LBP" + revision);
  }

  private void process(Data data, boolean isStreamingChunk) {
    data.seek(data.length - 4);

    String magic  = data.str(4);
    
    isFAR5 = magic.equals("FAR5");
    
    if (!magic.equals("FAR4") && !isFAR5) return;

    data.seek(data.length - 8);
    int count = data.i32f();

    if (isFAR5)
         data.seek(data.length - (0x20 + (0x1C * count)));   
    else data.seek(data.length - (0x1C + (0x1C * count)));

    int tableOffset = data.offset;
    if (!isStreamingChunk) {
        model = new FileModel(new FileNode("BIGPROFILE", null, null));
        root = (FileNode) model.getRoot();   
    }
    entries = new ArrayList < FileEntry > (count - 1);
    for (int i = 0; i < count; ++i) {
      data.seek(tableOffset + (0x1C * i));
      byte[] sha1 = data.bytes(0x14);
      int offset = data.i32f();
      int size = data.i32f();
      lastOffset += size;
      data.seek(offset);
      byte[] dataBuffer  = data.bytes(size);
      int resMagic = (dataBuffer[0] & 0xFF) << 24 | 
            (dataBuffer[1] & 0xFF) << 16 | 
            (dataBuffer[2] & 0xFF) << 8 | 
            (dataBuffer[3] & 0xFF) << 0;
      FileEntry entry = new FileEntry(dataBuffer, sha1); entry.timestamp = 0;
      entry.offset = offset;
      entry.size = size;
      if (resMagic == 0x42505262) { profile = entry; } 
      else { entries.add(entry); if (!isStreamingChunk) addNode(entry); }
    }
    if (!isStreamingChunk) {
        parseProfile();
        if (data.offset % 4 != 0) data.forward(4 - (data.offset % 4));
        integrity = data.bytes(tableOffset - data.offset);
    }
    isParsed = true;
  }

  public FileNode addNode(FileEntry entry) {
    String extension = new String(new byte[] { entry.data[0], entry.data[1], entry.data[2] });
    switch (extension) {
    case "ÿøÿ":
    case "jfi":
      extension = "jpg";
      break;
    case "vop":
      extension = "raw";
      break;
    case "msh":
      extension = "mol";
      break;
    case "gtf":
      extension = "tex";
      break;
    case "pln":
      extension = "plan";
      break;
    case "lvl":
      extension = "bin";
      break;
    case "‰pn":
      extension = "png";
      break;
    }

    if (extension.equals("plan") || extension.equals("bin")) return null;
    entry.path = "resources/";
    if (extension.equals("mol")) entry.path += "meshes/";
    else if (extension.equals("tex") || extension.equals("jpg") || extension.equals("png")) entry.path += "textures/";
    else if (extension.equals("raw")) entry.path += "audio/";
    else if (extension.equals("gmt")) entry.path += "gfx_materials/";
    else if (extension.equals("anm")) entry.path += "animations/";
    else if (extension.equals("bev")) entry.path += "bevels/";
    else if (extension.equals("mat")) entry.path += "materials/";
    else if (extension.equals("ssp")) entry.path += "skeletons/";
    else entry.path += "unknown/";

    entry.path += entry.offset + "." + extension;

    return Nodes.addNode(root, entry);

  }

  private void serializeProfile() {
    int itemCount = inventoryCollection.size();
    int stringCount = stringCollection.size();
    int slotCount = slots.size();
    Resource biggestProfile = new Resource(profile.data);
    
    
    Output output = new Output((InventoryDetails.MAX_SIZE * itemCount) + (itemCount * 0x12) + (Slot.MAX_SIZE * slotCount + 1) + (stringCount * StringEntry.MAX_SIZE + (StringEntry.MAX_SIZE * itemCount)) + 0xFFFF, biggestProfile.revision, biggestProfile.branchDescription);

    output.i32(itemCount);
    Serializer serializer = new Serializer(output);
    for (int i = 0; i < itemCount; ++i) {
      ProfileItem item = inventoryCollection.get(i);

      StringEntry location = findString(item.metadata.translatedLocation);
      StringEntry category = findString(item.metadata.translatedCategory);

      if (location == null) {
        item.metadata.locationIndex = (short) nextIndex;

        StringEntry entry = new StringEntry();
        if (item.metadata.translatedLocation == null || item.metadata.translatedLocation.equals("")) entry.key = 0;
        entry.index = nextIndex;
        nextIndex++;
        stringCount++;
        entry.string = item.metadata.translatedLocation;
        stringCollection.add(entry);
      } else item.metadata.locationIndex = (short) location.index;

      if (category == null) {
        item.metadata.categoryIndex = (short) nextIndex;

        StringEntry entry = new StringEntry();
        if (item.metadata.translatedCategory == null || item.metadata.translatedCategory.equals("")) entry.key = 0;
        entry.index = nextIndex;
        nextIndex++;
        stringCount++;
        entry.string = item.metadata.translatedCategory;
        stringCollection.add(entry);
      } else item.metadata.categoryIndex = (short) category.index;

      output.resource(item.resource, true);
      
      if (output.revision > 0x010503EF) output.i32(0);

      item.metadata = serializer.struct(item.metadata, InventoryDetails.class);

      if (output.revision == 0x3e2) output.u8(1);
      output.u8(0x80);
      output.i32(0);
      output.i16((short)(i + 1));
      output.pad(0x3);
      if (output.revision > 0x33a) {
        output.u8(item.flags);
        output.pad(0x4);
      } else {
        output.pad(0x8);
        output.u8(item.flags);
      }
    }

    if (profile.revision >= 0x3e6) output.u8(0); // vita cross dependency hashes
    if (profile.revision >= 0x3f6) output.u8(0); // data labels

    stringCount = stringCollection.size();

    output.bool(false); // unsorted = false
    output.bool(true); // sortEnabled = true

    Collections.sort(stringCollection, new Comparator < StringEntry > () {@Override
      public int compare(StringEntry e1, StringEntry e2) {
        return e1.string.compareTo(e2.string);
      }
    });

    output.i32(stringCount);
    if (stringCount > 0x100) output.u8(2);
    else if (stringCount > 0) output.u8(1);

    byte[] sorted = new byte[stringCount];
    byte[] overflow = new byte[stringCount];
    for (StringEntry entry: stringCollection) {
        byte o = (byte) 0;
        int index = getRawIndex(entry.index);
        while (index > 0xFF) {
            o++;
            index -= 0x101;
        }
        overflow[entry.index] = o;
        sorted[entry.index] = (byte) index;
    }
    output.bytes(sorted);
    if (stringCount > 0x100)
        output.bytes(overflow);

    output.i32(stringCount);
    for (StringEntry entry: stringCollection)
    entry.serialize(output);

    if (profile.revision > 0x33a) output.bool(fromProductionBuild);

    output.i32(slotCount);
    for (Slot slot: slots) {
        serializer.struct(slot.id, SlotID.class);
        serializer.struct(slot, Slot.class);
    }
    
    if (output.revision == 0x3e2) {
        output.i32(0); // labels
        output.i32(0); output.i32(0); // challenges
        output.i32(0); // treasures
        
        output.i32(downloadedSlots.length);
        for (int i = 0; i < downloadedSlots.length; ++i)
            downloadedSlots[i].serialize(output);
        
        output.resource(planets, true);
    }

    output.shrink();

    ResourceDescriptor[] dependencies = new ResourceDescriptor[output.dependencies.size()];
    dependencies = output.dependencies.toArray(dependencies);

    profile.data = Resource.compressToResource(output, ResourceType.BIG_PROFILE);
    profile.size = profile.data.length;
    profile.SHA1 = Bytes.SHA1(profile.data);

    setIntegrityHash(profile.SHA1);
  }

  private StringEntry findString(String string) {
    for (StringEntry entry: stringCollection)
    if (entry.string.equals(string)) return entry;
    return null;
  }

  private StringEntry findString(int index) {
    for (StringEntry entry: stringCollection)
    if (entry.index == index) return entry;
    return null;
  }

  private int getRawIndex(int index) {
    for (int i = 0; i < stringCollection.size(); ++i)
    if (stringCollection.get(i).index == index) return i;
    return - 1;
  }

  private void parseProfile() {
    Data profile = new Resource(this.profile.data).handle;
    this.profile.revision = profile.revision;
    
    if (profile.revision > 0x010503EF) revision = 3;
    else revision = 1;
    
    int itemCount = profile.i32();
    inventoryCollection = new ArrayList <ProfileItem>(itemCount);
    Serializer serializer = new Serializer(profile);
    for (int i = 0; i < itemCount; ++i) {
      ProfileItem item = new ProfileItem();
      item.resource = profile.resource(ResourceType.PLAN, true);
      if (profile.revision > 0x010503EF) item.GUID = profile.i32();
      item.metadata = serializer.struct(item.metadata, InventoryDetails.class);
      if (profile.revision == 0x3e2) profile.forward(0x1);
      profile.forward(0x7);
      item.flags = profile.i8();
      if (profile.revision > 0x33a) profile.forward(0x4);
      else {
        profile.forward(0x7);
        item.flags = profile.i8();
      }
      inventoryCollection.add(item);
      addItemNode(item);
    }
    
    System.out.println("vita hashes offset = 0x" + Bytes.toHex(profile.offset));

    if (profile.revision >= 0x3e6) {
        int hashCount = profile.i32();
        for (int i = 0; i < hashCount; ++i)
            profile.bytes(0x14);
    }
    
    System.out.println("data labels offset = 0x" + Bytes.toHex(profile.offset));
    
    if (profile.revision >= 0x3f6 && profile.revision != 0x3e2) {
        int labelCount = profile.i32();
        for (int i = 0; i < labelCount; ++i) {
            profile.i32();
            profile.str16();
        }
    }

    System.out.println("string table offset = 0x" + Bytes.toHex(profile.offset));
    
    boolean unsorted = profile.bool();
    boolean sortEnabled = profile.bool();
    
    System.out.println("string table indices offset = 0x" + Bytes.toHex(profile.offset));

    /* RawIndexToSortedIndex, skipping */
    int stringCount = profile.i32();
    if (stringCount != 0) {
       int tableCount = profile.i8();
       for (int i = 0; i < tableCount; ++i)
           for (int j = 0; j < stringCount; ++j)
               profile.i8();
    }
    
    System.out.println("strings offset = 0x" + Bytes.toHex(profile.offset));

    stringCount = profile.i32();
    System.out.println("strings count = 0x" + Bytes.toHex(stringCount));
    stringCollection = new ArrayList<StringEntry> (stringCount);
    for (int i = 0; i < stringCount; ++i) {
      stringCollection.add(new StringEntry(profile));
      nextIndex++;
    }

    if (profile.revision > 0x33a) fromProductionBuild = profile.bool();

    System.out.println("slots offset = 0x" + Bytes.toHex(profile.offset));
    
    int slotCount = profile.i32();
    slots = new ArrayList<Slot>(slotCount);
    for (int i = 0; i < slotCount; ++i) {
        serializer.struct(null, SlotID.class);
        addSlotNode(serializer.struct(null, Slot.class));
    }
    checkForSlotChanges();

    for (ProfileItem item : inventoryCollection) {
      StringEntry location = findString(item.metadata.locationIndex);
      StringEntry category = findString(item.metadata.categoryIndex);
      if (location != null) item.metadata.translatedLocation = location.string;
      if (category != null) item.metadata.translatedCategory = category.string;
    }
    
    if (profile.revision == 0x3e2) {
        
        // labels
        int labelCount = profile.i32();
        for (int i = 0; i < labelCount; ++i) {
            profile.i32();
            profile.str16();
        }
        
        profile.i32(); profile.i32();
        profile.i32();
        
        downloadedSlots = new SlotID[profile.i32()];
        for (int i = 0; i < downloadedSlots.length; ++i)
            downloadedSlots[i] = new SlotID(profile);
        
        planets = profile.resource(ResourceType.LEVEL, true);
    }
    
    this.profile.setResource("slots", slots);
    this.profile.setResource("items", inventoryCollection);
  }
  
  public void checkForSlotChanges() {
      usedSlots = new boolean[82];
      for (Slot slot : slots) {
        if (slot.id.type == SlotType.USER_CREATED_STORED_LOCAL) {
            if (!(slot.id.ID > 81) && !(slot.id.ID < 0))
              usedSlots[(int) slot.id.ID] = true;
        }
      }
  }

  public FileEntry find(byte[] hash) {
    if (hash == null) return null;
    for (int i = 0; i < entries.size(); i++)
    if (Arrays.equals(hash, entries.get(i).SHA1)) return entries.get(i);
    return null;
  }

  public byte[] extract(byte[] sha1) {
    FileEntry entry = find(sha1);
    if (entry == null) return null;
    return entry.data;
  }

  public void add(byte[] data) { add(data, true); }
  public void add(byte[] data, boolean parse) {
    Resource resource = new Resource(data);
    byte[] SHA1 = Bytes.SHA1(data);
    
    if (find(SHA1) != null) return;
    
    FileEntry entry = new FileEntry(data, SHA1);
    
    entry.offset = lastOffset;
    lastOffset += data.length;
    
    this.entries.add(entry);
    
    shouldSave = true;
    
    if (resource.type == ResourceType.PLAN) {
        if (parse) {
        Serializer serializer = new Serializer(resource.handle);
        Plan item = serializer.struct(null, Plan.class);
        InventoryDetails metadata = null;
        if (item != null) metadata = item.details;
        if (metadata == null) { metadata = new InventoryDetails(); System.out.println("Metadata is null, using default values..."); }
        addItem(new ResourceDescriptor(SHA1, ResourceType.PLAN), metadata);
        }
        return;
    }
    
    if (resource.type == ResourceType.LEVEL) {
        if (parse) {
        checkForSlotChanges();
        int index = getNextSlot(); 
        if (index == -1) {
            System.err.println("There are no more available moon slots!");
            return;
        }
        
        Crater crater = getCrater(index);
        
        Slot slot = new Slot();
        slot.id.ID = index;
        slot.id.type = SlotType.USER_CREATED_STORED_LOCAL;
        
        slot.location = crater.value;
        
        slot.root = new ResourceDescriptor(SHA1, ResourceType.LEVEL);
        
        addSlotNode(slot);
        }
        
        return;
    }
    
    this.addNode(entry);
  }

  public boolean edit(FileEntry entry, byte[] data) {
    shouldSave = true;
    byte[] hash = Bytes.SHA1(data);
    
    Slot slot = entry.getResource("slot");
    ProfileItem item = entry.getResource("profileItem");
    
    if (item != null) {
        ResourceDescriptor newRes = new ResourceDescriptor(hash, ResourceType.PLAN);
        item.resource = newRes;
        item.metadata.resource = newRes;
    }
    
    
    if (slot != null)
        slot.root = new ResourceDescriptor(hash, ResourceType.LEVEL);
    
    entry.SHA1 = hash;
    entry.data = data;
    entry.size = data.length;
    
    entry.resetResources(false);
    
    return true;
  }

  public void addString(String string, long hash) {
    StringEntry entry = new StringEntry();
    entry.key = hash;
    entry.string = string;
    entry.index = nextIndex;
    nextIndex++;
    stringCollection.add(entry);
    shouldSave = true;
  }

  public void addItem(ResourceDescriptor resource, InventoryDetails metadata) {
    ProfileItem item = new ProfileItem();
    item.resource = resource;
    item.metadata = metadata;
    if (metadata.dateAdded == 0)
        metadata.dateAdded = new Date().getTime() * 2 / 1000;
    item.flags = 0;
    inventoryCollection.add(item);
    addItemNode(item);
    shouldSave = true;
  }
  
  public void addSlot(Slot slot) {
      int index = getNextSlot();
      if (index == -1) {
          System.out.println("No more slots available on your moon!");
          return;
      }
      
      Crater crater = this.getCrater(index);
      
      SlotID id = new SlotID(SlotType.USER_CREATED_STORED_LOCAL, index);
      
      slot.id = id;
      
      slot.location = crater.value;
      
      addSlotNode(slot);
      shouldSave = true;
  }
  
  private void addItemNode(ProfileItem item) {
    if (item.resource == null) return;
      
    FileEntry entry = find(item.resource.hash);
    if (entry == null) return;
    
    entry.setResource("profileItem", item);
    entry.path = "items/" + item.metadata.type.name().toLowerCase() + "/";
    if (item.metadata.type.equals(ItemType.USER_COSTUMES) || item.metadata.type.equals(ItemType.COSTUMES)) {
      if (item.metadata.subType.equals(ItemType.ALL)) entry.path += "outfits/";
      else entry.path += item.metadata.subType.name().toLowerCase() + "/";
    }
    
    String title;
    if (item.metadata.userCreatedDetails != null && item.metadata.userCreatedDetails.title != null)
        title = item.metadata.userCreatedDetails.title;
    else {
        if (item.metadata.type == ItemType.USER_PHOTOS) title = "A Photo";
        else if (item.metadata.type == ItemType.POD) title = "A Pod";
        else if (item.metadata.type == ItemType.COSTUMES || item.metadata.type == ItemType.USER_COSTUMES) title = "A Costume";
        else title = "Some kind of object";
    }
    
    entry.path += title + ".plan";
    
    StringEntry location = findString(item.metadata.locationIndex);
    StringEntry category = findString(item.metadata.categoryIndex);
    if (location != null) item.metadata.translatedLocation = location.string;
    if (category != null) item.metadata.translatedCategory = category.string;

    Nodes.addNode(root, entry);
  }
  
  private void addSlotNode(Slot slot) {
      if (slot != null) {
        slots.add(slot); slot.revision = revision;
        if (slot.root == null) return;
        FileEntry entry = find(slot.root.hash);
        if (entry != null) {
          entry.setResource("slot", slot);
          int revision = new Resource(extract(entry.SHA1)).revision;
          if (slot.icon != null && slot.icon.hash != null) {
            FileEntry iconEntry = find(slot.icon.hash);
            if (iconEntry != null) {
                Texture texture = iconEntry.getResource("texture");
              if (texture != null) slot.renderedIcon = Images.getSlotIcon(texture.getImage(), new Resource(extract(entry.SHA1)).revision);
            }
          }

          if (slot.renderedIcon == null) slot.renderedIcon = Images.getSlotIcon(null, revision);

          entry.path = "slots/" + slot.title + ".bin";
          Nodes.addNode(root, entry);
        }
      }
  }

  public void setIntegrityHash(byte[] hash) {
    int start = integrity.length - 0x3C;

    integrity[(start - 0x14)] = 0;
    integrity[(start - 0x14) + 1] = 0;
    integrity[(start - 0x14) + 2] = 0;
    integrity[(start - 0x14) + 3] = 0;

    integrity[4] = 0;
    integrity[5] = 0;
    integrity[6] = 0;
    integrity[7] = 0;

    for (int i = start; i < start + 0x14; ++i)
    integrity[i] = hash[i - start];
  }

  @Override
  public boolean save(String path) {
    serializeProfile();

    FileEntry[] entries = new FileEntry[this.entries.size() + 1];
    for (int i = 0; i < this.entries.size(); ++i)
    entries[i] = this.entries.get(i);
    entries[entries.length - 1] = profile;

    Arrays.sort(entries, new Comparator < FileEntry > () {@Override
      public int compare(FileEntry e1, FileEntry e2) {
        return Bytes.toHex(e1.SHA1).compareTo(Bytes.toHex(e2.SHA1));
      }
    });

    int size = integrity.length + 0x34 + profile.data.length + 0xFFFF;
    for (FileEntry entry: entries)
    size += (0x1C) + entry.size;

    Output output = new Output(size);

    for (FileEntry entry: entries)
    output.bytes(entry.data);

    if (output.offset % 4 != 0) output.pad(4 - (output.offset % 4));
    output.bytes(integrity);

    int offset = 0;
    for (FileEntry entry: entries) {
      output.bytes(entry.SHA1);
      output.i32(offset);
      output.i32(entry.size);
      offset += entry.size;
    }

    if (isFAR5)
        output.pad(0x4);
    output.pad(0x14);

    output.i32(entries.length);
    if (isFAR5)
        output.str("FAR5");
    else 
        output.str("FAR4");

    output.shrink();

    FileIO.write(output.buffer, path);

    shouldSave = false;

    return true;
  }
}
