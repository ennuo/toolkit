package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class GlobalThingDescriptor implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceDescriptor levelDesc;
    public int UID;

    @Override
    public void serialize(Serializer serializer)
    {
        levelDesc = serializer.resource(levelDesc, ResourceType.LEVEL, true
            , false, false);
        UID = serializer.i32(UID);
    }

    @Override
    public int getAllocatedSize()
    {
        return GlobalThingDescriptor.BASE_ALLOCATION_SIZE;
    }
}
