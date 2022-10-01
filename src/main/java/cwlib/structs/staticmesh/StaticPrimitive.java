package cwlib.structs.staticmesh;

import cwlib.enums.PrimitiveType;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;
import org.joml.Vector4f;

public class StaticPrimitive implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public Vector4f min, max;
    public ResourceDescriptor gmat;
    public int vertexStart, indexStart;
    public int numIndices;
    public PrimitiveType type = PrimitiveType.GL_TRIANGLES;

    /* Not actually serialized, just used for exporting */
    public transient int numVerts;

    @SuppressWarnings("unchecked")
    @Override public StaticPrimitive serialize(Serializer serializer, Serializable structure) {
        StaticPrimitive primitive = (structure == null) ? new StaticPrimitive() : (StaticPrimitive) structure;

        primitive.min = serializer.v4(primitive.min);
        primitive.max = serializer.v4(primitive.max);
        primitive.gmat = serializer.resource(primitive.gmat, ResourceType.GFX_MATERIAL);
        primitive.vertexStart = serializer.i32(primitive.vertexStart);
        primitive.indexStart = serializer.i32(primitive.indexStart);
        primitive.numIndices = serializer.i32(primitive.numIndices);
        primitive.type = serializer.enum8(primitive.type);

        return primitive;
    }

    @Override public int getAllocatedSize() { return StaticPrimitive.BASE_ALLOCATION_SIZE; }
}
