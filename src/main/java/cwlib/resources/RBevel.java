package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.bevel.BevelVertex;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

/**
 * Resource that stores bevel data for
 * generated meshes in-game.
 */
public class RBevel implements Serializable, Compressable {
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

    @SuppressWarnings("unchecked")
    @Override public RBevel serialize(Serializer serializer, Serializable structure) {
        RBevel bevel = (structure == null) ? new RBevel() : (RBevel) structure;

        serializer.i32(MAX_MATERIALS); // This should always be 4.
        if (bevel.materials == null)
            bevel.materials = new ResourceDescriptor[MAX_MATERIALS];
        for (int i = 0; i < MAX_MATERIALS; ++i)
            bevel.materials[i] = serializer.resource(bevel.materials[i], ResourceType.GFX_MATERIAL);

        serializer.i32(MAX_MATERIALS); // This should always be 4.
        if (bevel.UVScales == null)
            bevel.UVScales = new float[MAX_MATERIALS];
        for (int i = 0; i < MAX_MATERIALS; ++i)
            bevel.UVScales[i] = serializer.f32(bevel.UVScales[i]);

        bevel.autoSmoothCutoffAngle = serializer.f32(bevel.autoSmoothCutoffAngle);
        
        bevel.vertices = serializer.arraylist(bevel.vertices, BevelVertex.class);

        bevel.includeBackface = serializer.bool(bevel.includeBackface);
        bevel.smoothWithFront = serializer.bool(bevel.smoothWithFront);
        bevel.relaxStrength = serializer.f32(bevel.relaxStrength);
        bevel.subDivRadius = serializer.f32(bevel.subDivRadius);
        bevel.fixedBevelSize = serializer.f32(bevel.fixedBevelSize);
        bevel.spongy = serializer.bool(bevel.spongy);

        bevel.softPhysicsSettings = serializer.resource(bevel.softPhysicsSettings, ResourceType.SETTINGS_SOFT_PHYS);

        bevel.textureRepeats = serializer.f32(bevel.textureRepeats);
        
        return bevel;
    }

    @Override public int getAllocatedSize() {
        int size = RBevel.BASE_ALLOCATION_SIZE;
        if (this.vertices != null)
            size += (this.vertices.size() * BevelVertex.BASE_ALLOCATION_SIZE);
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
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
     * @param index Slot index
     * @return Material in slot
     */
    public ResourceDescriptor getMaterial(int index) {
        return this.materials[index];
    }

    /**
     * Sets a material slot.
     * @param material Material resource descriptor to insert
     * @param index Slot index
     */
    public void setMaterial(ResourceDescriptor material, int index) {
        this.materials[index] = material;
    }

    /**
     * Returns the UV scale of the material at specified slot index.
     * @param index Slot index
     * @return UV scale of material in slot
     */
    public float getMaterialUVScale(int index) {
        return this.UVScales[index];
    }

    /**
     * Sets the UV scale of the material at specified slot index.
     * @param uvScale UV scale of the material
     * @param index Slot index
     */
    public void setMaterialUVScale(float uvScale, int index) {
        this.UVScales[index] = uvScale;
    }

    @Override public String toString() {
        return String.format("RBevel{%s}", this.vertices.toString());
    }
}
