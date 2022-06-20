package cwlib.structs.gmat;

import org.joml.Vector4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class MaterialParameterAnimation implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;
    public static final int NAME_SIZE = 0x3;

    public Vector4f baseValue;
    public float[] keys;
    private String name = "";
    public byte componentsAnimated;

    @SuppressWarnings("unchecked")
    @Override public MaterialParameterAnimation serialize(Serializer serializer, Serializable structure) {
        MaterialParameterAnimation anim = 
            (structure == null) ? new MaterialParameterAnimation() : (MaterialParameterAnimation) structure;

        anim.baseValue = serializer.v4(anim.baseValue);
        anim.keys = serializer.floatarray(anim.keys);

        // Name should always be 3 characters
        serializer.i32(3);
        anim.name = serializer.str(anim.name, 3);
        
        anim.componentsAnimated = serializer.i8(anim.componentsAnimated);

        return anim;
    }

    @Override public int getAllocatedSize() { 
        int size = MaterialParameterAnimation.BASE_ALLOCATION_SIZE;
        if (this.keys != null)
            size += (this.keys.length * 0x4);
        return size;
    }

    public String getName() { return this.name; }
    public void setName(String name) {
        if (name == null)
            throw new NullPointerException("Name cannot be null!");
        if (name.length() > NAME_SIZE)
            throw new IllegalArgumentException("Name cannot be longer than 3 characters!");
        this.name = name;
    }
}
