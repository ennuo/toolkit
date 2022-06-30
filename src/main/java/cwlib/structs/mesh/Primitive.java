package cwlib.structs.mesh;

import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

/**
 * Building blocks of a mesh that controls
 * how it gets rendered.
 */
public class Primitive implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    /**
     * RGfxMaterial used to texture this primitive.
     */
    private ResourceDescriptor material;

    /**
     * Presumably for mapping alternative textures to
     * the material, however functionality appears to be scrapped.
     */
    private ResourceDescriptor textureAlternatives;

    /**
     * Minimum vertex painted by this primitive.
     */
    private int minVert;

    /**
     * Maximum vertex painted by this primitive.
     */
    private int maxVert;
    
    /**
     * What index is first painted by this primitive.
     */
    private int firstIndex;

    /**
     * Number of indices painted with this material
     * after the first index.
     */
    private int numIndices;

    /**
     * The region ID of this mesh, it essentially defines a "submesh",
     * that can be hidden by costume pieces.
     */
    private int region;

    public Primitive() {};
    public Primitive(int minVert, int maxVert, int firstIndex, int numIndices) {
        this.minVert = minVert;
        this.maxVert = maxVert;
        this.firstIndex = firstIndex;
        this.numIndices = numIndices;
    }

    public Primitive(ResourceDescriptor material, int minVert, int maxVert, int firstIndex, int numIndices) {
        this(minVert, maxVert, firstIndex, numIndices);
        this.material = material;
    }

    @SuppressWarnings("unchecked")
    @Override public Primitive serialize(Serializer serializer, Serializable structure) {
        Primitive primitive = (structure == null) ? new Primitive() : (Primitive) structure;
        
        int version = serializer.getRevision().getVersion();

        primitive.material = serializer.resource(primitive.getMaterial(), ResourceType.GFX_MATERIAL);

        if (version < 0x149)
            serializer.resource(null, ResourceType.GFX_MATERIAL);
        
        if (version >= Revisions.MESH_TEXTURE_ALTERNATIVES)
            primitive.textureAlternatives = serializer.resource(primitive.textureAlternatives, ResourceType.TEXTURE_LIST);
        
        primitive.minVert = serializer.i32(primitive.getMinVert());
        primitive.maxVert = serializer.i32(primitive.getMaxVert());
        primitive.firstIndex = serializer.i32(primitive.firstIndex);
        primitive.numIndices = serializer.i32(primitive.numIndices);
        primitive.region = serializer.i32(primitive.region);
        
        return primitive;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }

    public ResourceDescriptor getMaterial() { return this.material; }
    public int getMinVert() { return this.minVert; }
    public int getMaxVert() { return this.maxVert; }
    public int getFirstIndex() { return this.firstIndex; }
    public int getNumIndices() { return this.numIndices; }
    public int getRegion() { return this.region; }

    public void setMaterial(ResourceDescriptor material) { this.material = material; }
    public void setRegion(int region) { this.region = region; }
}
