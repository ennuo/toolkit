package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PinProgress implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int progressType, progressCount;

    @Override
    public void serialize(Serializer serializer)
    {
        progressType = serializer.i32(progressType);
        progressCount = serializer.i32(progressCount);
    }

    @Override
    public int getAllocatedSize()
    {
        return PinProgress.BASE_ALLOCATION_SIZE;
    }
}
