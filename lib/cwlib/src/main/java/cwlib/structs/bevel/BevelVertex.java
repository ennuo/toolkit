package cwlib.structs.bevel;

import cwlib.enums.MappingMode;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

import java.util.Locale;

public class BevelVertex implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public float y;
    public float z;
    public float rigidity = 1.0f;
    public byte smoothWithPrevious = 1;
    public byte gmatSlot;
    public MappingMode mappingMode = MappingMode.DAVE;

    @Override
    public void serialize(Serializer serializer)
    {
        y = serializer.f32(y);
        z = serializer.f32(z);
        rigidity = serializer.f32(rigidity);
        smoothWithPrevious = serializer.i8(smoothWithPrevious);
        gmatSlot = serializer.i8(gmatSlot);
        mappingMode = serializer.enum8(mappingMode);
    }

    @Override
    public int getAllocatedSize()
    {
        return BevelVertex.BASE_ALLOCATION_SIZE;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.ROOT, "BevelVertex{v2(%f, %f), %s}", this.y, this.z,
            this.mappingMode);
    }
}
