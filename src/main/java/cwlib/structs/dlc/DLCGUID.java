package cwlib.structs.dlc;

import cwlib.enums.DLCFileFlags;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;

public class DLCGUID implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public GUID guid;
    public int flags = DLCFileFlags.NONE;

    @SuppressWarnings("unchecked")
    @Override public DLCGUID serialize(Serializer serializer, Serializable structure) {
        DLCGUID guid = (structure == null) ? new DLCGUID() : (DLCGUID) structure;

        guid.guid = serializer.guid(guid.guid);
        guid.flags = serializer.i32(guid.flags);
        
        return guid;
    }

    @Override public int getAllocatedSize() { 
        return DLCGUID.BASE_ALLOCATION_SIZE;
    }
}
