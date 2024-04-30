package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PStreamingHint implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public int type;
    public Vector3f offset, size;

    @GsonRevision(lbp3 = true, min = 0x114)
    public Thing relativeToThing;

    @GsonRevision(lbp3 = true, min = 0x133)
    public Thing[] connected;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        type = serializer.i32(type);
        offset = serializer.v3(offset);
        size = serializer.v3(size);

        if (subVersion > 0x113)
            relativeToThing = serializer.thing(relativeToThing);
        if (subVersion >= 0x133)
            connected = serializer.thingarray(connected);
    }


    @Override
    public int getAllocatedSize()
    {
        int size = PStreamingHint.BASE_ALLOCATION_SIZE;
        if (this.connected != null)
            size += (this.connected.length * 0x4);
        return size;
    }
}
