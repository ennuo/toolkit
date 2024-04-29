package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;

public class Value implements Serializable
{
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

    @Override
    public void serialize(Serializer serializer)
    {
        fluff = serializer.i32(fluff);
        icon = serializer.resource(icon, ResourceType.TEXTURE);
        type = serializer.i32(type);
        subType = serializer.i32(subType);
        linkTo = serializer.struct(linkTo, GlobalThingDescriptor.class);
        linkSlotID = serializer.struct(linkSlotID, SlotID.class);
        linkType = serializer.i32(linkType);
        lastUsed = serializer.i32(lastUsed);
        numUses = serializer.i32(numUses);
        name = serializer.str(name);
        location = serializer.str(location);
        category = serializer.str(category);
        color = serializer.i8(color);
        size = serializer.i8(size);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = Value.BASE_ALLOCATION_SIZE;
        if (this.name != null) size += this.name.length();
        if (this.location != null) size += this.location.length();
        if (this.category != null) size += this.category.length();
        return size;
    }
}
