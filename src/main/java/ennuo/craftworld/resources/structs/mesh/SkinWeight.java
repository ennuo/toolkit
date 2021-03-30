package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class SkinWeight {
    public byte boneIndex;
    public byte weight;
    
    public SkinWeight(Data data) {
        data.forward(2);
        weight = data.int8();
        boneIndex = data.int8();
        data.forward(0xC);
    }
    
    public void serialize(Output output) {
        output.bytes(new byte[] { 0x00, 0x00, (byte) Math.round(weight * 255.0f), boneIndex });
        output.pad(0xC);
    }
}
