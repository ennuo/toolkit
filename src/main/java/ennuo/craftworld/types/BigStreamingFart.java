package ennuo.craftworld.types;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.utilities.Images;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.enums.Crater;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.InventoryItem;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.resources.structs.SortString;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.swing.Nodes;
import ennuo.craftworld.resources.Plan;
import ennuo.craftworld.resources.enums.InventoryObjectSubType;
import ennuo.craftworld.resources.enums.InventoryObjectType;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.savedata.BigProfile;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

// I really want to burn this class.
// It's so messy! Why is it reimplementing a FAR4/5!
public class BigStreamingFart extends FileData {
    public boolean isParsed = false;

    /**
     * File entry of the root resource.
     */
    public FileEntry rootProfileEntry;

    /**
     * Parsed RBigProfile if it's the root type.
     */
    public BigProfile bigProfile;

    public ArrayList<FileEntry> entries;

    /**
     * Tracks the "root" resource of this archive,
     * as well as its type.
     */
    private byte[] saveKey;
    
    /**
     * Used for determing which moon model positions to use
     * LBP1/LBP3
     */
    public int revision = 1;

    /**
     * Which user created slots have been used so far
     */
    private boolean[] usedSlots = new boolean[82];
    
    /**
     * Whether or not the archive is version 5,
     * not actually fully supported as of right now.
     */
    private boolean isFAR5 = false;

    public BigStreamingFart(File file) {
        this.path = file.getAbsolutePath();
        this.name = file.getName();
        type = "Big Profile";
        byte[] data = FileIO.read(path);
        if (data != null)
            process(new Data(data), false);
    }

    public BigStreamingFart(File file, boolean isStreamingChunk) {
        this.path = file.getAbsolutePath();
        this.name = file.getName();
        if (isStreamingChunk)
            type = "Streaming Chunk";
        else
            type = "Big Profile";
        byte[] data = FileIO.read(path);
        if (data != null)
            this.process(new Data(data), isStreamingChunk);
    }

    public BigStreamingFart(Data data) {
        this.process(data, false);
    }

    public BigStreamingFart(Data data, boolean isStreamingChunk) {
        this.process(data, isStreamingChunk);
    }

    private int getNextSlot() {
        for (int i = 0; i < this.usedSlots.length; ++i) {
            if (this.usedSlots[i])
                continue;
            return i;
        }
        return -1;
    }

    private Crater getCrater(int crater) {
        this.usedSlots[crater] = true;
        return Crater.valueOf("SLOT_" + crater + "_LBP" + revision);
    }

    private void process(Data data, boolean isStreamingChunk) {
        data.seek(data.length - 4);

        String magic = data.str(4);

        this.isFAR5 = magic.equals("FAR5");

        if (!magic.equals("FAR4") && !isFAR5)
            return;

        data.seek(data.length - 8);
        int count = data.i32f();

        if (isFAR5)
            data.seek(data.length - (0x20 + (0x1C * count)));
        else
            data.seek(data.length - (0x1C + (0x1C * count)));

        int tableOffset = data.offset;
        if (!isStreamingChunk) {
            this.model = new FileModel(new FileNode("BIGPROFILE", null, null));
            this.root = (FileNode) model.getRoot();
        }
        
        this.entries = new ArrayList<FileEntry>(count - 1);
        for (int i = 0; i < count; ++i) {
            data.seek(tableOffset + (0x1C * i));
            SHA1 hash = data.sha1();
            int offset = data.i32f();
            int size = data.i32f();
            data.seek(offset);
            byte[] dataBuffer = data.bytes(size);
            int resMagic = (dataBuffer[0] & 0xFF) << 24 |
                    (dataBuffer[1] & 0xFF) << 16 |
                    (dataBuffer[2] & 0xFF) << 8 |
                    (dataBuffer[3] & 0xFF) << 0;
            FileEntry entry = new FileEntry(dataBuffer, hash);
            entry.timestamp = 0;
            entry.offset = offset;
            entry.size = size;
            if (resMagic == 0x42505262) this.rootProfileEntry = entry;
            else this.entries.add(entry);
        }
        
        if (!isStreamingChunk) {
            this.parseProfile();
            
            // Wait until we parse the profile to add the nodes
            // so we can add plans that aren't in the inventory properly
            if (!isStreamingChunk)
                for (FileEntry entry : this.entries)
                    this.addNode(entry);
            
            // Save key is aligned against a 4 byte boundary
            if (data.offset % 4 != 0)
                data.forward(4 - (data.offset % 4));
            this.saveKey = data.bytes(tableOffset - data.offset);
        }
        
        this.isParsed = true;
    }

