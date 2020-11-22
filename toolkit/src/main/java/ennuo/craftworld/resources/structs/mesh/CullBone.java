package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Vector4f;

public class CullBone {
    public float[] invSkinPoseMatrix;
    public Vector4f boundBoxMin, boundBoxMax;
    
    public CullBone(Data data) {
        invSkinPoseMatrix = data.matrix();
        boundBoxMin = data.v4(); boundBoxMax = data.v4();
    }
    
    public static CullBone[] array(Data data) {
        int count = data.int32();
        CullBone[] out = new CullBone[count];
        for (int i = 0; i < count; ++i)
            out[i] = new CullBone(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.matrix(invSkinPoseMatrix);
        output.v4(boundBoxMin); output.v4(boundBoxMax);
    }
}
