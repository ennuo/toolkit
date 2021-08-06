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
        flags = data.int32();
        animHash = data.uint32();
        parent = data.int32();
        if (data.revision > 0x271)
            parent /= 2;
        firstChild = data.int32();
        nextSibling = data.int32();
        skinPoseMatrix = data.matrix();
        invSkinPoseMatrix = data.matrix();
        OBBMin = data.v4(); OBBMax = data.v4();
        
        int shapeVertCount = data.int32();
        shapeVerts = new ShapeVert[shapeVertCount];
        for (int i = 0; i < shapeVertCount; ++i)
            shapeVerts[i] = new ShapeVert(data);
                
        int shapeInfoCount = data.int32();
        shapeInfos = new ShapeInfo[shapeInfoCount];
        for (int i = 0; i < shapeInfoCount; ++i)
            shapeInfos[i] = new ShapeInfo(data);
        
        shapeMinZ = data.float32(); shapeMaxZ = data.float32();
        
        boundBoxMin = data.v4(); boundBoxMax = data.v4();
        boundSphere = data.v4();
    }
    
    public static Bone[] array(Data data) {
        int count = data.int32();
        Bone[] out = new Bone[count];
        for (int i = 0; i < count; ++i)
            out[i] = new Bone(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.string(name, 0x20);
        output.int32(flags);
        output.uint32(animHash);
        output.int32(parent);
        output.int32(firstChild);
        output.int32(nextSibling);
        output.matrix(skinPoseMatrix);
        output.matrix(invSkinPoseMatrix);
        output.v4(OBBMin); output.v4(OBBMax);
        
        if (shapeVerts != null) {
            output.int32(shapeVerts.length);
            for (ShapeVert vert : shapeVerts)
                vert.serialize(output);
        }
        
        if (shapeInfos != null) {
            output.int32(shapeInfos.length);
            for (ShapeInfo info : shapeInfos)
                info.serialize(output);
        }
        
        output.float32(shapeMinZ); output.float32(shapeMaxZ);
        output.v4(boundBoxMin); output.v4(boundBoxMax);
        output.v4(boundSphere);
    }
}
