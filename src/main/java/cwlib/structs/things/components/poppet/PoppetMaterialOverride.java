package cwlib.structs.things.components.poppet;

import cwlib.enums.AudioMaterial;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class PoppetMaterialOverride implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xA0;

    public ResourceDescriptor plan, gfxMaterial, bevel, physicsMaterial;
    public float bevelSize;
    public AudioMaterial soundEnum = AudioMaterial.NONE;

    @GsonRevision(lbp3=true, min=0x63)
    public boolean headDucking;

    @SuppressWarnings("unchecked")
    @Override public PoppetMaterialOverride serialize(Serializer serializer, Serializable structure) {
        PoppetMaterialOverride override = 
            (structure == null) ? new PoppetMaterialOverride() : (PoppetMaterialOverride) structure;

        int version = serializer.getRevision().getVersion();
        
        if (version >= 0x2ed)
            override.plan = serializer.resource(override.plan, ResourceType.PLAN, true);

        override.gfxMaterial = serializer.resource(override.gfxMaterial, ResourceType.GFX_MATERIAL);
        override.bevel = serializer.resource(override.bevel, ResourceType.BEVEL);
        override.physicsMaterial = serializer.resource(override.physicsMaterial, ResourceType.MATERIAL);
        override.soundEnum = serializer.enum32(override.soundEnum);
        override.bevelSize = serializer.f32(override.bevelSize);
            
        if (version < 0x2ed)
            override.plan = serializer.resource(override.plan, ResourceType.PLAN, true);

        if (serializer.getRevision().getSubVersion() > 0x62)
            override.headDucking = serializer.bool(override.headDucking);

        return override;
    }

    @Override public int getAllocatedSize() { return PoppetMaterialOverride.BASE_ALLOCATION_SIZE; }
}
