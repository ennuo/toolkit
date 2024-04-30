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
public class Primitive implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    /**
     * RGfxMaterial used to texture this primitive.
     */
    public ResourceDescriptor material;

    /**
     * Presumably for mapping alternative textures to
     * the material, however functionality appears to be scrapped.
     */
    public ResourceDescriptor textureAlternatives;

    /**
     * Minimum vertex painted by this primitive.
     */
    public int minVert;

    /**
     * Maximum vertex painted by this primitive.
     */
    public int maxVert;

    /**
     * What index is first painted by this primitive.
     */
    public int firstIndex;

    /**
     * Number of indices painted with this material
     * after the first index.
     */
    public int numIndices;

    /**
     * The region ID of this mesh, it essentially defines a "submesh",
     * that can be hidden by costume pieces.
     */
    public int region;

    public Primitive() { }

    public Primitive(int minVert, int maxVert, int firstIndex, int numIndices)
    {
        this.minVert = minVert;
        this.maxVert = maxVert;
        this.firstIndex = firstIndex;
        this.numIndices = numIndices;
    }

    public Primitive(ResourceDescriptor material, int minVert, int maxVert, int firstIndex,
                     int numIndices)
    {
        this(minVert, maxVert, firstIndex, numIndices);
        this.material = material;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        material = serializer.resource(getMaterial(),
            ResourceType.GFX_MATERIAL);

        if (version < 0x149)
            serializer.resource(null, ResourceType.GFX_MATERIAL);

        if (version >= Revisions.MESH_TEXTURE_ALTERNATIVES)
            textureAlternatives = serializer.resource(textureAlternatives,
                ResourceType.TEXTURE_LIST);

        minVert = serializer.i32(minVert);
        maxVert = serializer.i32(maxVert);
        firstIndex = serializer.i32(firstIndex);
        numIndices = serializer.i32(numIndices);
        region = serializer.i32(region);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }

    public ResourceDescriptor getMaterial()
    {
        return this.material;
    }

    public int getMinVert()
    {
        return this.minVert;
    }

    public int getMaxVert()
    {
        return this.maxVert;
    }

    public int getFirstIndex()
    {
        return this.firstIndex;
    }

    public int getNumIndices()
    {
        return this.numIndices;
    }

    public int getRegion()
    {
        return this.region;
    }

    /**
     * In PS4 versions of models, the min/max vertices are all over the place,
     * as I'm not sure what's the reason for it, the MeshExporter has to fix up primitives.
     *
     * @param minVert Fixed up minimum vertex in primitive
     * @param maxVert Fixed up maximum vertex in primitive
     */
    public void setMinMax(int minVert, int maxVert)
    {
        this.minVert = minVert;
        this.maxVert = maxVert;
    }

    public void setMaterial(ResourceDescriptor material)
    {
        this.material = material;
    }

    public void setRegion(int region)
    {
        this.region = region;
    }
}
