package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class Box {
    int type;
    int[] params;
    float x, y, z, w;
    int subType;
    
    ParameterAnimation anim;
    ParameterAnimation anim2;
    
    public Box(Data data) {
       type = data.int32();
       params = new int[data.int32()];
       for (int i = 0; i < params.length; ++i)
           params[i] = data.int32();
       x = data.float32(); y = data.float32();
       z = data.float32(); w = data.float32();
       if (data.revision > 0x272) {
          subType = data.int32();
          anim = new ParameterAnimation(data);
          anim2 = new ParameterAnimation(data);
       }
    }
    
    public static Box[] array(Data data) {
        int count = data.int32();
        Box[] out = new Box[count];
        for (int i = 0; i < count; ++i)
            out[i] = new Box(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.int32(type);
        output.int32(params.length);
        for (int i = 0; i < params.length; ++i)
            output.int32(params[i]);
        output.float32(x); output.float32(y);
        output.float32(z); output.float32(w);
        if (output.revision > 0x272) {
            output.int32(subType);
            anim.serialize(output);
            anim2.serialize(output);
        }
    }
}
