package cwlib.structs.streaming;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class StreamingCheckpoint implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public int type;
    public Vector3f position;
    public StreamingID startPointName;
    public SlotID slotID;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x72)
        {
            type = serializer.i32(type);
            position = serializer.v3(position);
        }

        if (subVersion > 0xdc)
            startPointName = serializer.struct(startPointName,
                StreamingID.class);

        if (subVersion >= 0x13b)
            slotID = serializer.struct(slotID, SlotID.class);

        if (subVersion > 0x115 && subVersion <= 0x128)
            serializer.s32(0);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = StreamingCheckpoint.BASE_ALLOCATION_SIZE;
        if (this.startPointName != null)
            size += this.startPointName.getAllocatedSize();
        return size;
    }
}
