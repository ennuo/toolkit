package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PStreamingHint implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public int type;
    public Vector3f offset, size;

    @GsonRevision(lbp3=true, min=0x114)
    public Thing relativeToThing;

    @GsonRevision(lbp3=true, min=0x133)
    public Thing[] connected;

    @SuppressWarnings("unchecked")
    @Override public PStreamingHint serialize(Serializer serializer, Serializable structure) {
        PStreamingHint hint = (structure == null) ? new PStreamingHint() : (PStreamingHint) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        hint.type = serializer.i32(hint.type);
        hint.offset = serializer.v3(hint.offset);
        hint.size = serializer.v3(hint.size);

        if (subVersion > 0x113)
            hint.relativeToThing = serializer.thing(hint.relativeToThing);
        if (subVersion >= 0x133)
            hint.connected = serializer.thingarray(hint.connected);

        return hint;
    }


    @Override public int getAllocatedSize() {
        int size = PStreamingHint.BASE_ALLOCATION_SIZE;
        if (this.connected != null)
            size += (this.connected.length * 0x4);
        return size;
    }
}
