package cwlib.structs.level;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.structs.streaming.StreamingID;

import java.util.ArrayList;

public class StartPoint implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public SlotID slot;
    public ArrayList<StreamingID> ids = new ArrayList<>();

    @Override
    public void serialize(Serializer serializer)
    {
        slot = serializer.struct(slot, SlotID.class);
        ids = serializer.arraylist(ids, StreamingID.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = StartPoint.BASE_ALLOCATION_SIZE;
        if (this.ids != null)
        {
            for (StreamingID id : this.ids)
                size += id.getAllocatedSize();
        }
        return size;
    }
}
