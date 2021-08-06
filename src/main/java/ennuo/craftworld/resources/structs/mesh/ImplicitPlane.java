package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import org.joml.Vector4f;

public class ImplicitPlane {
    public Vector4f planeNormal, pointInPlane;
    public int parentBone;
    
    public ImplicitPlane(Data data) {
        planeNormal = data.v4();
        pointInPlane = data.v4();
        parentBone = data.int32();
    }
    
    public static ImplicitPlane[] array(Data data) {
        int count = data.int32();
        ImplicitPlane[] out = new ImplicitPlane[count];
        for (int i = 0; i < count; ++i)
            out[i] = new ImplicitPlane(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.v4(planeNormal);
        output.v4(pointInPlane);
        output.int32(parentBone);
    }
}
