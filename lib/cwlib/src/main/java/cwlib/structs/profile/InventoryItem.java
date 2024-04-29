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
public class InventoryItem implements Serializable
{
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
    @GsonRevision(min = 262, lbp3 = true)
    public GUID guid;

    /**
     * Unique ID of this item in your inventory(?)
     */
    public int UID;

    /**
     * Tutorial related data, see ETutorialLevels
     */
    @GsonRevision(max = 875)
    public TutorialLevel tutorialLevel = TutorialLevel.UNKNOWN;
    @GsonRevision(max = 875)
    public TutorialLevel tutorialVideo = TutorialLevel.UNKNOWN;

    /**
     * State of the item in your inventory,
     * whether it's hearted, uploaded, etc
     */
    public int flags = InventoryItemFlags.NONE;

    /**
     * Index of user defined category in the string table.
     */
    @GsonRevision(min = 842)
    public int userCategoryIndex;


    public InventoryItem() { }

    public InventoryItem(int UID, ResourceDescriptor descriptor, InventoryItemDetails details)
    {
        if (details == null)
            details = new InventoryItemDetails();

        details.dateAdded = new Date().getTime() / 1000;

        this.UID = UID;
        this.details = details;
        this.plan = descriptor;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        plan = serializer.resource(plan, ResourceType.PLAN, true);
        if (plan != null)
            serializer.addDependency(plan);

        if (serializer.getRevision().getSubVersion() >= Revisions.ITEM_GUID)
            guid = serializer.guid(guid);

        details = serializer.struct(details, InventoryItemDetails.class);

        int version = serializer.getRevision().getVersion();

        if (version >= Revisions.ITEM_FLAGS)
        {
            UID = serializer.i32(UID, true);
            if (version < Revisions.REMOVE_LBP1_TUTORIALS)
            {
                if (serializer.isWriting())
                {
                    serializer.getOutput().i32(tutorialLevel.getValue(), true);
                    serializer.getOutput().i32(tutorialVideo.getValue(), true);
                }
                else
                {
                    tutorialLevel =
                        TutorialLevel.fromValue(serializer.getInput().i32(true));
                    tutorialVideo =
                        TutorialLevel.fromValue(serializer.getInput().i32(true));
                }
            }
            flags = serializer.i32(flags, true);
            if (version >= Revisions.USER_CATEGORIES)
                userCategoryIndex = serializer.i32(userCategoryIndex, true);
        }
        else throw new SerializationException("InventoryItem's below r565 are not supported!");
    }

    @Override
    public int getAllocatedSize()
    {
        return InventoryItem.BASE_ALLOCATION_SIZE + this.details.getAllocatedSize();
    }
}
