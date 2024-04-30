package cwlib.structs.things.components.poppet;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class ObjectState implements Serializable
{
    public static int BASE_ALLOCATION_SIZE = 0x20;

    public Thing thing;
    public int backZ;
    public int frontZ;

    @GsonRevision(min = 0x2bd)
    public int flags;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        thing = serializer.thing(thing);
        backZ = serializer.s32(backZ);
        frontZ = serializer.s32(frontZ);

        if (version < 0x2bd) serializer.bool(false);
        if (version > 0x147 && version < 0x2be) serializer.bool(false);

        if (version > 0x2bc)
            flags = serializer.i32(flags);
        else if (version > 0x25e) serializer.bool(false);
    }


    @Override
    public int getAllocatedSize()
    {
        return ObjectState.BASE_ALLOCATION_SIZE;
    }
}