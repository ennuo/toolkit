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
       type = data.int32();
       if (data.revision >= 0x2b2)
           params = new long[data.int32()];
       else params = new long[6];
       for (int i = 0; i < params.length; ++i)
           params[i] = data.uint32();
       x = data.float32(); y = data.float32();
       z = data.float32(); w = data.float32();
       if (data.revision >= 0x2a2) {
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
        if (output.revision >= 0x2b2)
            output.int32(params.length);
        for (int i = 0; i < params.length; ++i)
            output.uint32(params[i]);
        output.float32(x); output.float32(y);
        output.float32(z); output.float32(w);
        if (output.revision >= 0x2a2) {
            output.int32(subType);
            anim.serialize(output);
            anim2.serialize(output);
        }
    }
}
