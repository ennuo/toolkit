package cwlib.structs.streaming;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class QuestSwitch implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public StreamingID questID;
    public int questAction;
    public int questKey;
    public int objectiveID;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x118)
        {
            questID = serializer.struct(questID, StreamingID.class);
            questAction = serializer.i32(questAction);
        }

        if (subVersion > 0x140)
            questKey = serializer.i32(questKey);

        if (subVersion > 0x17a)
            objectiveID = serializer.i32(objectiveID);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = QuestSwitch.BASE_ALLOCATION_SIZE;
        if (this.questID != null)
            size += this.questID.getAllocatedSize();
        return size;
    }
}
