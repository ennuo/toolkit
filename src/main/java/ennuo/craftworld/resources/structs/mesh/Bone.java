package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.resources.structs.ShapeInfo;
import ennuo.craftworld.resources.structs.ShapeVert;
import org.joml.Vector4f;

public class Bone {
    public String name;
    public int flags;
    public long animHash;
    public int parent, firstChild, nextSibling;
    public float[] skinPoseMatrix, invSkinPoseMatrix;
    public Vector4f OBBMin, OBBMax;
    public ShapeVert[] shapeVerts;
    public ShapeInfo[] shapeInfos;
    public float shapeMinZ, shapeMaxZ;
    public Vector4f boundBoxMin, boundBoxMax, boundSphere;
    
    public short mirror;
    public byte mirrorType;
    
    public Bone(Data data) {
        name = data.str(0x20);
        flags = data.i32();
        animHash = data.u32();
        parent = data.i32();
        if (data.revision > 0x271)
            parent /= 2;
        firstChild = data.i32();
        nextSibling = data.i32();
        skinPoseMatrix = data.matrix();
        invSkinPoseMatrix = data.matrix();
        OBBMin = data.v4(); OBBMax = data.v4();
        
        int shapeVertCount = data.i32();
        shapeVerts = new ShapeVert[shapeVertCount];
        for (int i = 0; i < shapeVertCount; ++i)
            shapeVerts[i] = new ShapeVert(data);
                
        int shapeInfoCount = data.i32();
        shapeInfos = new ShapeInfo[shapeInfoCount];
        for (int i = 0; i < shapeInfoCount; ++i)
            shapeInfos[i] = new ShapeInfo(data);
        
        shapeMinZ = data.f32(); shapeMaxZ = data.f32();
        
        boundBoxMin = data.v4(); boundBoxMax = data.v4();
        boundSphere = data.v4();
    }
    
    public static Bone[] array(Data data) {
        int count = data.i32();
        Bone[] out = new Bone[count];
        for (int i = 0; i < count; ++i)
            out[i] = new Bone(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.str(name, 0x20);
        output.i32(flags);
        output.u32(animHash);
        output.i32(parent);
        output.i32(firstChild);
        output.i32(nextSibling);
        output.matrix(skinPoseMatrix);
        output.matrix(invSkinPoseMatrix);
        output.v4(OBBMin); output.v4(OBBMax);
        
        if (shapeVerts != null) {
            output.i32(shapeVerts.length);
            for (ShapeVert vert : shapeVerts)
                vert.serialize(output);
        }
        
        if (shapeInfos != null) {
            output.i32(shapeInfos.length);
            for (ShapeInfo info : shapeInfos)
                info.serialize(output);
        }
        
        output.f32(shapeMinZ); output.f32(shapeMaxZ);
        output.v4(boundBoxMin); output.v4(boundBoxMax);
        output.v4(boundSphere);
    }
}
