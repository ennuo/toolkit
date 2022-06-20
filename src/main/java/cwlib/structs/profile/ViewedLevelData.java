package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class ViewedLevelData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public SlotID slotID;
    public int lastReviewCount;
    public int lastCommentCount;
    public int lastPhotoCount;
    public int lastAuthorPhotoCount;
    public long lastStreamEventTimestamp;
    public long lastViewedTimestamp;

    @SuppressWarnings("unchecked")
    @Override public ViewedLevelData serialize(Serializer serializer, Serializable structure) {
        ViewedLevelData data = (structure == null) ? new ViewedLevelData() : (ViewedLevelData) structure;

        data.slotID = serializer.struct(data.slotID, SlotID.class);
        data.lastReviewCount = serializer.i32(data.lastReviewCount);
        data.lastCommentCount = serializer.i32(data.lastCommentCount);
        data.lastPhotoCount = serializer.i32(data.lastPhotoCount);
        data.lastAuthorPhotoCount = serializer.i32(data.lastAuthorPhotoCount);
        data.lastStreamEventTimestamp = serializer.i64(data.lastStreamEventTimestamp);
        data.lastViewedTimestamp = serializer.i64(data.lastViewedTimestamp);

        return data;
    }

    @Override public int getAllocatedSize() { return ViewedLevelData.BASE_ALLOCATION_SIZE; }
}
