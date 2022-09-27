package cwlib.types.save;

import cwlib.enums.ResourceType;
import cwlib.resources.RAdventureCreateProfile;
import cwlib.resources.RPlan;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.types.Resource;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileEntry;

public final class SaveEntry extends FileEntry {
    /**
     * Item associated with this plan entry.
     */
    private InventoryItem item;

    /**
     * Adventure entry that owns this level.
     */
    private SaveEntry adventure;

    /**
     * ID of slot, used for adventure sublevels.
     */
    private SlotID slotID;

    /**
     * Slot that contains metadata for this level in the save.
     */
    private Slot slot;

    // Used for non-item/level resources
    public SaveEntry(BigSave database, String path, long size, SHA1 sha1) {
        super(database, path, sha1, size);
    }

    public SaveEntry(BigSave database, InventoryItem item, String path, long size, SHA1 sha1) {
        super(database, path, sha1, size);
        this.item = item;
    }

    public SaveEntry(BigSave database, SaveEntry adventure, SlotID slotID, String path, long size, SHA1 sha1) {
        super(database, path, sha1, size);
        this.adventure = adventure;
        this.slotID = slotID;
    }

    public SaveEntry(BigSave database, Slot slot, String path, long size, SHA1 sha1) {
        super(database, path, sha1, size);
        this.slot = slot;
        this.slotID = slot.id;
    }

    public BigSave getSave() { return (BigSave) this.source; }

    public SaveEntry getAdventure() { return this.adventure; }
    public SlotID getSlotID() { return this.slotID; }
    public Slot getSlot() { return this.slot; }

    public InventoryItem getItem() { return this.item; }

    public boolean isAdventureLevel() { return this.adventure != null && this.slotID != null; }
    public boolean isLevel() { return this.slot != null; }
    public boolean isItem() { return this.item != null; }

    public byte[] extract() { return this.getSave().extract(this.getSHA1()); }

    @Override public void setDetails(byte[] buffer) {
        if (buffer == null) 
            throw new NullPointerException("Buffer cannot be null!");
        super.setDetails(buffer);
        SaveArchive archive = this.getSave().getArchive();
        SHA1 sha1 = archive.add(buffer);

        if (this.isAdventureLevel()) {
            Resource resource = new Resource(archive.extract(this.adventure.getSHA1()));
            RAdventureCreateProfile profile = resource.loadResource(RAdventureCreateProfile.class);
            Slot slot = profile.getAdventureSlots().get(this.slotID);
            slot.root = new ResourceDescriptor(sha1, ResourceType.LEVEL);
            byte[] build = Resource.compress(profile.build(resource.getRevision(), resource.getCompressionFlags()));
            this.adventure.setDetails(build);
        }

        if (this.isLevel()) {
            if (this.slot.isAdventure()) this.slot.adventure = new ResourceDescriptor(sha1, ResourceType.ADVENTURE_CREATE_PROFILE);
            else this.slot.root = new ResourceDescriptor(sha1, ResourceType.LEVEL);
        }

        if (this.isItem()) {
            ResourceDescriptor descriptor = new ResourceDescriptor(sha1, ResourceType.PLAN);
            this.item.plan = descriptor;
            try {
                RPlan plan = new Resource(buffer).loadResource(RPlan.class);
                item.details = plan.inventoryData;
            } catch (Exception ex) {
                System.err.println("An error occurred parsing inventory details, ignoring...");
                // I'm not going to stop you from using invalid data,
                // but that ain't updating the cached details.
            }
        }
    }

    @Override public String toString() {
        return String.format("SaveEntry (%s, %s)", this.path, this.sha1);
    }
}
