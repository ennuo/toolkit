package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class Wire {
    int boxFrom, boxTo;
    byte portFrom, portTo;
    
    public Wire(Data data) {
        boxFrom = data.int32(); boxTo = data.int32();
        portFrom = data.int8(); portTo = data.int8();
    }
    
    public static Wire[] array(Data data) {
        int count = data.int32();
        Wire[] out = new Wire[count];
        for (int i = 0; i < count; ++i)
            out[i] = new Wire(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.int32(boxFrom); output.int32(boxTo);
        output.int8(portFrom); output.int8(portTo);
    }
}
