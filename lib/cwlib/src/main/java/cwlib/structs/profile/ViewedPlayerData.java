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
            lastReviewCount = serializer.i32(lastReviewCount);
            lastCommentCount = serializer.i32(lastCommentCount);
            lastPhotosByMeCount = serializer.i32(lastPhotosByMeCount);
            lastPhotosWithMeCount = serializer.i32(lastPhotosWithMeCount);
            lastFavouriteSlotsCount = serializer.i32(lastFavouriteSlotsCount);
            lastFavouriteUsersCount = serializer.i32(lastFavouriteUsersCount);
            lastStreamEventTimestamp = serializer.i64(lastStreamEventTimestamp);
            lastViewedTimestamp = serializer.i64(lastViewedTimestamp);
      }

      @Override
      public int getAllocatedSize()
      {
            return ViewedPlayerData.BASE_ALLOCATION_SIZE;
      }
}
