package cwlib.types;

import cwlib.resources.RAdventureCreateProfile;
import cwlib.io.streams.MemoryInputStream;
import cwlib.util.FileIO;
import cwlib.util.Images;
import toolkit.utilities.ResourceSystem;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.archives.Fat;
import cwlib.types.archives.SaveArchive;
import cwlib.types.archives.SaveKey;
import cwlib.types.data.ResourceDescriptor;
import cwlib.resources.RTexture;
import cwlib.structs.slot.Slot;
import cwlib.enums.Crater;
import cwlib.enums.DatabaseType;
import cwlib.enums.ResourceType;
import cwlib.enums.SlotType;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.slot.SlotID;
import cwlib.structs.profile.SortString;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.util.Nodes;
import cwlib.resources.RPlan;
import cwlib.enums.CompressionFlags;
import cwlib.enums.CostumePieceCategory;
import cwlib.enums.InventoryObjectSubType;
import cwlib.enums.InventoryObjectType;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileEntry;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RBigProfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

// I really want to burn this class.
// It's so messy! Why is it reimplementing a FAR4/5!
public class BigSave extends FileData {
    private ArrayList<FileEntry> entries;
    
    /**
     * Used for determing which moon model positions to use
     * LBP1/LBP3
     */
    public int revision = 1;
    
    /**
     * Parsed archive containing all file data,
     * as well as save key reference data.
     */
    private SaveArchive archive;

    /**
     * Big profile resource attached to this archive.
     */
    private RBigProfile profile;

    /**
     * Used for names of items added to profile.
     */
    private int lastOffset = 0;

