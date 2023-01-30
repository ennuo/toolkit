package cwlib.structs.bevel;

import java.util.Locale;

import cwlib.enums.MappingMode;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class BevelVertex implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public float y;
    public float z;
    public float rigidity = 1.0f;
    public byte smoothWithPrevious = 1;
    public byte gmatSlot;
    public MappingMode mappingMode = MappingMode.DAVE;
    
    @SuppressWarnings("unchecked")
    @Override public BevelVertex serialize(Serializer serializer, Serializable structure) {
        BevelVertex vertex = (structure == null) ? new BevelVertex() : (BevelVertex) structure;

        vertex.y = serializer.f32(vertex.y);
        vertex.z = serializer.f32(vertex.z);
        vertex.rigidity = serializer.f32(vertex.rigidity);
        vertex.smoothWithPrevious = serializer.i8(vertex.smoothWithPrevious);
        vertex.gmatSlot = serializer.i8(vertex.gmatSlot);
        vertex.mappingMode = serializer.enum8(vertex.mappingMode);

        return vertex;
    }

    @Override public int getAllocatedSize() { return BevelVertex.BASE_ALLOCATION_SIZE; }
    
    @Override public String toString() {
        return String.format(Locale.ROOT, "BevelVertex{v2(%f, %f), %s}", this.y, this.z, this.mappingMode);
    }
}
