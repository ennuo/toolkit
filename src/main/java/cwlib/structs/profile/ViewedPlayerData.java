package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.NetworkOnlineID;

public class ViewedPlayerData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public NetworkOnlineID onlineID;
    public int lastReviewCount;
    public int lastCommentCount;
    public int lastPhotosByMeCount;
    public int lastPhotosWithMeCount;
    public int lastFavouriteSlotsCount;
    public int lastFavouriteUsersCount;
    public long lastStreamEventTimestamp;
    public long lastViewedTimestamp;

    @SuppressWarnings("unchecked")
    @Override public ViewedPlayerData serialize(Serializer serializer, Serializable structure) {
        ViewedPlayerData data = (structure == null) ? new ViewedPlayerData() : (ViewedPlayerData) structure;

        data.onlineID = serializer.struct(data.onlineID, NetworkOnlineID.class);
        data.lastReviewCount = serializer.i32(data.lastReviewCount);
        data.lastCommentCount = serializer.i32(data.lastCommentCount);
        data.lastPhotosByMeCount = serializer.i32(data.lastPhotosByMeCount);
        data.lastPhotosWithMeCount = serializer.i32(data.lastPhotosWithMeCount);
        data.lastFavouriteSlotsCount = serializer.i32(data.lastFavouriteSlotsCount);
        data.lastFavouriteUsersCount = serializer.i32(data.lastFavouriteUsersCount);
        data.lastStreamEventTimestamp = serializer.i64(data.lastStreamEventTimestamp);
        data.lastViewedTimestamp = serializer.i64(data.lastViewedTimestamp);

        return data;
    }

    @Override public int getAllocatedSize() { return ViewedPlayerData.BASE_ALLOCATION_SIZE; }
}
