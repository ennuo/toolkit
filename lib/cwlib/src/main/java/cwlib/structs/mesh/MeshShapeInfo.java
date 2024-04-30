package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MeshShapeInfo implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public int numVerts;
    public boolean isPointCloud;

    @Override
    public void serialize(Serializer serializer)
    {
        numVerts = serializer.i32(numVerts);
        isPointCloud = serializer.intbool(isPointCloud);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
