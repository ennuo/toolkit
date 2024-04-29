package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class SortString implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int lamsKeyID;
    public String string;
    public int index;

    public SortString() { }

    public SortString(int key, String string, int index)
    {
        this.lamsKeyID = key;
        this.string = string;
        this.index = index;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        lamsKeyID = serializer.i32(lamsKeyID);
        string = serializer.wstr(string);
        index = serializer.i32(index);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = SortString.BASE_ALLOCATION_SIZE;
        if (this.string != null)
            size += (this.string.length() * 2);
        return size;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this) return true;
        if (!(other instanceof SortString)) return false;
        return ((SortString) other).string.equals(this.string);
    }

    @Override
    public int hashCode()
    {
        return this.string.hashCode();
    }
}
