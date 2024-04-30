package cwlib.structs.profile;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class CollectedBubble implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public ResourceDescriptor level;
    public int thingUID;
    public ResourceDescriptor plan;

    @Override
    public void serialize(Serializer serializer)
    {
        level = serializer.resource(level, ResourceType.LEVEL, true);
        thingUID = serializer.i32(thingUID);
        plan = serializer.resource(plan, ResourceType.PLAN, true);
    }

    @Override
    public int getAllocatedSize()
    {
        return CollectedBubble.BASE_ALLOCATION_SIZE;
    }
}
