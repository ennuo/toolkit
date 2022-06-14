package cwlib.structs.profile;

import cwlib.ex.SerializationException;
import cwlib.enums.ResourceType;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;

public class InventoryItem implements Serializable {
    public ResourceReference plan;
    public InventoryItemDetails details = new InventoryItemDetails();
    public int GUID = 0;
    public int UID;
    public int tutorialLevel, tutorialVideo;
    public int flags = 0;
    public int userCategoryIndex;

    @SuppressWarnings("unchecked")
    @Override public InventoryItem serialize(Serializer serializer, Serializable structure) {
        InventoryItem item = (structure == null) ? new InventoryItem() : (InventoryItem) structure;
        item.plan = serializer.resource(item.plan, ResourceType.PLAN, true);
        if (serializer.revision.isAfterLBP3Revision(0x105))
            item.GUID = serializer.i32(item.GUID);
        item.details = serializer.struct(item.details, InventoryItemDetails.class);
        int head = serializer.revision.head & 0xFFFF;
        if (head > 0x234) {
            item.UID = serializer.i32f(item.UID);
            if (head < 0x36c) {
                item.tutorialLevel = serializer.i32f(item.tutorialLevel);
                item.tutorialVideo = serializer.i32f(item.tutorialVideo);
            }
            item.flags = serializer.i32f(item.flags);
            if (head > 0x349)
                item.userCategoryIndex = serializer.i32f(item.userCategoryIndex);
        } else throw new SerializationException("InventoryItem's below r565 are not supported!");
        return item;
    }
}
