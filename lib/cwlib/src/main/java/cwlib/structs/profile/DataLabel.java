package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class DataLabel implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public int labelIndex;
    public String name;

    @Override
    public void serialize(Serializer serializer)
    {
        labelIndex = serializer.i32(labelIndex);
        name = serializer.wstr(name);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = DataLabel.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length() * 2);
        return size;
    }
}
