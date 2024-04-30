package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.bevel.BevelVertex;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

/**
 * Resource that stores bevel data for
 * generated meshes in-game.
 */
public class RBevel implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0xD0;
    public static final int MAX_MATERIALS = 4;

    private ResourceDescriptor[] materials = new ResourceDescriptor[MAX_MATERIALS];
    private float[] UVScales = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    public float autoSmoothCutoffAngle = 120.0f;
    public ArrayList<BevelVertex> vertices = new ArrayList<>();
    public boolean includeBackface;
    public boolean smoothWithFront = true;
    public float relaxStrength;
    public float subDivRadius = 0.3f;
    public float fixedBevelSize = 10.0f;
    public boolean spongy;
    public ResourceDescriptor softPhysicsSettings;
    public float textureRepeats = 1.0f;

    @Override
    public void serialize(Serializer serializer)
    {

        serializer.i32(MAX_MATERIALS); // This should always be 4.
        if (materials == null)
            materials = new ResourceDescriptor[MAX_MATERIALS];
        for (int i = 0; i < MAX_MATERIALS; ++i)
            materials[i] = serializer.resource(materials[i], ResourceType.GFX_MATERIAL);

        serializer.i32(MAX_MATERIALS); // This should always be 4.
        if (UVScales == null)
            UVScales = new float[MAX_MATERIALS];
        for (int i = 0; i < MAX_MATERIALS; ++i)
            UVScales[i] = serializer.f32(UVScales[i]);

        autoSmoothCutoffAngle = serializer.f32(autoSmoothCutoffAngle);

        vertices = serializer.arraylist(vertices, BevelVertex.class);

        includeBackface = serializer.bool(includeBackface);
        smoothWithFront = serializer.bool(smoothWithFront);
        relaxStrength = serializer.f32(relaxStrength);
        subDivRadius = serializer.f32(subDivRadius);
        fixedBevelSize = serializer.f32(fixedBevelSize);
        spongy = serializer.bool(spongy);

        softPhysicsSettings = serializer.resource(softPhysicsSettings,
            ResourceType.SETTINGS_SOFT_PHYS);

        textureRepeats = serializer.f32(textureRepeats);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = RBevel.BASE_ALLOCATION_SIZE;
        if (this.vertices != null)
            size += (this.vertices.size() * BevelVertex.BASE_ALLOCATION_SIZE);
        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
        serializer.struct(this, RBevel.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.BEVEL,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }

    /**
     * Returns the material at specified slot index.
     *
     * @param index Slot index
     * @return Material in slot
     */
    public ResourceDescriptor getMaterial(int index)
    {
        return this.materials[index];
    }

    /**
     * Sets a material slot.
     *
     * @param material Material resource descriptor to insert
     * @param index    Slot index
     */
    public void setMaterial(ResourceDescriptor material, int index)
    {
        this.materials[index] = material;
    }

    /**
     * Returns the UV scale of the material at specified slot index.
     *
     * @param index Slot index
     * @return UV scale of material in slot
     */
    public float getMaterialUVScale(int index)
    {
        return this.UVScales[index];
    }

    /**
     * Sets the UV scale of the material at specified slot index.
     *
     * @param uvScale UV scale of the material
     * @param index   Slot index
     */
    public void setMaterialUVScale(float uvScale, int index)
    {
        this.UVScales[index] = uvScale;
    }

    @Override
    public String toString()
    {
        return String.format("RBevel{%s}", this.vertices.toString());
    }
}
