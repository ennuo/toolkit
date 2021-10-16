package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;

public class MeshPrimitive {
    public static int MAX_SIZE = 0x4B;
    
    public ResourcePtr material;
    public ResourcePtr textureAlternatives;
    public int minVert;
    public int maxVert;
    public int firstIndex;
    public int numIndices;
    public int region;
    
    public MeshPrimitive(Data data) {
        boolean isBit = data.revision < 0x230;
        material = data.resource(RType.GFXMATERIAL, isBit);
        textureAlternatives = data.resource(RType.TEXTURE_LIST, isBit);
        minVert = data.i32();
        maxVert = data.i32();
        firstIndex = data.i32();
        numIndices = data.i32();
        region = data.i32();
    }
    
    public static MeshPrimitive[] array(Data data) {
        int count = data.i32();
        MeshPrimitive[] out = new MeshPrimitive[count];
        for (int i = 0; i < count; ++i)
            out[i] = new MeshPrimitive(data);
        return out;
    }
    
    public void serialize(Output output) {
        boolean isBit = output.revision < 0x230;
        output.resource(material, isBit);
        output.resource(textureAlternatives, isBit);
        output.i32(minVert); output.i32(maxVert);
        output.i32(firstIndex); output.i32(numIndices);
        output.i32(region);
    }
}
