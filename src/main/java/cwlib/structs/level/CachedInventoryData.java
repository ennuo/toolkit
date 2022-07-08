package cwlib.structs.level;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class CachedInventoryData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;
    
    public long category, location;
    public int cachedToolType;
    // CachedUVs
    public byte U0, U1, V0, V1;

    @SuppressWarnings("unchecked")
    @Override public CachedInventoryData serialize(Serializer serializer, Serializable structure) {
        CachedInventoryData data = (structure == null) ? new CachedInventoryData() : (CachedInventoryData) structure;

        int version = serializer.getRevision().getVersion();

        data.category = serializer.u32(data.category);
        data.location = serializer.u32(data.location);
        if (version > 0x39d) {
            data.cachedToolType = serializer.i32(data.cachedToolType);

            data.U0 = serializer.i8(data.U0);
            data.U1 = serializer.i8(data.U1);
            data.V0 = serializer.i8(data.V0);
            data.V1 = serializer.i8(data.V1);
        }

        return data;
    }

    @Override public int getAllocatedSize() { return CachedInventoryData.BASE_ALLOCATION_SIZE; }
}
