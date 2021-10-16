package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import org.joml.Vector4f;

public class ShapeVert {
    public static int MAX_SIZE = 0x25;
    
    public Vector4f localPos, localNormal;
    public int boneIndex;
    
    public ShapeVert(Data data) {
        localPos = data.v4();
        localNormal = data.v4();
        boneIndex = data.i32();
    }
    
    public void serialize(Output output) {
        output.v4(localPos);
        output.v4(localNormal);
        output.i32(boneIndex);
    }
}
