package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MeshShapeInfo implements Serializable {
    public static int MAX_SIZE = 0x8;
    
    public int numVerts;
    public int isPointCloud;

    public MeshShapeInfo serialize(Serializer serializer, Serializable structure) {
        MeshShapeInfo info = (structure == null) ? new MeshShapeInfo() : (MeshShapeInfo) structure;
        
        info.numVerts = serializer.i32(info.numVerts);
        info.isPointCloud = serializer.i32(info.isPointCloud);
        
        return info;
    }
}
