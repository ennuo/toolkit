package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Vector4f;

public class MeshShapeVertex implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x24;

    public Vector4f localPos, localNormal;
    public int boneIndex;

    @Override
    public void serialize(Serializer serializer)
    {
        localPos = serializer.v4(localPos);
        localNormal = serializer.v4(localNormal);
        boneIndex = serializer.i32(boneIndex);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }

}
