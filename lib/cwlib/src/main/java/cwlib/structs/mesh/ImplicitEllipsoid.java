package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Matrix4f;

public class ImplicitEllipsoid implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public Matrix4f transform;
    public int parentBone, affectWorldOnly;

    @Override
    public void serialize(Serializer serializer)
    {
        transform = serializer.m44(transform);
        parentBone = serializer.i32(parentBone);
        affectWorldOnly = serializer.i32(affectWorldOnly);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
