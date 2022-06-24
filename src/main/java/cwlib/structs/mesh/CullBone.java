package cwlib.structs.mesh;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Bones that control the render culling of a model.
 */
public class CullBone implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public Matrix4f invSkinPoseMatrix;
    public Vector4f boundBoxMin;
    public Vector4f boundBoxMax;

    @SuppressWarnings("unchecked")
    @Override public CullBone serialize(Serializer serializer, Serializable structure) {
        CullBone bone = (structure == null) ? new CullBone() : (CullBone) structure;
        
        bone.invSkinPoseMatrix = serializer.m44(bone.invSkinPoseMatrix);
        bone.boundBoxMin = serializer.v4(bone.boundBoxMin);
        bone.boundBoxMax = serializer.v4(bone.boundBoxMax);

        return bone;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
