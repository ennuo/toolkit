package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class SoftbodyVertEquivalence {
    short first, count;
    
    public SoftbodyVertEquivalence(Data data) {
        first = data.int16();
        count = data.int16();
    }
    
    public static SoftbodyVertEquivalence[] array(Data data) {
        int count = data.int32();
        SoftbodyVertEquivalence[] out = new SoftbodyVertEquivalence[count];
        for (int i = 0; i < count; ++i)
            out[i] = new SoftbodyVertEquivalence(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.int16(first);
        output.int16(count);
    }
}
