package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Vector4f;

public class ImplicitPlane implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public Vector4f planeNormal, pointInPlane;
    public int parentBone;

    @Override
    public void serialize(Serializer serializer)
    {
        planeNormal = serializer.v4(planeNormal);
        pointInPlane = serializer.v4(pointInPlane);
        parentBone = serializer.i32(parentBone);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
