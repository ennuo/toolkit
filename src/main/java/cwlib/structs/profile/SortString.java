package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class SortString implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int lamsKeyID;
    public String string;
    public int index;

    public SortString() {};
    public SortString(int key, String string, int index) {
        this.lamsKeyID = key;
        this.string = string;
        this.index = index;
    }

    @SuppressWarnings("unchecked")
    @Override public SortString serialize(Serializer serializer, Serializable structure) {
        SortString string = (structure == null) ? new SortString() : (SortString) structure;

        string.lamsKeyID = serializer.i32(string.lamsKeyID);
        string.string = serializer.wstr(string.string);
        string.index = serializer.i32(string.index);
        
        return string;
    }

    @Override public int getAllocatedSize() {
        int size = SortString.BASE_ALLOCATION_SIZE;
        if (this.string != null)
            size += (this.string.length() * 2);
        return size;
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SortString)) return false;
        SortString otherString = (SortString) other;
        return otherString.string.equals(this.string);
    }

    @Override public int hashCode() {
        return this.string.hashCode();
    }
}
