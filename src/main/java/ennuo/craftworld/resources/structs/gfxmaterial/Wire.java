package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class Wire {
    public int boxFrom, boxTo;
    public byte portFrom, portTo;
    
    public Wire(Data data) {
        boxFrom = data.i32(); boxTo = data.i32();
        portFrom = data.i8(); portTo = data.i8();
        
        if (data.isEncoded()) boxFrom /= 2;
        if (data.isEncoded()) boxTo /= 2;
        
        data.bytes(5); // Padding bytes?
    }
    
    public static Wire[] array(Data data) {
        int count = data.i32();
        Wire[] out = new Wire[count];
        for (int i = 0; i < count; ++i)
            out[i] = new Wire(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.i32(boxFrom); output.i32(boxTo);
        output.i8(portFrom); output.i8(portTo);
        output.pad(5);
    }
}
