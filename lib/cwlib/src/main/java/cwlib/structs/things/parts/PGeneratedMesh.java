package cwlib.structs.things.parts;

import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.enums.VisibilityFlags;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonResourceType;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

/**
 * This part functionally serves as the rendering
 * component for materials in games, this must
 * be used alongside a PShape.
 */
public class PGeneratedMesh implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    /**
     * The material used to render this mesh.
     */
    @GsonResourceType(ResourceType.GFX_MATERIAL)
    public ResourceDescriptor gfxMaterial =
        new ResourceDescriptor(11166, ResourceType.GFX_MATERIAL);

    /**
     * The bevel used with this mesh.
     */
    @GsonResourceType(ResourceType.BEVEL)
    public ResourceDescriptor bevel;

    /**
     * How much the UVs of the texture are offset.
     * (UV tool in-game)
     */
    public Vector4f uvOffset;

    /**
     * The plan this generated mesh came from.
     */
    @GsonRevision(min = 0x258)
    public GUID planGUID;

    /**
     * Flags controlling the visibility of this mesh.
     */
    @GsonRevision(min = 0x27c)
    public byte visibilityFlags = VisibilityFlags.PLAY_MODE | VisibilityFlags.EDIT_MODE;

    /**
     * Speed of this material's animation,
     * animations are defined in the RGfxMaterial.
     */
    @GsonRevision(min = 0x305)
    public float textureAnimationSpeed = 1.0f;

    /**
     * The speed of this material's animation when it's off,
     * animations are defined in the RGfxMaterial.
     */
    @GsonRevision(min = 0x305)
    public float textureAnimationSpeedOff = 1.0f;


    /* Vita texture parameters */
    @GsonRevision(branch = 0x4431, min = 0x6b)
    public int uvMode;
    @GsonRevision(branch = 0x4431, min = 0x76, max = 0x76)
    public float textureScale;

    /**
     * Indicates that this material should have no bevel.
     */
    @GsonRevision(lbp3 = true, min = 0x34)
    public boolean noBevel;

    /**
     * Whether or not this material has been sharded.
     */
    @GsonRevision(lbp3 = true, min = 0x97)
    public boolean sharded;

    /**
     * Whether or not the sides should be rendered(?)
     */
    @GsonRevision(lbp3 = true, min = 0x13d)
    public boolean includeSides = true;

    @GsonRevision(lbp3 = true, min = 0x155)
    public byte slideImpactDamping = 50;

    /**
     * Speed the player moves when steering on a slide.
     */
    @GsonRevision(lbp3 = true, min = 0x13d)
    public byte slideSteer = 100;

    /**
     * Speed the player descends down this slide.
     */
    @GsonRevision(lbp3 = true, min = 0x13d)
    public byte slideSpeed = 50;

    public PGeneratedMesh() { }

    public PGeneratedMesh(ResourceDescriptor material, ResourceDescriptor bevel)
    {
        this.gfxMaterial = material;
        this.bevel = bevel;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        gfxMaterial = serializer.resource(gfxMaterial, ResourceType.GFX_MATERIAL);
        bevel = serializer.resource(bevel, ResourceType.BEVEL);
        uvOffset = serializer.v4(uvOffset);
        if (version >= 0x258)
            planGUID = serializer.guid(planGUID);

        if (version >= 0x27c && subVersion < 0xfb)
        {
            if (serializer.isWriting())
                serializer.getOutput().bool((visibilityFlags & VisibilityFlags.PLAY_MODE) != 0);
            else
            {
                visibilityFlags = VisibilityFlags.EDIT_MODE;
                if (serializer.getInput().bool())
                    visibilityFlags |= VisibilityFlags.PLAY_MODE;
            }
        }

        if (subVersion >= 0xfb)
            visibilityFlags = serializer.i8(visibilityFlags);

        if (version >= 0x305)
        {
            textureAnimationSpeed = serializer.f32(textureAnimationSpeed);
            textureAnimationSpeedOff = serializer.f32(textureAnimationSpeedOff);
        }

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();
            if (vita >= 0x6b)
                uvMode = serializer.i32(uvMode);
            if (vita == 0x76)
                textureScale = serializer.f32(textureScale);
        }

        if (subVersion >= 0x34)
            noBevel = serializer.bool(noBevel);
        if (subVersion >= 0x97)
            sharded = serializer.bool(sharded);
        if (subVersion >= 0x13d)
            includeSides = serializer.bool(includeSides);
        if (subVersion >= 0x155)
            slideImpactDamping = serializer.i8(slideImpactDamping);
        if (subVersion >= 0x13d)
        {
            slideSteer = serializer.i8(slideSteer);
            slideSpeed = serializer.i8(slideSpeed);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}