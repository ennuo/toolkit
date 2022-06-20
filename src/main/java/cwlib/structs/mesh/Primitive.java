package cwlib.structs.mesh;

import cwlib.types.data.ResourceDescriptor;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Primitive implements Serializable {
    public static int MAX_SIZE = 0x4B;
    
    public ResourceDescriptor material;
    public ResourceDescriptor textureAlternatives;
    public int minVert;
    public int maxVert;
    public int firstIndex;
    public int numIndices;
    public int region;

    public Primitive serialize(Serializer serializer, Serializable structure) {
        Primitive primitive = (structure == null) ? new Primitive() : (Primitive) structure;
        
        primitive.material = serializer.resource(primitive.material, ResourceType.GFX_MATERIAL);
        primitive.textureAlternatives = serializer.resource(primitive.textureAlternatives, ResourceType.TEXTURE_LIST);
        primitive.minVert = serializer.i32(primitive.minVert);
        primitive.maxVert = serializer.i32(primitive.maxVert);
        primitive.firstIndex = serializer.i32(primitive.firstIndex);
        primitive.numIndices = serializer.i32(primitive.numIndices);
        primitive.region = serializer.i32(primitive.region);
        
        return primitive;
    }
}
