package cwlib.structs.profile;

import java.util.Date;

import cwlib.enums.InventoryItemFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.TutorialLevel;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
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
    @GsonRevision(min=262, lbp3=true)
    public GUID guid;

    /**
     * Unique ID of this item in your inventory(?)
     */
    public int UID;

    /**
     * Tutorial related data, see ETutorialLevels
     */
    @GsonRevision(max=875)
    public TutorialLevel tutorialLevel = TutorialLevel.UNKNOWN;
    @GsonRevision(max=875)
    public TutorialLevel tutorialVideo = TutorialLevel.UNKNOWN;

    /**
     * State of the item in your inventory,
     * whether it's hearted, uploaded, etc
     */
    public int flags = InventoryItemFlags.NONE;

    /**
     * Index of user defined category in the string table.
     */
    @GsonRevision(min=842)
    public int userCategoryIndex;


    public InventoryItem() {};
    public InventoryItem(int UID, ResourceDescriptor descriptor, InventoryItemDetails details) {
        if (details == null) 
            details = new InventoryItemDetails();
        
        details.dateAdded = new Date().getTime() / 1000;

        this.UID = UID;
        this.details = details;
        this.plan = descriptor;
    }

    @SuppressWarnings("unchecked")
    @Override public InventoryItem serialize(Serializer serializer, Serializable structure) {
        InventoryItem item = (structure == null) ? new InventoryItem() : (InventoryItem) structure;

        item.plan = serializer.resource(item.plan, ResourceType.PLAN, true);
        if (item.plan != null)
            serializer.addDependency(item.plan);

        if (serializer.getRevision().getSubVersion() >= Revisions.ITEM_GUID)
            item.guid = serializer.guid(item.guid);
        
        item.details = serializer.struct(item.details, InventoryItemDetails.class);

        int version = serializer.getRevision().getVersion();

        if (version >= Revisions.ITEM_FLAGS) {
            item.UID = serializer.i32(item.UID, true);
            if (version < Revisions.REMOVE_LBP1_TUTORIALS) {
                if (serializer.isWriting()) {
                    serializer.getOutput().i32(item.tutorialLevel.getValue(), true);
                    serializer.getOutput().i32(item.tutorialVideo.getValue(), true);
                } else {
                    item.tutorialLevel = TutorialLevel.fromValue(serializer.getInput().i32(true));
                    item.tutorialVideo = TutorialLevel.fromValue(serializer.getInput().i32(true));
                }
            }
            item.flags = serializer.i32(item.flags, true);
            if (version >= Revisions.USER_CATEGORIES)
                item.userCategoryIndex = serializer.i32(item.userCategoryIndex, true); 
        } else throw new SerializationException("InventoryItem's below r565 are not supported!");

        return item;
    }

    @Override public int getAllocatedSize() { 
        return InventoryItem.BASE_ALLOCATION_SIZE + this.details.getAllocatedSize(); 
    }
}
