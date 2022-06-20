package cwlib.structs.profile;

import cwlib.enums.InventoryItemFlags;
import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

/**
 * Represents an instance of an item in your inventory.
 * Used in the RLocalProfile and RBigProfile.
 */
public class InventoryItem implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    /**
     * The reference to the actual plan data used by this item.
     */
    public ResourceDescriptor plan;

    /**
     * Cache for inventory details of this item.
     */
    public InventoryItemDetails details = new InventoryItemDetails();

    /**
     * Cache of GUID for this item.
     */
    public GUID guid;

    /**
     * Unique ID of this item in your inventory(?)
     */
    public int UID;

    /**
     * Tutorial related data, see ETutorialLevels
     */
    public int tutorialLevel, tutorialVideo;

    /**
     * State of the item in your inventory,
     * whether it's hearted, uploaded, etc
     */
    public int flags = InventoryItemFlags.NONE;

    /**
     * Index of user defined category in the string table.
     */
    public int userCategoryIndex;

    @SuppressWarnings("unchecked")
    @Override public InventoryItem serialize(Serializer serializer, Serializable structure) {
        InventoryItem item = (structure == null) ? new InventoryItem() : (InventoryItem) structure;

        item.plan = serializer.resource(item.plan, ResourceType.PLAN, true);

        if (serializer.getRevision().isAfterLBP3Revision(0x105))
            item.guid = serializer.guid(item.guid);
        
        item.details = serializer.struct(item.details, InventoryItemDetails.class);

        int head = serializer.getRevision().getVersion();

        if (head > 0x234) {
            item.UID = serializer.i32(item.UID, true);
            if (head < 0x36c) {
                item.tutorialLevel = serializer.i32(item.tutorialLevel, true);
                item.tutorialVideo = serializer.i32(item.tutorialVideo, true);
            }
            item.flags = serializer.i32(item.flags, true);
            if (head > 0x349)
                item.userCategoryIndex = serializer.i32(item.userCategoryIndex, true); 
        } else throw new SerializationException("InventoryItem's below r565 are not supported!");

        return item;
    }

    @Override public int getAllocatedSize() { 
        return InventoryItem.BASE_ALLOCATION_SIZE + this.details.getAllocatedSize(); 
    }
}
