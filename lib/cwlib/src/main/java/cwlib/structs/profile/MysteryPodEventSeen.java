package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * I don't think either of these variable
 * names or correct, or if this is actually some
 * other structure, but only place I've seen it used,
 * so whatever.
 */
public class MysteryPodEventSeen implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int id;
    public int type;

    @Override
    public void serialize(Serializer serializer)
    {
        id = serializer.i32(id);
        type = serializer.s32(type);
    }

    @Override
    public int getAllocatedSize()
    {
        return MysteryPodEventSeen.BASE_ALLOCATION_SIZE;
    }
}
