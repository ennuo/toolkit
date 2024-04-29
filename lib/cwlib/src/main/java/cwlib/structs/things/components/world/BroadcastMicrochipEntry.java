package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class BroadcastMicrochipEntry implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public Thing sourceMicrochip;
    public Thing clonedMicrochip;

    @Override
    public void serialize(Serializer serializer)
    {
        sourceMicrochip = serializer.thing(sourceMicrochip);
        clonedMicrochip = serializer.thing(clonedMicrochip);
    }


    @Override
    public int getAllocatedSize()
    {
        return BroadcastMicrochipEntry.BASE_ALLOCATION_SIZE;
    }
}
