package cwlib.structs.animation;

import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import org.joml.Vector3f;

public class Locator {
    public Vector3f position;
    public String name;
    public byte looping, type;
    
    public Locator(MemoryInputStream data) {
        position = data.v3();
        name = data.str();
        looping = data.i8();
        type = data.i8();
    }
    
    public static Locator[] array(MemoryInputStream data) {
        int count = data.i32();
        Locator[] out = new Locator[count];
        for (int i = 0; i < count; ++i)
            out[i] = new Locator(data);
        return out;
    }
    
    public void serialize(MemoryOutputStream output) {
        output.v3(position);
        output.str(name);
        output.i8(looping);
        output.i8(type);
    }
    
}
