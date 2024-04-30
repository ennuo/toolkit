package cwlib.structs.streaming;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class StreamingID implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public String name;
    public int type;

    @Override
    public void serialize(Serializer serializer)
    {
        name = serializer.wstr(name);
        type = serializer.i32(type);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = StreamingID.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length() * 0x2);
        return size;
    }
}
