package cwlib.structs.things.components.poppet;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.AudioMaterialSerializer;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class PoppetMaterialOverride implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0xA0;

    public ResourceDescriptor plan, gfxMaterial, bevel, physicsMaterial;
    public float bevelSize;

    @JsonAdapter(AudioMaterialSerializer.class)
    public int soundEnum;

    @GsonRevision(lbp3 = true, min = 0x63)
    public boolean headDucking;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        if (version >= 0x2ed)
            plan = serializer.resource(plan, ResourceType.PLAN, true);

        gfxMaterial = serializer.resource(gfxMaterial, ResourceType.GFX_MATERIAL);
        bevel = serializer.resource(bevel, ResourceType.BEVEL);
        physicsMaterial = serializer.resource(physicsMaterial,
            ResourceType.MATERIAL);
        soundEnum = serializer.i32(soundEnum);
        bevelSize = serializer.f32(bevelSize);

        if (version < 0x2ed)
            plan = serializer.resource(plan, ResourceType.PLAN, true);

        if (serializer.getRevision().getSubVersion() > 0x62)
            headDucking = serializer.bool(headDucking);
    }

    @Override
    public int getAllocatedSize()
    {
        return PoppetMaterialOverride.BASE_ALLOCATION_SIZE;
    }
}
