package cwlib.structs.script;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class TypeOffset implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int typeReferenceIdx;
    public int offset;

    @Override
    public void serialize(Serializer serializer)
    {
        typeReferenceIdx = serializer.i32(typeReferenceIdx);
        offset = serializer.i32(offset);
    }

    @Override
    public int getAllocatedSize()
    {
        return TypeOffset.BASE_ALLOCATION_SIZE;
    }
}
