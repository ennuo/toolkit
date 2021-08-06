package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import org.joml.Vector4f;

public class SoftbodyCluster {
    public int clusterCount = 0;
    public Vector4f[] restCenterOfMass = new Vector4f[0];
    public float[][] restDyadicSum = new float[0][];
    public float[] restQuadraticDyadicSum = new float[0];
    public String[] name = new String[0];
    
    public SoftbodyCluster() {};
    public SoftbodyCluster(Data data) {
        clusterCount = data.int32();
        restCenterOfMass = new Vector4f[data.int32()];
        for (int i = 0; i < restCenterOfMass.length; ++i)
            restCenterOfMass[i] = data.v4();
        restDyadicSum = new float[data.int32()][];
        for (int i = 0; i < restDyadicSum.length; ++i)
            restDyadicSum[i] = data.matrix();
        restQuadraticDyadicSum = data.float32arr();
        name = new String[data.int32()];
        for (int i = 0; i < name.length; ++i)
            name[i] = data.str(0x20);
    }
    
    public void serialize(Output output) {
        output.int32(clusterCount);
        output.int32(restCenterOfMass.length);
        for (int i = 0; i < restCenterOfMass.length; ++i)
            output.v4(restCenterOfMass[i]);
        output.int32(restDyadicSum.length);
        for (int i = 0; i < restDyadicSum.length; ++i)
            output.matrix(restDyadicSum[i]);
        output.float32arr(restQuadraticDyadicSum);
        output.int32(name.length);
        for (int i = 0; i < name.length; ++i)
           output.string(name[i], 0x20);
    }
}
