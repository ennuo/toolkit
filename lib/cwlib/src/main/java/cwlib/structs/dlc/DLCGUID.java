package cwlib.structs.dlc;

import cwlib.enums.DLCFileFlags;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;

public class DLCGUID implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public GUID guid;
    public int flags = DLCFileFlags.NONE;

    @Override
    public void serialize(Serializer serializer)
    {
        guid = serializer.guid(guid);
        flags = serializer.i32(flags);
    }

    @Override
    public int getAllocatedSize()
    {
        return DLCGUID.BASE_ALLOCATION_SIZE;
    }
}
