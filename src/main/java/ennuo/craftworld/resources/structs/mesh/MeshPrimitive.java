package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.serializer.v2.Serializable;
import ennuo.craftworld.serializer.v2.Serializer;

public class MeshPrimitive implements Serializable {
    public static int MAX_SIZE = 0x4B;
    
    public ResourcePtr material;
    public ResourcePtr textureAlternatives;
    public int minVert;
    public int maxVert;
    public int firstIndex;
    public int numIndices;
    public int region;

    public MeshPrimitive serialize(Serializer serializer, Serializable structure) {
        MeshPrimitive primitive = (structure == null) ? new MeshPrimitive() : (MeshPrimitive) structure;
        
        primitive.material = serializer.resource(primitive.material, RType.GFXMATERIAL);
        primitive.textureAlternatives = serializer.resource(primitive.textureAlternatives, RType.TEXTURE_LIST);
        primitive.minVert = serializer.i32(primitive.minVert);
        primitive.maxVert = serializer.i32(primitive.maxVert);
        primitive.firstIndex = serializer.i32(primitive.firstIndex);
        primitive.numIndices = serializer.i32(primitive.numIndices);
        primitive.region = serializer.i32(primitive.region);
        
        return primitive;
    }
}
