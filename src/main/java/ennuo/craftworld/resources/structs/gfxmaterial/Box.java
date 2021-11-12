package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class Box implements Serializable {
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
    
    public Box serialize(Serializer serializer, Serializable structure) {
        Box box = null;
        if (structure != null) box = (Box) structure;
        else box = new Box();
        
        box.type = serializer.i32(box.type);
        
        if (serializer.revision.head >= 0x2b2)
            box.params = serializer.u32a(box.params);
        else {
            if (!serializer.isWriting) box.params = new long[6];
            for (int i = 0; i < 6; ++i)
                box.params[i] = serializer.u32(box.params[i]);
        }
        
        box.x = serializer.f32(box.x);
        box.y = serializer.f32(box.y);
        box.z = serializer.f32(box.z);
        box.w = serializer.f32(box.w);
        
        if (serializer.revision.head >= 0x2a2) {
            box.subType = serializer.i32(box.subType);
            box.anim = serializer.struct(box.anim, ParameterAnimation.class);
            box.anim2 = serializer.struct(box.anim2, ParameterAnimation.class);
        }
        
        return box;
    }
}
