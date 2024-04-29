package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Treasure implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int treasureID;
    public int planID;
    public int timestamp;

    @Override
    public void serialize(Serializer serializer)
    {
        treasureID = serializer.i32(treasureID);
        planID = serializer.i32(planID);
        timestamp = serializer.i32(timestamp);
    }

    @Override
    public int getAllocatedSize()
    {
        return Treasure.BASE_ALLOCATION_SIZE;
    }
}
