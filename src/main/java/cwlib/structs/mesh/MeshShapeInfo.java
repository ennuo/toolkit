package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MeshShapeInfo implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public int numVerts;
    public boolean isPointCloud;

    @SuppressWarnings("unchecked")
    @Override public MeshShapeInfo serialize(Serializer serializer, Serializable structure) {
        MeshShapeInfo info = 
            (structure == null) ? new MeshShapeInfo() : (MeshShapeInfo) structure;

        info.numVerts = serializer.i32(info.numVerts);
        info.isPointCloud = serializer.intbool(info.isPointCloud);

        return info;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
