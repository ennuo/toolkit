package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * I don't think either of these variable
 * names or correct, or if this is actually some
 * other structure, but only place I've seen it used,
 * so whatever.
 */
public class MysteryPodEventSeen implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int id;
    public int type;

    @SuppressWarnings("unchecked")
    @Override public MysteryPodEventSeen serialize(Serializer serializer, Serializable structure) {
        MysteryPodEventSeen event = (structure == null) ? new MysteryPodEventSeen() : (MysteryPodEventSeen) structure;

        event.id = serializer.i32(event.id);
        event.type = serializer.s32(event.type);

        return event;
    }

    @Override public int getAllocatedSize() {
        return MysteryPodEventSeen.BASE_ALLOCATION_SIZE;
    }
}
