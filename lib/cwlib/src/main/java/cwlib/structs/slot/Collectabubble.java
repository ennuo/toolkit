package cwlib.structs.slot;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class Collectabubble implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceDescriptor plan;
    public int count;

    @Override
    public void serialize(Serializer serializer)
    {
        plan = serializer.resource(plan, ResourceType.PLAN, true);
        count = serializer.i32(count);
    }

    @Override
    public int getAllocatedSize()
    {
        return Collectabubble.BASE_ALLOCATION_SIZE;
    }
}