package cwlib.structs.staticmesh;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;
import org.joml.Vector4f;

public class StaticPrimitive implements Serializable {
    public Vector4f obbMin, obbMax;
    public ResourceReference gmat;
    public int vertexStart, indexStart;
    public int numIndices;
    
    /* Not actually serialized, just used for exporting */
    public int numVerts;
    
    public byte type = 7;

    @SuppressWarnings("unchecked")
    @Override public StaticPrimitive serialize(Serializer serializer, Serializable structure) {
        StaticPrimitive primitive = (structure == null) ? new StaticPrimitive() : (StaticPrimitive) structure;

        primitive.obbMin = serializer.v4(primitive.obbMin);
        primitive.obbMax = serializer.v4(primitive.obbMax);
        primitive.gmat = serializer.resource(primitive.gmat, ResourceType.GFX_MATERIAL);
        primitive.vertexStart = serializer.i32(primitive.vertexStart);
        primitive.indexStart = serializer.i32(primitive.indexStart);
        primitive.numIndices = serializer.i32(primitive.numIndices);
        primitive.type = serializer.i8(primitive.type);

        return primitive;
    }
}
