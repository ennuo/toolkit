package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PStreamingData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public byte hidden;
    @GsonRevision(lbp3 = true, min = 0xde)
    public short originator;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        hidden = serializer.i8(hidden);
        if (subVersion > 0xdd)
            originator = serializer.i16(originator);
    }

    @Override
    public int getAllocatedSize()
    {
        return PStreamingData.BASE_ALLOCATION_SIZE;
    }
}
