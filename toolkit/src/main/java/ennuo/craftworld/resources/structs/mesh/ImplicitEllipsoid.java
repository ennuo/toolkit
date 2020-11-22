package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class ImplicitEllipsoid {
    float[] transform;
    int parentBone, affectWorldOnly;
    
    public ImplicitEllipsoid(Data data) {
        transform = data.matrix();
        parentBone = data.int32();
        affectWorldOnly = data.int32();
    }
    
    public static ImplicitEllipsoid[] array(Data data) {
        int count = data.int32();
        ImplicitEllipsoid[] out = new ImplicitEllipsoid[count];
        for (int i = 0; i < count; ++i)
            out[i] = new ImplicitEllipsoid(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.matrix(transform);
        output.int32(parentBone);
        output.int32(affectWorldOnly);
    }
}
