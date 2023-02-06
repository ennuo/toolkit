package cwlib.structs.streaming;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class QuestSwitch implements Serializable { 
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public StreamingID questID;
    public int questAction;
    public int questKey;
    public int objectiveID;

    @SuppressWarnings("unchecked")
    @Override public QuestSwitch serialize(Serializer serializer, Serializable structure) {
        QuestSwitch sw = (structure == null) ? new QuestSwitch() : (QuestSwitch) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x118) {
            sw.questID = serializer.struct(sw.questID, StreamingID.class);
            sw.questAction = serializer.i32(sw.questAction);
        }

        if (subVersion > 0x140)
            sw.questKey = serializer.i32(sw.questKey);

        if (subVersion > 0x17a)
            sw.objectiveID = serializer.i32(sw.objectiveID);

        return sw;
    }

    @Override public int getAllocatedSize() {
        int size = QuestSwitch.BASE_ALLOCATION_SIZE;
        if (this.questID != null)
            size += this.questID.getAllocatedSize();
        return size;
    }
}
