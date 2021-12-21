package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class CullBone implements Serializable {
    public Matrix4f invSkinPoseMatrix;
    public Vector4f boundBoxMin, boundBoxMax;
    
    public CullBone serialize(Serializer serializer, Serializable structure) {
        CullBone bone = (structure == null) ? new CullBone() : (CullBone) structure;
        
        bone.invSkinPoseMatrix = serializer.matrix(bone.invSkinPoseMatrix);
        bone.boundBoxMin = serializer.v4(bone.boundBoxMin);
        bone.boundBoxMax = serializer.v4(bone.boundBoxMax);
        
        return bone;
    }
}
