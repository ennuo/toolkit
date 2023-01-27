package cwlib.types.save;

import cwlib.resources.RAdventureCreateProfile;
import cwlib.util.Bytes;
import cwlib.types.Resource;
import cwlib.types.archives.Fat;
import cwlib.types.archives.SaveArchive;
import cwlib.types.archives.SaveKey;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.structs.slot.Slot;
import cwlib.enums.Crater;
import cwlib.enums.DatabaseType;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.slot.SlotID;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileNode;
import cwlib.enums.Branch;
import cwlib.enums.CompressionFlags;
import cwlib.enums.CostumePieceCategory;
import cwlib.enums.InventoryObjectSubType;
import cwlib.enums.InventoryObjectType;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileEntry;
import cwlib.resources.RBigProfile;
import cwlib.resources.RPlan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BigSave extends FileData {
    private ArrayList<SaveEntry> entries = new ArrayList<>();
    private HashMap<SHA1, SaveEntry> lookup = new HashMap<>();
    
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

    public BigSave(File folder, SaveArchive master) {
        super(folder, DatabaseType.BIGFART);
        this.archive = master;
        this.process();
    }

    public BigSave(File file) {
        super(file, DatabaseType.BIGFART);
        this.archive = new SaveArchive(file);
        if (this.archive.getArchiveRevision() == 5)
            throw new IllegalArgumentException("This is a Vita profile! Use the correct load option!");
        this.process();
    }

    private void process() {
        SaveKey key = this.archive.getKey();
        if (key.getRootType() != ResourceType.BIG_PROFILE)
            throw new IllegalArgumentException("Save archive doesn't have an RBigProfile root resource!");
        this.profile = this.archive.loadResource(key.getRootHash(), RBigProfile.class);
        if (this.profile == null)
            throw new IllegalArgumentException("Unable to locate RBigProfile root resource!");
        
        HashSet<SHA1> locked = new HashSet<>();
        locked.add(key.getRootHash());
        
        for (InventoryItem item : this.profile.inventory) {
            item.details.translatedLocation = this.profile.stringTable.get(item.details.locationIndex);
            item.details.translatedCategory = this.profile.stringTable.get(item.details.categoryIndex);
            ResourceDescriptor plan = item.plan;
            if (plan == null || plan.isGUID()) continue;
            byte[] data = archive.extract(plan.getSHA1()); 
            if (data == null) continue;
            this.entries.add(new SaveEntry(this, item, this.generatePath(item), data.length, plan.getSHA1()));
            locked.add(plan.getSHA1());
        }

        for (Slot slot : this.profile.myMoonSlots.values()) {
            ResourceDescriptor root = (slot.isAdventure()) ? slot.adventure : slot.root;
            if (root == null || root.isGUID()) continue;
            byte[] data = archive.extract(root.getSHA1());
            if (data == null) continue;

            if (slot.isLevel()) {
                String path = "levels/" + ((slot.name.isEmpty()) ? "Unnamed Level" : slot.name);
                this.entries.add(new SaveEntry(this, slot, path, data.length, root.getSHA1()));
                locked.add(root.getSHA1());
            }

            if (slot.isAdventure()) {
                String path = "adventures/" + ((slot.name.isEmpty()) ? "Unnamed Adventure" : slot.name);
                SaveEntry adventure = new SaveEntry(this, slot, path, data.length, root.getSHA1());
                this.entries.add(adventure);
                locked.add(root.getSHA1());

                RAdventureCreateProfile profile = null;
                try { profile = new Resource(data).loadResource(RAdventureCreateProfile.class); }
                catch (Exception ex) { continue; }

                for (Slot local : profile.getAdventureSlots().values()) {
                    if (!local.isLevel() || local.root.isGUID()) continue;
                    String localPath = path + "/levels/" + ((local.name.isEmpty()) ? "Unnamed Level" : local.name);
                    byte[] localData = this.archive.extract(local.root.getSHA1());
                    if (localData == null) continue;
                    this.entries.add(new SaveEntry(this, adventure, local.id, localPath, localData.length, local.root.getSHA1()));
                    locked.add(local.root.getSHA1());
                }
            }
        }

        for (Fat fat : this.archive) {
            SHA1 sha1 = fat.getSHA1();
            if (locked.contains(sha1)) continue;
            byte[] data = fat.extract();
            this.entries.add(new SaveEntry(this, this.generatePath(data, sha1), fat.getSize(), sha1));
        }

        // When loading a Vita profile, everything gets concatenated in memory, so we have to grab it from the queue.
        for (SHA1 sha1 : this.archive.getQueueHashes()) {
            if (locked.contains(sha1)) continue;
            byte[] data = this.archive.extract(sha1);
            this.entries.add(new SaveEntry(this, this.generatePath(data, sha1), data.length, sha1));
        }

        for (SaveEntry entry : this.entries)
            this.lookup.put(entry.getSHA1(), entry);
    }

    public String generatePath(byte[] data, SHA1 sha1) {
        if (data.length < 4) return "resources/unknown/" + sha1.toString();
        String header = new String(new byte[] { data[0], data[1], data[2] });
        ResourceType type = ResourceType.fromMagic(header);
        if (type == ResourceType.INVALID) {
            switch (Bytes.toIntegerBE(data)) {
                case 0xFFD8FFE0: return "resources/textures/" + sha1.toString() + ".jpg";
                case 0x89504E47: return "resources/textures/" + sha1.toString() + ".png";
                default: return "resources/unknown/" + sha1.toString();
            }
        }
        return "resources/" + type.getFolder() + sha1.toString() + type.getExtension();
    }

    @Override public SaveEntry get(SHA1 sha1) { return this.lookup.get(sha1); }
    @Override public byte[] extract(SHA1 sha1) { return this.archive.extract(sha1); }

    @Override public void add(byte[] data) {
        SHA1 sha1 = this.archive.add(data);
        this.setHasChanges();

        if (data.length < 4) return;

        ResourceType type = ResourceType.fromMagic(new String(new byte[] { data[0], data[1], data[2] }));
        SerializationType method = SerializationType.fromValue(Character.toString((char) data[3]));
        SaveEntry entry = null;

        if (method == SerializationType.BINARY) {
            switch (type) {
                case LEVEL: {
                    ResourceDescriptor root = new ResourceDescriptor(sha1, ResourceType.LEVEL);

                    SlotID id = this.profile.getNextSlotID();

                    String version = this.archive.getGameRevision().getSubVersion() > 0x105 ? "_LBP3" : "_LBP1";
                    Crater crater = Crater.valueOf("SLOT_" + (id.slotNumber % 82) + version);

                    Slot slot = new Slot(
                        this.profile.getNextSlotID(),
                        root,
                        crater.getValue()
                    );

                    this.profile.myMoonSlots.put(id, slot);
                    entry = new SaveEntry(this, slot, "levels/Unnamed Level", data.length, sha1);

                    break;
                }
                case PLAN: {
                    RPlan plan = new Resource(data).loadResource(RPlan.class);

                    InventoryItem item = new InventoryItem(
                        this.profile.getNextUID(),
                        new ResourceDescriptor(sha1, ResourceType.PLAN),
                        plan.inventoryData
                    );

                    this.profile.inventory.add(item);
                    entry = new SaveEntry(this, item, this.generatePath(item), data.length, sha1); 

                    break;
                }
                case ADVENTURE_CREATE_PROFILE: {
                    // TODO: Handle this case some other time, not that important.
                    break;
                }
                default: break;
            }
        }

        if (entry == null)
            entry = new SaveEntry(this, this.generatePath(data, sha1), data.length, sha1);

        this.lookup.put(sha1, entry);
        this.entries.add(entry);
    }

    @Override public void remove(FileEntry entry) {
        if (entry.getSource() != this) 
            throw new IllegalArgumentException("SaveEntry doesn't belong to this database!");
        SaveEntry saveEntry = (SaveEntry) entry;
        this.entries.remove(saveEntry);
        if (saveEntry.isLevel())
            this.profile.myMoonSlots.remove(saveEntry.getSlot().id);
        if (saveEntry.isItem())
            this.profile.inventory.remove(saveEntry.getItem());
        if (saveEntry.isAdventureLevel()) {
            // TODO: Handle this case some other time, not that important.
        }
        FileNode node = saveEntry.getNode();
        if (node != null) node.delete();
    }

    private String generatePath(InventoryItem item) {
        String path = "items/" + InventoryObjectType.getPrimaryName(item.details.type).toLowerCase() + "/";
        
        if (item.details.type.contains(InventoryObjectType.USER_PLANET)) {
            if (item.details.subType == InventoryObjectSubType.EARTH) path += "earths/";
            else if (item.details.subType == InventoryObjectSubType.MOON) path += "moons/";
            else if (item.details.subType == InventoryObjectSubType.ADVENTURE) path += "adventure_maps/";
            else if (item.details.subType == InventoryObjectSubType.EXTERNAL) path += "external/";
        }
        
        if (item.details.type.contains(InventoryObjectType.USER_STICKER) || item.details.type.contains(InventoryObjectType.STICKER)) {
            if ((item.details.subType & InventoryObjectSubType.PAINTING) != 0)
                path = "items/paintings/";
            else if (item.details.type.contains(InventoryObjectType.PHOTOBOOTH))
                path = "items/photo_booth/";
            else if (item.details.type.contains(InventoryObjectType.EYETOY))
                path = "items/eyetoy/";
            else if (item.details.type.contains(InventoryObjectType.USER_STICKER))
                path = "items/photos/";
            else
                path = "items/stickers/";
        }
        
        if (item.details.type.contains(InventoryObjectType.USER_COSTUME) || item.details.type.contains(InventoryObjectType.COSTUME)) {    
            if (this.archive.getGameRevision().isLBP3()) {
                boolean isDwarf = (item.details.subType & InventoryObjectSubType.CREATURE_MASK_DWARF) != 0;
                boolean isGiant = (item.details.subType & InventoryObjectSubType.CREATURE_MASK_GIANT) != 0;
                if (isDwarf && isGiant)
                    path += "bird/";
                else if (isDwarf) path += "dwarf/";
                else if (isGiant) path += "giant/";
                else if ((item.details.subType & InventoryObjectSubType.CREATURE_MASK_QUAD) != 0)
                    path += "quad/";
                else path += "sackboy/";
            }
            
            if ((item.details.subType & InventoryObjectSubType.FULL_COSTUME) != 0)
                path += "outfits/";
            else
                path += (CostumePieceCategory.getPrimaryName(CostumePieceCategory.fromFlags(item.details.subType)) + "/");
        }

        String title;
        if (item.details.userCreatedDetails != null && item.details.userCreatedDetails.name != null)
            title = item.details.userCreatedDetails.name;
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

        return path + title + ".plan";
    }

    public RBigProfile getProfile() { return this.profile; }
    public SaveArchive getArchive() { return this.archive; }

    @Override public boolean save(File file) {
        if (this.archive.getArchiveRevision() == 5) {
            throw new UnsupportedOperationException("Saving Vita profiles isn't implemented yet!");
        }

        byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        Revision revision = this.archive.getGameRevision();
        if ((revision.getVersion() >= 0x297) || revision.has(Branch.LEERDAMMER, Revisions.LD_RESOURCES))
            compressionFlags = CompressionFlags.USE_ALL_COMPRESSION;
        
        this.archive.getKey().setRootHash(this.archive.add(Resource.compress(this.profile, revision, compressionFlags)));
        this.archive.save(file.getAbsolutePath());
        if (file == this.getFile())
            this.hasChanges = false;
        return true;
    }
}
