package cwlib.structs.streaming;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class StreamingCheckpoint implements Serializable { 
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public int type;
    public Vector3f position;
    public StreamingID startPointName;
    public SlotID slotID;

    @SuppressWarnings("unchecked")
    @Override public StreamingCheckpoint serialize(Serializer serializer, Serializable structure) {
        StreamingCheckpoint checkpoint = (structure == null) ? new StreamingCheckpoint() : (StreamingCheckpoint) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x72) {
            checkpoint.type = serializer.i32(checkpoint.type);
            checkpoint.position = serializer.v3(checkpoint.position);
        }

        if (subVersion > 0xdc)
            checkpoint.startPointName = serializer.struct(checkpoint.startPointName, StreamingID.class);
        
        if (subVersion >= 0x13b) 
            checkpoint.slotID = serializer.struct(checkpoint.slotID, SlotID.class);

        if (subVersion > 0x115 && subVersion <= 0x128)
            serializer.s32(0);

        return checkpoint;
    }

    @Override public int getAllocatedSize() {
        int size = StreamingCheckpoint.BASE_ALLOCATION_SIZE;
        if (this.startPointName != null)
            size += this.startPointName.getAllocatedSize();
        return size;
    }
}
