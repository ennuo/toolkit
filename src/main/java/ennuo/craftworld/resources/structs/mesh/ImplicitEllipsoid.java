package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class ImplicitEllipsoid {
    public float[] transform;
    public int parentBone, affectWorldOnly;
    
    public ImplicitEllipsoid(Data data) {
        transform = data.matrix();
        parentBone = data.i32();
        affectWorldOnly = data.i32();
    }
    
    public static ImplicitEllipsoid[] array(Data data) {
        int count = data.i32();
        ImplicitEllipsoid[] out = new ImplicitEllipsoid[count];
        for (int i = 0; i < count; ++i)
            out[i] = new ImplicitEllipsoid(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.matrix(transform);
        output.i32(parentBone);
        output.i32(affectWorldOnly);
    }
}
