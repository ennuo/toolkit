package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.NetworkOnlineID;

public class ViewedPlayerData implements Serializable
{
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

    @Override
    public void serialize(Serializer serializer)
    {
        onlineID = serializer.struct(onlineID, NetworkOnlineID.class);
        lastReviewCount = serializer.s32(lastReviewCount);
        lastCommentCount = serializer.s32(lastCommentCount);
        lastPhotosByMeCount = serializer.s32(lastPhotosByMeCount);
        lastPhotosWithMeCount = serializer.s32(lastPhotosWithMeCount);
        lastFavouriteSlotsCount = serializer.s32(lastFavouriteSlotsCount);
        lastFavouriteUsersCount = serializer.s32(lastFavouriteUsersCount);
        lastStreamEventTimestamp = serializer.s64(lastStreamEventTimestamp);
        lastViewedTimestamp = serializer.s64(lastViewedTimestamp);
    }

    @Override
    public int getAllocatedSize()
    {
        return ViewedPlayerData.BASE_ALLOCATION_SIZE;
    }
}
