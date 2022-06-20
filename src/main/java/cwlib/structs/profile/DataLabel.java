package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class DataLabel implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public int labelIndex;
    public String name;

    @SuppressWarnings("unchecked")
    @Override public DataLabel serialize(Serializer serializer, Serializable structure) {
        DataLabel label = (structure == null) ? new DataLabel() : (DataLabel) structure;

        label.labelIndex = serializer.i32(label.labelIndex);
        label.name = serializer.wstr(label.name);

        return label;
    }

    @Override public int getAllocatedSize() {
        int size = DataLabel.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += (this.name.length() * 2);
        return size;
    }
}
