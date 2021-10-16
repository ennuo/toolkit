package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class Box {
    
    public final class BoxType {
        public static final int OUTPUT = 0;
        public static final int TEXTURE_SAMPLE = 1;
        public static final int COLOR = 3;
        public static final int MULTIPLY = 10;
        public static final int SUBTRACT = 12;
    }
    
    public int type;
    public long[] params;
    public float x, y, z, w;
    public int subType;
    
    public ParameterAnimation anim;
    public ParameterAnimation anim2;
    
    public Box(Data data) {
       type = data.i32();
       if (data.revision >= 0x2b2)
           params = new long[data.i32()];
       else params = new long[6];
       for (int i = 0; i < params.length; ++i)
           params[i] = data.u32();
       x = data.f32(); y = data.f32();
       z = data.f32(); w = data.f32();
       if (data.revision >= 0x2a2) {
          subType = data.i32();
          anim = new ParameterAnimation(data);
          anim2 = new ParameterAnimation(data);
       }
    }
    
    public static Box[] array(Data data) {
        int count = data.i32();
        Box[] out = new Box[count];
        for (int i = 0; i < count; ++i)
            out[i] = new Box(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.i32(type);
        if (output.revision >= 0x2b2)
            output.i32(params.length);
        for (int i = 0; i < params.length; ++i)
            output.u32(params[i]);
        output.f32(x); output.f32(y);
        output.f32(z); output.f32(w);
        if (output.revision >= 0x2a2) {
            output.i32(subType);
            anim.serialize(output);
            anim2.serialize(output);
        }
    }
}
