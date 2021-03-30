package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class SoftbodySpring {
    public short A, B;
    public float restLengthSq;
    
    public SoftbodySpring(Data data) {
        A = data.int16();
        B = data.int16();
        restLengthSq = data.float32();
    }
    
    public static SoftbodySpring[] array(Data data) {
        int count = data.int32();
        SoftbodySpring[] out = new SoftbodySpring[count];
        for (int i = 0; i < count; ++i)
            out[i] = new SoftbodySpring(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.int16(A); output.int16(B);
        output.float32(restLengthSq);
    }
}
