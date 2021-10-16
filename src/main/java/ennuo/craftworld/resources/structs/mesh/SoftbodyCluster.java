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
        clusterCount = data.i32();
        restCenterOfMass = new Vector4f[data.i32()];
        for (int i = 0; i < restCenterOfMass.length; ++i)
            restCenterOfMass[i] = data.v4();
        restDyadicSum = new float[data.i32()][];
        for (int i = 0; i < restDyadicSum.length; ++i)
            restDyadicSum[i] = data.matrix();
        restQuadraticDyadicSum = data.f32a();
        name = new String[data.i32()];
        for (int i = 0; i < name.length; ++i)
            name[i] = data.str(0x20);
    }
    
    public void serialize(Output output) {
        output.i32(clusterCount);
        output.i32(restCenterOfMass.length);
        for (int i = 0; i < restCenterOfMass.length; ++i)
            output.v4(restCenterOfMass[i]);
        output.i32(restDyadicSum.length);
        for (int i = 0; i < restDyadicSum.length; ++i)
            output.matrix(restDyadicSum[i]);
        output.f32a(restQuadraticDyadicSum);
        output.i32(name.length);
        for (int i = 0; i < name.length; ++i)
           output.str(name[i], 0x20);
    }
}
