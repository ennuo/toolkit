package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PinAward implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int pinID, awardCount;

    @Override
    public void serialize(Serializer serializer)
    {
        pinID = serializer.i32(pinID);
        awardCount = serializer.i32(awardCount);
    }

    @Override
    public int getAllocatedSize()
    {
        return PinAward.BASE_ALLOCATION_SIZE;
    }
}
