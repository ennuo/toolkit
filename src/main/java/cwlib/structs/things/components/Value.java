package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;

public class Value implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public int fluff;
    public ResourceDescriptor icon;
    public int type, subType;
    public GlobalThingDescriptor linkTo;
    public SlotID linkSlotID;
    public int linkType;
    public int lastUsed, numUses;
    public String name, location, category;
    public byte color, size;

    @SuppressWarnings("unchecked")
    @Override public Value serialize(Serializer serializer, Serializable structure) {
        Value value = (structure == null) ? new Value() : (Value) structure;

        value.fluff = serializer.i32(value.fluff);
        value.icon = serializer.resource(value.icon, ResourceType.TEXTURE);
        value.type = serializer.i32(value.type);
        value.subType = serializer.i32(value.subType);
        value.linkTo = serializer.struct(value.linkTo, GlobalThingDescriptor.class);
        value.linkSlotID = serializer.struct(value.linkSlotID, SlotID.class);
        value.linkType = serializer.i32(value.linkType);
        value.lastUsed = serializer.i32(value.lastUsed);
        value.numUses = serializer.i32(value.numUses);
        value.name = serializer.str(value.name);
        value.location = serializer.str(value.location);
        value.category = serializer.str(value.category);
        value.color = serializer.i8(value.color);
        value.size = serializer.i8(value.size);

        return value;
    }

    @Override public int getAllocatedSize() {
        int size = Value.BASE_ALLOCATION_SIZE;
        if (this.name != null) size += this.name.length();
        if (this.location != null) size += this.location.length();
        if (this.category != null) size += this.category.length();
        return size;
    }
}
