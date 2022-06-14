package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Vector4f;

public class MeshShapeVertex implements Serializable {
    public static int MAX_SIZE = 0x25;
    
    public Vector4f localPos, localNormal;
    public int boneIndex;
    
    public MeshShapeVertex serialize(Serializer serializer, Serializable structure) {
        MeshShapeVertex vert = (structure == null) ? new MeshShapeVertex() : (MeshShapeVertex) structure;
        
        vert.localPos = serializer.v4(vert.localPos);
        vert.localNormal = serializer.v4(vert.localNormal);
        vert.boneIndex = serializer.i32(vert.boneIndex);
        
        return vert;
    }
}
