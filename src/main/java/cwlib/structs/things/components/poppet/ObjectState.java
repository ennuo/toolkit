package cwlib.structs.things.components.poppet;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class ObjectState implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x20;

    public Thing thing;
    public int backZ;
    public int frontZ;

    @GsonRevision(min=0x2bd)
    public int flags;

    @SuppressWarnings("unchecked")
    @Override public ObjectState serialize(Serializer serializer, Serializable structure) {
        ObjectState state = (structure == null) ? new ObjectState() : (ObjectState) structure;

        int version = serializer.getRevision().getVersion();

        state.thing = serializer.thing(state.thing);
        state.backZ = serializer.s32(state.backZ);
        state.frontZ = serializer.s32(state.frontZ);

        if (version < 0x2bd) serializer.bool(false);
        if (version > 0x147 && version < 0x2be) serializer.bool(false);

        if (version > 0x2bc)
            state.flags = serializer.i32(state.flags);
        else if (version > 0x25e) serializer.bool(false);

        return state;
    }


    @Override public int getAllocatedSize() { return ObjectState.BASE_ALLOCATION_SIZE; }
}