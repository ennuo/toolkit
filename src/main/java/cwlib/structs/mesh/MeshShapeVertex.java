package cwlib.structs.mesh;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MeshShapeVertex implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x24;

    public Vector4f localPos, localNormal;
    public int boneIndex;

    @SuppressWarnings("unchecked")
    @Override public MeshShapeVertex serialize(Serializer serializer, Serializable structure) {
        MeshShapeVertex vert = 
            (structure == null) ? new MeshShapeVertex() : (MeshShapeVertex) structure;

        vert.localPos = serializer.v4(vert.localPos);
        vert.localNormal = serializer.v4(vert.localNormal);
        vert.boneIndex = serializer.i32(vert.boneIndex);

        return vert;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }

}
