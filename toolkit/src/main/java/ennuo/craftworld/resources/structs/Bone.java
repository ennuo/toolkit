package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.structs.ShapeInfo;
import ennuo.craftworld.resources.structs.ShapeVert;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Vector4f;

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
}
