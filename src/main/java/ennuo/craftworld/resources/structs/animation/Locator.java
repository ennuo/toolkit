package ennuo.craftworld.resources.structs.animation;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import org.joml.Vector3f;

public class Locator {
    public Vector3f position;
    public String name;
    public byte looping, type;
    
    public Locator(Data data) {
        position = data.v3();
        name = data.str8();
        looping = data.i8();
        type = data.i8();
    }
    
    public static Locator[] array(Data data) {
        int count = data.i32();
        Locator[] out = new Locator[count];
        for (int i = 0; i < count; ++i)
            out[i] = new Locator(data);
        return out;
    }
    
    public void serialize(Output output) {
        output.v3(position);
        output.str8(name);
        output.i8(looping);
        output.i8(type);
    }
    
}
