package cwlib.structs.level;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class CachedInventoryData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public long category, location;
    public int cachedToolType;
    // CachedUVs
    public byte U0, U1, V0, V1;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        category = serializer.u32(category);
        location = serializer.u32(location);
        if (version > 0x39d)
        {
            cachedToolType = serializer.i32(cachedToolType);

            U0 = serializer.i8(U0);
            U1 = serializer.i8(U1);
            V0 = serializer.i8(V0);
            V1 = serializer.i8(V1);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return CachedInventoryData.BASE_ALLOCATION_SIZE;
    }
}
