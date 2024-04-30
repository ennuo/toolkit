package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class SlotLink implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public SlotID from = new SlotID();
    public SlotID to = new SlotID();

    @Override
    public void serialize(Serializer serializer)
    {
        from = serializer.struct(from, SlotID.class);
        to = serializer.struct(to, SlotID.class);
    }

    @Override
    public int getAllocatedSize()
    {
        return SlotLink.BASE_ALLOCATION_SIZE;
    }
}
