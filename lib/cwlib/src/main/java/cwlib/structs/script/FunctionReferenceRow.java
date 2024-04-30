package cwlib.structs.script;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class FunctionReferenceRow implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int typeReferenceIdx;
    public int nameStringIdx;

    @Override
    public void serialize(Serializer serializer)
    {
        typeReferenceIdx = serializer.i32(typeReferenceIdx);
        nameStringIdx = serializer.i32(nameStringIdx);
    }

    @Override
    public int getAllocatedSize()
    {
        return FunctionReferenceRow.BASE_ALLOCATION_SIZE;
    }
}
