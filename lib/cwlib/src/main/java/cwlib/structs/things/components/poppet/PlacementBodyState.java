package cwlib.structs.things.components.poppet;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PlacementBodyState implements Serializable
{
    public static int BASE_ALLOCATION_SIZE = 0x10;

    public Thing thing;
    public Thing oldParent;
    public int frozen;

    @Override
    public void serialize(Serializer serializer)
    {
        thing = serializer.thing(thing);
        oldParent = serializer.thing(oldParent);
        frozen = serializer.i32(frozen);
    }


    @Override
    public int getAllocatedSize()
    {
        return PlacementBodyState.BASE_ALLOCATION_SIZE;
    }
}