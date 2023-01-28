package cwlib.structs.streaming;

import org.joml.Vector3f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class QuestTracker implements Serializable { 
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public Vector3f position;
    public StreamingID questID;
    public StreamingID objectiveID;
    public int questKey;
    public int objectiveKey;

    @SuppressWarnings("unchecked")
    @Override public QuestTracker serialize(Serializer serializer, Serializable structure) {
        QuestTracker tracker = (structure == null) ? new QuestTracker() : (QuestTracker) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0xeb) {
            tracker.position = serializer.v3(tracker.position);
            tracker.questID = serializer.struct(tracker.questID, StreamingID.class);
        }

        if (subVersion > 0xf3)
            tracker.objectiveID = serializer.struct(tracker.questID, StreamingID.class);

        if (subVersion > 0x140) {
            tracker.questKey = serializer.i32(tracker.questKey);
            tracker.objectiveKey = serializer.i32(tracker.objectiveKey);
        }

        // jenkins(?) hash of questID and objectiveID
        // for keys if revision too early

        return tracker;
    }

    @Override public int getAllocatedSize() {
        int size = QuestTracker.BASE_ALLOCATION_SIZE;
        if (this.questID != null)
            size += this.questID.getAllocatedSize();
        if (this.objectiveID != null)
            size += this.objectiveID.getAllocatedSize();
        return size;
    }
}