    public FileNode addNode(FileEntry entry) {
        String extension = new String(new byte[] { entry.data[0], entry.data[1], entry.data[2] }).toLowerCase();
        switch (extension) {
            case "ÿøÿ":
            case "jfi":
                extension = "jpg";
                break;
            case "vop":
                extension = "vop";
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
        
        if (extension.equals("bin")) return null;
        if (extension.equals("plan")) {
            // Don't add items if they're in our inventory
            ResourceDescriptor plan = new ResourceDescriptor(entry.hash, ResourceType.PLAN);
            for (InventoryItem item : this.bigProfile.inventory)
                if (plan.equals(item.plan))
                    return null;
        }

        entry.path = "resources/";
        if (extension.equals("mol"))
            entry.path += "meshes/";
        else if (extension.equals("tex") || extension.equals("jpg") || extension.equals("png"))
            entry.path += "textures/";
        else if (extension.equals("raw"))
            entry.path += "audio/";
        else if (extension.equals("gmt"))
            entry.path += "gfx_materials/";
        else if (extension.equals("anm"))
            entry.path += "animations/";
        else if (extension.equals("bev"))
            entry.path += "bevels/";
        else if (extension.equals("mat"))
            entry.path += "materials/";
        else if (extension.equals("ssp"))
            entry.path += "skeletons/";
        else if (extension.equals("adc"))
            entry.path += "adventure_create_profiles/";
        else if (extension.equals("ads"))
            entry.path += "shared_adventure_data/";
        else if (extension.equals("vop"))
            entry.path += "audio/";
        else if (extension.equals("plan"))
            entry.path += "plans/";
        else
            entry.path += "unknown/";

        entry.path += entry.offset + "." + extension;

        return Nodes.addNode(root, entry);

    }

    private void serializeProfile() {
        int itemCount = this.bigProfile.inventory.size();
        int stringCount = this.bigProfile.stringTable.stringList.size();
        int slotCount = this.bigProfile.myMoonSlots.size();

        Resource originalBigProfile = new Resource(this.rootProfileEntry.data);

        Output output = new Output(
                (InventoryDetails.MAX_SIZE * itemCount) + (itemCount * 0x12) + (Slot.MAX_SIZE * slotCount + 1)
                        + (stringCount * SortString.MAX_SIZE + (SortString.MAX_SIZE * itemCount)) + 0xFFFF,
                originalBigProfile.revision);
        
        new Serializer(output).struct(this.bigProfile, BigProfile.class);
        output.shrink();

        ResourceDescriptor[] dependencies = new ResourceDescriptor[output.dependencies.size()];
        dependencies = output.dependencies.toArray(dependencies);

        this.rootProfileEntry.data = Resource.compressToResource(output, ResourceType.BIG_PROFILE);
        this.rootProfileEntry.size = this.rootProfileEntry.data.length;
        this.rootProfileEntry.hash = SHA1.fromBuffer(this.rootProfileEntry.data);

        this.setSaveKeyRootHash(this.rootProfileEntry.hash);
    }

    private void parseProfile() {
        Data profile = new Resource(this.rootProfileEntry.data).handle;
        this.rootProfileEntry.revision = profile.revision;
        
        this.revision = (profile.revision.isAfterLBP3Revision(0x105)) ? 3 : 1;
        
        this.bigProfile = new Serializer(profile).struct(null, BigProfile.class);
        
        for (InventoryItem item : this.bigProfile.inventory)
            this.addItemNode(item);
        ArrayList<Slot> slots =  new ArrayList<>(this.bigProfile.myMoonSlots.values());
        for (Slot slot : slots)
            this.addSlotNode(slot);
        
        // Update used slots map
        this.checkForSlotChanges();

        this.rootProfileEntry.setResource("slots", slots);
        this.rootProfileEntry.setResource("items", this.bigProfile.inventory);
    }

    public void checkForSlotChanges() {
        this.usedSlots = new boolean[82];
        for (Slot slot : this.bigProfile.myMoonSlots.values()) {
            if (slot.id.type == SlotType.USER_CREATED_STORED_LOCAL) {
                if (!(slot.id.ID > 81) && !(slot.id.ID < 0))
                    this.usedSlots[(int) slot.id.ID] = true;
            }
        }
    }

    public FileEntry find(SHA1 hash) {
        if (hash == null)
            return null;
        for (int i = 0; i < entries.size(); i++) {
            FileEntry entry = entries.get(i);
            if (entry.hash.equals(hash))
                return entry;
        }
        return null;
    }

    public byte[] extract(SHA1 hash) {
        FileEntry entry = this.find(hash);
        if (entry == null) return null;
        return entry.data;
    }

    public void add(byte[] data) { this.add(data, true); }
    public void add(byte[] data, boolean parse) {
        SHA1 hash = SHA1.fromBuffer(data);
        if (this.find(hash) != null) return;
        
        Resource resource = new Resource(data);
        
        FileEntry entry = new FileEntry(data, hash);
        this.entries.add(entry);

        this.shouldSave = true;
        
        if (resource.type == ResourceType.PLAN) {
            if (parse) {
                Serializer serializer = new Serializer(resource.handle);
                Plan item = serializer.struct(null, Plan.class);
                InventoryDetails metadata = null;
                if (item != null) metadata = item.details;
                if (metadata == null) {
                    metadata = new InventoryDetails();
                    System.out.println("Metadata is null, using default values...");
                }
                this.addItem(new ResourceDescriptor(hash, ResourceType.PLAN), metadata);
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

                slot.root = new ResourceDescriptor(hash, ResourceType.LEVEL);

                this.addSlotNode(slot);
            }

            return;
        }

        this.addNode(entry);
    }

    public boolean edit(FileEntry entry, byte[] data) {
        this.shouldSave = true;
        
        SHA1 hash = SHA1.fromBuffer(data);

        Slot slot = entry.getResource("slot");
        InventoryItem item = entry.getResource("profileItem");

        if (item != null) {
            ResourceDescriptor newRes = new ResourceDescriptor(hash, ResourceType.PLAN);
            item.plan = newRes;
            item.details.resource = newRes;
        }

        if (slot != null)
            slot.root = new ResourceDescriptor(hash, ResourceType.LEVEL);

        entry.hash = hash;
        entry.data = data;
        entry.size = data.length;

        entry.resetResources(false);

        return true;
    }

    public void addString(String string, long hash) {
        this.bigProfile.stringTable.add(string, (int) hash & 0xFFFFFFFF);
        this.shouldSave = true;
    }

    public void addItem(ResourceDescriptor resource, InventoryDetails metadata) {
        InventoryItem item = new InventoryItem();
        
        item.plan = resource;
        item.details = metadata;
        if (metadata != null && metadata.dateAdded == 0)
            metadata.dateAdded = new Date().getTime() / 1000;
        item.UID = this.bigProfile.inventory.size() | 0x80000000;
        item.flags = 0;
        
        this.bigProfile.inventory.add(item);
        this.addItemNode(item);
        
        this.shouldSave = true;
    }

    public void addSlot(Slot slot) {
        int index = this.getNextSlot();
        if (index == -1) {
            System.out.println("No more slots available on your moon!");
            return;
        }
        
        Crater crater = this.getCrater(index);
        SlotID id = new SlotID(SlotType.USER_CREATED_STORED_LOCAL, index);

        slot.id = id;
        slot.location = crater.value;

        this.addSlotNode(slot);
        this.shouldSave = true;
    }

    private void addItemNode(InventoryItem item) {
        if (item.plan == null) return;
        
        FileEntry entry = this.find(item.plan.hash);
        if (entry == null) return;

        entry.setResource("profileItem", item);
        entry.path = "items/" + InventoryObjectType.getPrimaryName(item.details.type).toLowerCase() + "/";
        if (item.details.type.contains(InventoryObjectType.USER_COSTUME) || item.details.type.contains(InventoryObjectType.COSTUME)) {
            if ((item.details.subType | InventoryObjectSubType.FULL_COSTUME) != 0)
                entry.path += "outfits/";
            else
                entry.path += "";
        }

        String title;
        if (item.details.userCreatedDetails != null && item.details.userCreatedDetails.title != null)
            title = item.details.userCreatedDetails.title;
        else {
            if (item.details.type.contains(InventoryObjectType.USER_STICKER))
                title = "A Photo";
            else if (item.details.type.contains(InventoryObjectType.USER_POD))
                title = "A Pod";
            else if (item.details.type.contains(InventoryObjectType.USER_COSTUME) || item.details.type.contains(InventoryObjectType.COSTUME))
                title = "A Costume";
            else
                title = "Some kind of object";
        }

        entry.path += title + ".plan";

        item.details.translatedLocation = this.bigProfile.stringTable.get(item.details.locationIndex);
        item.details.translatedCategory = this.bigProfile.stringTable.get(item.details.categoryIndex);

        Nodes.addNode(this.root, entry);
    }

    private void addSlotNode(Slot slot) {
        if (slot != null) {
            this.bigProfile.myMoonSlots.put(slot.id, slot);
            slot.revision = revision;
            if (slot.root == null)
                return;
            FileEntry entry = find(slot.root.hash);
            if (entry != null) {
                entry.setResource("slot", slot);
                int revision = new Resource(extract(entry.hash)).revision.head;
                if (slot.icon != null && slot.icon.hash != null) {
                    FileEntry iconEntry = find(slot.icon.hash);
                    if (iconEntry != null) {
                        Texture texture = iconEntry.getResource("texture");
                        if (texture != null)
                            slot.renderedIcon = Images.getSlotIcon(texture.getImage(),
                                    new Resource(extract(entry.hash)).revision.head);
                    }
                }

                if (slot.renderedIcon == null)
                    slot.renderedIcon = Images.getSlotIcon(null, revision);

                String title = slot.title;
                if (title.isEmpty())
                    title = "Unnamed Level";
                entry.path = "slots/" + title + ".bin";
                Nodes.addNode(this.root, entry);
            }
        }
    }

    public void setSaveKeyRootHash(SHA1 hash) {
        int start = this.saveKey.length - 0x3C;
        byte[] hashBuffer = hash.getHash();
        for (int i = start; i < start + 0x14; ++i)
            this.saveKey[i] = hashBuffer[i - start];
    }

    @Override public boolean save(String path) {
        this.serializeProfile();

        FileEntry[] entries = new FileEntry[this.entries.size() + 1];
        for (int i = 0; i < this.entries.size(); ++i)
            entries[i] = this.entries.get(i);
        entries[entries.length - 1] = this.rootProfileEntry;
        
        Arrays.sort(entries, (e1, e2) -> e1.hash.toString().compareTo(e2.hash.toString()));
        
        int size = this.saveKey.length + 0x34 + this.rootProfileEntry.data.length + 0xFFFF;
        for (FileEntry entry : entries)
            size += (0x1C) + entry.size;

        Output output = new Output(size);

        for (FileEntry entry : entries)
            output.bytes(entry.data);

        if (output.offset % 4 != 0)
            output.pad(4 - (output.offset % 4));
        output.bytes(this.saveKey);

        int offset = 0;
        for (FileEntry entry : entries) {
            output.sha1(entry.hash);
            output.i32(offset);
            output.i32(entry.size);
            offset += entry.size;
        }

        // "Fragment" count, how many other
        // parts of this archive were serialized,
        // this isn't fully supported, so we're just writing 0.
        if (this.isFAR5)
            output.pad(0x4);
        
        // Hashinate, used as a signature for
        // profile backups, not necessary here.
        output.pad(0x14);

        output.i32(entries.length);
        if (this.isFAR5)
            output.str("FAR5");
        else
            output.str("FAR4");

        output.shrink();

        FileIO.write(output.buffer, path);

        if (path.equals(this.path))
            this.shouldSave = false;
        
        return true;
    }
}
