package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import org.joml.Vector4f;

public class ParameterAnimation {
    public Vector4f baseValue;
    public float[] keys;
    public byte[] name;
    public byte componentsAnimated;
    
    public ParameterAnimation(Data data) {
        baseValue = data.v4();
        keys = new float[data.i32()];
        for (int i = 0; i < keys.length; ++i)
            keys[i] = data.f32();
        name = new byte[data.i32()];
        for (int i = 0; i < name.length; ++i)
            name[i] = data.i8();
        componentsAnimated = data.i8();
    }
    
    public static ParameterAnimation[] array(Data data) {
        int count = data.i32();
        ParameterAnimation[] out = new ParameterAnimation[count];
        for (int i = 0; i < count; ++i)
            out[i] = new ParameterAnimation(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.v4(baseValue);
        output.i32(keys.length);
        for (float key : keys) output.f32(key);
        output.i32(name.length);
        for (byte name : name)
            output.i8(name);
        output.i8(componentsAnimated);
    }
}