    public BigSave(File file) {
        super(file, DatabaseType.BIGFART);
        this.archive = new SaveArchive(file);
        SaveKey key = this.archive.getKey();
        if (key.getRootType() != ResourceType.BIG_PROFILE)
            throw new IllegalArgumentException("Save archive doesn't have an RBigProfile root resource!");
        this.profile = this.archive.loadResource(key.getRootHash(), RBigProfile.class);
        if (this.profile == null)
            throw new IllegalArgumentException("Unable to locate RBigProfile root resource!");
        
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
        
        if (extension.equals("bin") || extension.equals("adc")) return null;
        if (extension.equals("plan")) {
            // Don't add items if they're in our inventory
            ResourceDescriptor plan = new ResourceDescriptor(entry.hash, ResourceType.PLAN);
            for (InventoryItem item : this.bigProfile.inventory)
                if (plan.equals(item.plan))
                    return null;
        }

        entry.path = "resources/";
        switch (extension) {
            case "mol": entry.path += "meshes/"; break;
            case "tex": case "jpg": case "png":
                entry.path += "textures/"; break;
            case "vop":  entry.path += "audio/"; break;
            case "gmt": entry.path += "gfx_materials/"; break;
            case "anm": entry.path += "animations/"; break;
            case "bev": entry.path += "bevels/"; break;
            case "mat": entry.path += "materials/"; break;
            case "ssp": entry.path += "skeletons/"; break;
            case "adc": entry.path += "adventure_create_profiles/"; break;
            case "ads": entry.path += "shared_adventure_data/"; break;
            case "plan": entry.path += "plans/"; break;
            case "ptg": entry.path += "paintings/"; break;
            case "smh": entry.path += "static_meshes/"; break;
            case "mus": entry.path += "music_settings/"; break;
            default: entry.path += "unknown/"; break;
        }
        
        entry.path += entry.offset + "." + extension;

        return Nodes.addNode(root, entry);

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
        entry.offset = this.lastOffset;
        this.lastOffset += data.length;
        
        this.entries.add(entry);

        this.shouldSave = true;
        
        if (resource.type == ResourceType.PLAN) {
            if (parse) {
                Serializer serializer = new Serializer(resource.handle);
                RPlan item = serializer.struct(null, RPlan.class);
                InventoryItemDetails metadata = null;
                if (item != null) metadata = item.details;
                if (metadata == null) {
                    metadata = new InventoryItemDetails();
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
        FileEntry parentAdventure = entry.getResource("parentAdventure");
        
        if (parentAdventure != null) {
            byte[] adventureData = parentAdventure.data;
            if (data != null) {
                ResourceDescriptor old = new ResourceDescriptor(entry.hash, ResourceType.LEVEL);
                Resource resource = new Resource(adventureData);
                RAdventureCreateProfile profile = new RAdventureCreateProfile(resource);
                for (Slot adventureSlot : profile.adventureSlots.values())
                    if (adventureSlot.root != null && adventureSlot.root.equals(old)) {
                        adventureSlot.root = new ResourceDescriptor(hash, ResourceType.LEVEL);
                        break;
                    }
                byte[] built = profile.build(resource.revision, resource.compressionFlags);
                ResourceSystem.replaceEntry(parentAdventure, built);
            }
        }

        if (item != null) {
            ResourceDescriptor newRes = new ResourceDescriptor(hash, ResourceType.PLAN);
            item.plan = newRes;
            
            try {
                RPlan plan = new RPlan(new Resource(data));
                item.details = plan.details;
                plan.details.locationIndex = (short) this.bigProfile.stringTable.find(plan.details.location);
                plan.details.categoryIndex = (short) this.bigProfile.stringTable.find(plan.details.category);
            } catch (Exception ex) {
                System.err.println("An error occurred parsing inventory details, ignoring...");
                // I'm not going to stop you from using invalid data,
                // but that ain't updating the cached details.
            }
        }

        if (slot != null) {
            if (slot.id.type.equals(SlotType.ADVENTURE_PLANET_LOCAL))
                slot.adventure = new ResourceDescriptor(hash, ResourceType.ADVENTURE_CREATE_PROFILE);
            else
                slot.root = new ResourceDescriptor(hash, ResourceType.LEVEL);
        }

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

    public void addItem(ResourceDescriptor resource, InventoryItemDetails metadata) {
        InventoryItem item = new InventoryItem();
        
        item.plan = resource;
        item.details = metadata;
        if (metadata != null && metadata.dateAdded == 0)
            metadata.dateAdded = new Date().getTime() / 1000;
        item.UID = this.getNextUID();
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
        
        if (item.details.type.contains(InventoryObjectType.USER_PLANET)) {
            if (item.details.subType == InventoryObjectSubType.EARTH) entry.path += "earths/";
            else if (item.details.subType == InventoryObjectSubType.MOON) entry.path += "moons/";
            else if (item.details.subType == InventoryObjectSubType.ADVENTURE) entry.path += "adventure_maps/";
            else if (item.details.subType == InventoryObjectSubType.EXTERNAL) entry.path += "external/";
        }
        
        if (item.details.type.contains(InventoryObjectType.USER_STICKER) || item.details.type.contains(InventoryObjectType.STICKER)) {
            if ((item.details.subType & InventoryObjectSubType.PAINTING) != 0)
                entry.path = "items/paintings/";
            else if (item.details.type.contains(InventoryObjectType.PHOTOBOOTH))
                entry.path = "items/photo_booth/";
            else if (item.details.type.contains(InventoryObjectType.EYETOY))
                entry.path = "items/eyetoy/";
            else if (item.details.type.contains(InventoryObjectType.USER_STICKER))
                entry.path = "items/photos/";
            else
                entry.path = "items/stickers/";
        }
        
        
        
        if (item.details.type.contains(InventoryObjectType.USER_COSTUME) || item.details.type.contains(InventoryObjectType.COSTUME)) {
            
            if (this.gameRevision.isLBP3()) {
                boolean isDwarf = (item.details.subType & InventoryObjectSubType.CREATURE_MASK_DWARF) != 0;
                boolean isGiant = (item.details.subType & InventoryObjectSubType.CREATURE_MASK_GIANT) != 0;
                if (isDwarf && isGiant)
                    entry.path += "bird/";
                else if (isDwarf) entry.path += "dwarf/";
                else if (isGiant) entry.path += "giant/";
                else if ((item.details.subType & InventoryObjectSubType.CREATURE_MASK_QUAD) != 0)
                    entry.path += "quad/";
                else entry.path += "sackboy/";
            }
            
            if ((item.details.subType & InventoryObjectSubType.FULL_COSTUME) != 0)
                entry.path += "outfits/";
            else
                entry.path += (CostumePieceCategory.getPrimaryName(CostumePieceCategory.fromFlags(item.details.subType)) + "/");
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

    private FileNode addSlotNode(Slot slot) { return this.addSlotNode(slot, null); }
    private FileNode addSlotNode(Slot slot, FileNode adventureNode) {
        if (slot != null) {
            if (adventureNode == null)
                this.bigProfile.myMoonSlots.put(slot.id, slot);
            slot.revision = revision;
            boolean isAdventure = slot.id.type.equals(SlotType.ADVENTURE_PLANET_LOCAL);
            boolean isLevel = slot.id.type.equals(SlotType.USER_CREATED_STORED_LOCAL) || slot.id.type.equals(SlotType.ADVENTURE_LEVEL_LOCAL);
            if (!isLevel && !isAdventure) return null;
            if (isAdventure && (slot.adventure == null || slot.adventure.hash == null))  return null;
            if (isLevel  && (slot.root == null || slot.root.hash == null)) return null;
            
            FileEntry entry = null;
            if (isAdventure) entry = this.find(slot.adventure.hash);
            else entry = this.find(slot.root.hash);
            
            Resource root = null;
            if (entry != null) {
                if (adventureNode == null)
                    entry.setResource("slot", slot);
                root = new Resource(extract(entry.hash));
                int revision = root.revision.head;
                if (slot.icon != null && slot.icon.hash != null) {
                    FileEntry iconEntry = find(slot.icon.hash);
                    if (iconEntry != null) {
                        RTexture texture = iconEntry.getResource("texture");
                        if (texture != null)
                            slot.renderedIcon = Images.getSlotIcon(texture.getImage(),
                                    new Resource(extract(entry.hash)).revision.head);
                    }
                }

                if (slot.renderedIcon == null)
                    slot.renderedIcon = Images.getSlotIcon(null, revision);

                String title = slot.title;
                if (title.isEmpty())
                    title = (isAdventure) ? "Unnamed Adventure" : "Unnamed Level";
                if (isAdventure)
                    entry.path = "adventures/" + title + "/adventure.adc";
                else entry.path = "levels/" + title + ".bin";
                        
                
                FileNode node = Nodes.addNode(adventureNode == null ? this.root : adventureNode, entry);
                // Bit hacky, should implement relative paths maybe?
                if (adventureNode != null) {
                    entry.path = adventureNode.path + "levels/" + title + ".bin";
                    node.path = entry.path;
                }
                
                if (isAdventure && root != null) {
                    FileNode parent = (FileNode) node.getParent();
                    RAdventureCreateProfile profile = new Serializer(root.handle).struct(null, RAdventureCreateProfile.class);
                    for (Slot childSlot : profile.adventureSlots.values()) {
                        FileNode child = this.addSlotNode(childSlot, parent);
                        if (child != null)
                            child.entry.setResource("parentAdventure", entry);
                    }
                }
                
                return node;
            }
        }
        return null;
    }

    @Override public boolean save(File file) {
        this.archive.getKey().setRootHash(this.archive.add(Resource.compress(this.profile, this.archive.getGameRevision(), CompressionFlags.USE_NO_COMPRESSION)));
        this.archive.save(file.getAbsolutePath());
        if (file == this.getFile())
            this.hasChanges = false;
        return true;
    }
}
