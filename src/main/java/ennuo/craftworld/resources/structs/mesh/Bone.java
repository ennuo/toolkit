package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.v2.Serializable;
import ennuo.craftworld.serializer.v2.Serializer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class Bone implements Serializable {
    public String name;
    public int flags;
    public long animHash;
    public int parent, firstChild, nextSibling;
    public Matrix4f skinPoseMatrix, invSkinPoseMatrix;
    public Vector4f OBBMin, OBBMax;
    public ShapeVert[] shapeVerts;
    public ShapeInfo[] shapeInfos;
    public float shapeMinZ, shapeMaxZ;
    public Vector4f boundBoxMin, boundBoxMax, boundSphere;

    public Bone serialize(Serializer serializer, Serializable structure) {
        Bone bone = (structure == null) ? new Bone() : (Bone) structure;
        
        bone.name = serializer.str(bone.name, 0x20);
        bone.flags = serializer.i32(bone.flags);
        bone.animHash = serializer.u32(bone.animHash);
        
        bone.parent = (int) serializer.u32d(bone.parent);
        bone.firstChild = (int) serializer.u32d(bone.firstChild);
        bone.nextSibling = (int) serializer.u32d(bone.nextSibling);
        
        bone.skinPoseMatrix = serializer.matrix(bone.skinPoseMatrix);
        bone.invSkinPoseMatrix = serializer.matrix(bone.invSkinPoseMatrix);
        
        bone.OBBMin = serializer.v4(bone.OBBMin);
        bone.OBBMax = serializer.v4(bone.OBBMax);
        
        bone.shapeVerts = serializer.array(bone.shapeVerts, ShapeVert.class);
        bone.shapeInfos = serializer.array(bone.shapeInfos, ShapeInfo.class);
        
        bone.shapeMinZ = serializer.f32(bone.shapeMinZ);
        bone.shapeMaxZ = serializer.f32(bone.shapeMaxZ);
        
        bone.boundBoxMin = serializer.v4(bone.boundBoxMin);
        bone.boundBoxMax = serializer.v4(bone.boundBoxMax);
        bone.boundSphere = serializer.v4(bone.boundSphere);
        
        return bone;
    }
}
