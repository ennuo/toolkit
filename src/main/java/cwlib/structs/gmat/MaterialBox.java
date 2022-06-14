package cwlib.structs.gmat;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MaterialBox implements Serializable {
    public final class BoxType {
        public static final int OUTPUT = 0;
        public static final int TEXTURE_SAMPLE = 1;
        public static final int COLOR = 3;
        public static final int MULTIPLY = 10;
        public static final int SUBTRACT = 12;
    }
    
    public int type;
    public long[] params;
    public float x, y, w, h;
    public int subType;
    
    public MaterialParameterAnimation anim;
    public MaterialParameterAnimation anim2;
    
    public MaterialBox serialize(Serializer serializer, Serializable structure) {
        MaterialBox box = null;
        if (structure != null) box = (MaterialBox) structure;
        else box = new MaterialBox();
        
        box.type = serializer.i32(box.type);
        
        if (serializer.revision.head < 0x2a4) {
            if (!serializer.isWriting) box.params = new long[6];
            for (int i = 0; i < 6; ++i)
                box.params[i] = serializer.u32(box.params[i]);
        } else box.params = serializer.u32a(box.params);

        box.x = serializer.f32(box.x);
        box.y = serializer.f32(box.y);
        box.w = serializer.f32(box.w);
        box.h = serializer.f32(box.h);
        
        if (serializer.revision.head > 0x2a3)
            box.subType = serializer.i32(box.subType);
        
        if (serializer.revision.head > 0x2a1)
            box.anim = serializer.struct(box.anim, MaterialParameterAnimation.class);
        
        if (serializer.revision.head > 0x2a3)
            box.anim2 = serializer.struct(box.anim2, MaterialParameterAnimation.class);
        
        return box;
    }
}
