package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class SoftbodyVertEquivalence {
    public short first, count;
    
    public SoftbodyVertEquivalence(Data data) {
        first = data.i16();
        count = data.i16();
    }
    
    public static SoftbodyVertEquivalence[] array(Data data) {
        int count = data.i32();
        SoftbodyVertEquivalence[] out = new SoftbodyVertEquivalence[count];
        for (int i = 0; i < count; ++i)
            out[i] = new SoftbodyVertEquivalence(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.i16(first);
        output.i16(count);
    }
}
