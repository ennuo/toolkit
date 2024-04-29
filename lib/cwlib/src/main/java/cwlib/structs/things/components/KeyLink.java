package cwlib.structs.things.components;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class KeyLink implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public SlotID slotID;

    @GsonRevision(max = 0x16e)
    @Deprecated
    public int unknown;

    @Override
    public void serialize(Serializer serializer)
    {
        slotID = serializer.struct(slotID, SlotID.class);
        if (serializer.getRevision().getVersion() < 0x16f)
            unknown = serializer.i32(unknown);
    }

    @Override
    public int getAllocatedSize()
    {
        return KeyLink.BASE_ALLOCATION_SIZE;
    }
}
