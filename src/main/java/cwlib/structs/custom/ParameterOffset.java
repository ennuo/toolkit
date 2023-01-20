package cwlib.structs.custom;

import cwlib.enums.ParameterSubType;
import cwlib.enums.ParameterType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class ParameterOffset implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;
    public static final int UNSWIZZLED_FLAG = 0x80000000;

    public ParameterType type = ParameterType.INVALID;
    public ParameterSubType subType = ParameterSubType.NONE;
    public int[] offsets;

    public ParameterOffset() {};
    public ParameterOffset(ParameterType type, int[] offsets) {
        this.type = type;
        this.offsets = offsets;
    }
    public ParameterOffset(ParameterType type, ParameterSubType subType, int[] offsets) {
        this.type = type;
        this.subType = subType;
        this.offsets = offsets;
    }

    @SuppressWarnings("unchecked")
    @Override public ParameterOffset serialize(Serializer serializer, Serializable structure) {
        ParameterOffset offset = (structure == null) ? new ParameterOffset() : (ParameterOffset) structure;

        offset.type = serializer.enum8(offset.type);
        offset.subType = serializer.enum8(offset.subType);
        offset.offsets = serializer.intarray(offset.offsets);

        return offset;
    }

    @Override public int getAllocatedSize() { 
        int size = ParameterOffset.BASE_ALLOCATION_SIZE;
        if (this.offsets != null)
            size += (this.offsets.length * 0x4);
        return size;
    }


}
