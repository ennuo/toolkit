package cwlib.structs.level;

import java.util.ArrayList;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.structs.streaming.StreamingID;

public class StartPoint implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public SlotID slot;
    public ArrayList<StreamingID> ids = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public StartPoint serialize(Serializer serializer, Serializable structure) {
        StartPoint start = (structure == null) ? new StartPoint() : (StartPoint) structure;

        start.slot = serializer.struct(start.slot, SlotID.class);
        start.ids = serializer.arraylist(start.ids, StreamingID.class);

        return start;
    }

    @Override public int getAllocatedSize() {
        int size = StartPoint.BASE_ALLOCATION_SIZE;
        if (this.ids != null) {
            for (StreamingID id : this.ids)
                size += id.getAllocatedSize();
        }
        return size;
    }
}
