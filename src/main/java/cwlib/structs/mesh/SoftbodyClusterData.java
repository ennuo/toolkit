package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class SoftbodyClusterData implements Serializable {
    public int clusterCount;
    public Vector4f[] restCenterOfMass;
    public Matrix4f[] restDyadicSum;
    public float[] restQuadraticDyadicSum;
    public String[] name;

    public SoftbodyClusterData serialize(Serializer serializer, Serializable structure) {
        SoftbodyClusterData softbody = (structure == null) ? new SoftbodyClusterData() : (SoftbodyClusterData) structure;
        
        softbody.clusterCount = serializer.i32(clusterCount);
        
        // NOTE(Aidan): Vector4, Matrix4f, and String arrays don't serve too much of a purpose to have globally
        // at the moment, so I'm just going to manually make them here.
        
        if (!serializer.isWriting) softbody.restCenterOfMass = new Vector4f[serializer.input.i32()];
        else serializer.output.i32(softbody.restCenterOfMass.length);
        for (int i = 0; i < softbody.restCenterOfMass.length; ++i)
            softbody.restCenterOfMass[i] = serializer.v4(softbody.restCenterOfMass[i]);
        
        if (!serializer.isWriting) softbody.restDyadicSum = new Matrix4f[serializer.input.i32()];
        else serializer.output.i32(softbody.restDyadicSum.length);
        for (int i = 0; i < softbody.restDyadicSum.length; ++i)
            softbody.restDyadicSum[i] = serializer.matrix(softbody.restDyadicSum[i]);
        
        softbody.restQuadraticDyadicSum = serializer.f32a(softbody.restQuadraticDyadicSum);
        
        if (!serializer.isWriting) softbody.name = new String[serializer.input.i32()];
        else serializer.output.i32(softbody.name.length);
        for (int i = 0; i < softbody.name.length; ++i)
            softbody.name[i] = serializer.str(softbody.name[i], 0x20);
        
        return softbody;
    }
}
