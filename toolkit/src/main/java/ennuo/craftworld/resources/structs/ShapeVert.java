package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Vector4f;

public class ShapeVert {
    public static int MAX_SIZE = 0x25;
    
    Vector4f localPos, localNormal;
    int boneIndex;
    
    public ShapeVert(Data data) {
        localPos = data.v4();
        localNormal = data.v4();
        boneIndex = data.int32();
    }
    
    public void serialize(Output output) {
        output.v4(localPos);
        output.v4(localNormal);
        output.int32(boneIndex);
    }
}
