package cwlib.structs.gmat;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MaterialBox implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    /**
     * Number of parameters used before r0x2a4
     */
    public static final int LEGACY_PARAMETER_COUNT = 0x6;

    public static final int PARAMETER_COUNT = 0x8;

    public static int lastUID = 0;

    public transient int id = ++lastUID;
    public transient int[] inputs = new int[] { 
        ++lastUID, ++lastUID, ++lastUID, ++lastUID,
        ++lastUID, ++lastUID, ++lastUID, ++lastUID,
        ++lastUID, ++lastUID, ++lastUID, ++lastUID,
        ++lastUID, ++lastUID, ++lastUID, ++lastUID 
    };

    public transient int[] outputs = new int[] { 
        ++lastUID, ++lastUID, ++lastUID, ++lastUID 
    };

    public int type;
    private int[] params = new int[PARAMETER_COUNT];
    public float x, y, w, h;
    public int subType;
    public MaterialParameterAnimation anim = new MaterialParameterAnimation();
    public MaterialParameterAnimation anim2 = new MaterialParameterAnimation();

    @SuppressWarnings("unchecked")
    @Override public MaterialBox serialize(Serializer serializer, Serializable structure) {
        MaterialBox box = 
            (structure == null) ? new MaterialBox() : (MaterialBox) structure;

        box.type = serializer.i32(box.type);

        int head = serializer.getRevision().getVersion();

        if (!serializer.isWriting()) box.params = new int[PARAMETER_COUNT];
        if (head < 0x2a4) {
            for (int i = 0; i < LEGACY_PARAMETER_COUNT; ++i)
                box.params[i] = serializer.i32(box.params[i]);
        } else {
            serializer.i32(PARAMETER_COUNT);
            for (int i = 0; i < PARAMETER_COUNT; ++i)
                box.params[i] = serializer.i32(box.params[i]);
        }

        box.x = serializer.f32(box.x);
        box.y = serializer.f32(box.y);
        box.w = serializer.f32(box.w);
        box.h = serializer.f32(box.h);

        if (head > 0x2a3) 
            box.subType = serializer.i32(box.subType);
        
        if (head > 0x2a1)
            box.anim = serializer.struct(box.anim, MaterialParameterAnimation.class);
        if (head > 0x2a3)
            box.anim2 = serializer.struct(box.anim2, MaterialParameterAnimation.class);
        
        return box;
    }

    @Override public int getAllocatedSize() { 
        int size = MaterialBox.BASE_ALLOCATION_SIZE;
        size += this.anim.getAllocatedSize();
        size += this.anim2.getAllocatedSize();
        return size;
    }

    public int[] getParameters() { return this.params; }
}
