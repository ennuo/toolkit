package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PStreamingData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public byte hidden;
    @GsonRevision(lbp3=true, min=0xde)
    public short originator;

    @SuppressWarnings("unchecked")
    @Override public PStreamingData serialize(Serializer serializer, Serializable structure) {
        PStreamingData data = (structure == null) ? new PStreamingData() : (PStreamingData) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        data.hidden = serializer.i8(data.hidden);
        if (subVersion > 0xdd)
            data.originator = serializer.i16(data.originator);

        return data;
    }

    @Override public int getAllocatedSize() {
        return PStreamingData.BASE_ALLOCATION_SIZE;
    }
}
