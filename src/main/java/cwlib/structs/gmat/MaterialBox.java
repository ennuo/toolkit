package cwlib.structs.gmat;

import org.joml.Vector2f;
import org.joml.Vector4f;

import cwlib.enums.BoxType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MaterialBox implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    /**
     * Number of parameters used before r0x2a4
     */
    public static final int LEGACY_PARAMETER_COUNT = 0x6;

    public static final int PARAMETER_COUNT = 0x8;

    public int type;
    private int[] params = new int[PARAMETER_COUNT];
    public float x, y, w, h;
    public int subType;
    public MaterialParameterAnimation anim = new MaterialParameterAnimation();
    public MaterialParameterAnimation anim2 = new MaterialParameterAnimation();

    /**
     * Creates an output node
     */
    public MaterialBox() {};

    /**
     * Creates a texture sample node
     */
    public MaterialBox(Vector2f scale, Vector2f offset, int channel, int texture) {
        this.type = BoxType.TEXTURE_SAMPLE;
        this.params[0] = Float.floatToIntBits(scale.x);
        this.params[1] = Float.floatToIntBits(scale.y);
        this.params[2] = Float.floatToIntBits(offset.x);
        this.params[3] = Float.floatToIntBits(offset.y);
        this.params[4] = channel;
        this.params[5] = texture;
    }

    /**
     * Creates a texture sample node
     */
    public MaterialBox(Vector4f transform, int channel, int texture) {
        this.type = BoxType.TEXTURE_SAMPLE;
        this.params[0] = Float.floatToIntBits(transform.x);
        this.params[1] = Float.floatToIntBits(transform.y);
        this.params[2] = Float.floatToIntBits(transform.z);
        this.params[3] = Float.floatToIntBits(transform.w);
        this.params[4] = channel;
        this.params[5] = texture;
    }

    /***
     * Creates a color node
     */
    public MaterialBox(Vector4f color) {
        this.type = BoxType.COLOR;
        this.params[0] = Float.floatToIntBits(color.x);
        this.params[1] = Float.floatToIntBits(color.y);
        this.params[2] = Float.floatToIntBits(color.z);
        this.params[3] = Float.floatToIntBits(color.w);
    }

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

    public boolean isColor() { return this.type == BoxType.COLOR; }
    public boolean isTexture() { return this.type == BoxType.TEXTURE_SAMPLE; }
}
