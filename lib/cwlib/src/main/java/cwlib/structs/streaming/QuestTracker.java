package cwlib.structs.streaming;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class QuestTracker implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public Vector3f position;
    public StreamingID questID;
    public StreamingID objectiveID;
    public int questKey;
    public int objectiveKey;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0xeb)
        {
            position = serializer.v3(position);
            questID = serializer.struct(questID, StreamingID.class);
        }

        if (subVersion > 0xf3)
            objectiveID = serializer.struct(questID, StreamingID.class);

        if (subVersion > 0x140)
        {
            questKey = serializer.i32(questKey);
            objectiveKey = serializer.i32(objectiveKey);
        }

        // jenkins(?) hash of questID and objectiveID
        // for keys if revision too early
    }

    @Override
    public int getAllocatedSize()
    {
        int size = QuestTracker.BASE_ALLOCATION_SIZE;
        if (this.questID != null)
            size += this.questID.getAllocatedSize();
        if (this.objectiveID != null)
            size += this.objectiveID.getAllocatedSize();
        return size;
    }
}
