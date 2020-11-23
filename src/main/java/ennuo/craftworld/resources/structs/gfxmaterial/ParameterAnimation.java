package ennuo.craftworld.resources.structs.gfxmaterial;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Vector4f;

public class ParameterAnimation {
    Vector4f baseValue;
    float[] keys;
    byte[] name;
    byte componentsAnimated;
    
    public ParameterAnimation(Data data) {
        baseValue = data.v4();
        keys = new float[data.int32()];
        for (int i = 0; i < keys.length; ++i)
            keys[i] = data.float32();
        name = new byte[data.int32()];
        for (int i = 0; i < name.length; ++i)
            name[i] = data.int8();
        componentsAnimated = data.int8();
    }
    
    public static ParameterAnimation[] array(Data data) {
        int count = data.int32();
        ParameterAnimation[] out = new ParameterAnimation[count];
        for (int i = 0; i < count; ++i)
            out[i] = new ParameterAnimation(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.v4(baseValue);
        output.int32(keys.length);
        for (float key : keys) output.float32(key);
        output.int32(name.length);
        for (byte name : name)
            output.int8(name);
        output.int8(componentsAnimated);
    }
}
