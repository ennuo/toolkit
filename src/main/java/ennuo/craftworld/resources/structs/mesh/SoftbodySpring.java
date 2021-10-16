package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class SoftbodySpring {
    public short A, B;
    public float restLengthSq;
    
    public SoftbodySpring(Data data) {
        A = data.i16();
        B = data.i16();
        restLengthSq = data.f32();
    }
    
    public static SoftbodySpring[] array(Data data) {
        int count = data.i32();
        SoftbodySpring[] out = new SoftbodySpring[count];
        for (int i = 0; i < count; ++i)
            out[i] = new SoftbodySpring(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.i16(A); output.i16(B);
        output.f32(restLengthSq);
    }
}
