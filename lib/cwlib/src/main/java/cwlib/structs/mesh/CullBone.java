package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Bones that control the render culling of a model.
 */
public class CullBone implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public Matrix4f invSkinPoseMatrix;
    public Vector4f boundBoxMin;
    public Vector4f boundBoxMax;

    @Override
    public void serialize(Serializer serializer)
    {
        invSkinPoseMatrix = serializer.m44(invSkinPoseMatrix);
        boundBoxMin = serializer.v4(boundBoxMin);
        boundBoxMax = serializer.v4(boundBoxMax);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
