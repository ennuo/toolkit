package ennuo.craftworld.resources.structs;

import ennuo.craftworld.ex.SerializationException;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;

public class InventoryItem implements Serializable {
    public ResourceDescriptor plan;
    public InventoryDetails details = new InventoryDetails();
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
        item.details = serializer.struct(item.details, InventoryDetails.class);
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
