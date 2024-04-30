package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class SoftbodyVertEquivalence implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public short first, count;

    public SoftbodyVertEquivalence() { }

    public SoftbodyVertEquivalence(int first, int count)
    {
        this.first = (short) first;
        this.count = (short) count;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        first = serializer.i16(first);
        count = serializer.i16(count);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
