package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.RegionOverride;
import cwlib.types.data.ResourceDescriptor;

public class PMaterialOverride implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public RegionOverride[] overrides;
    public ResourceDescriptor mesh;
    public int color;
    public byte brightness;

    @SuppressWarnings("unchecked")
    @Override public PMaterialOverride serialize(Serializer serializer, Serializable structure) {
        PMaterialOverride override = (structure == null) ? new PMaterialOverride() : (PMaterialOverride) structure;
        
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        if (version < 0x360) {
            // arr of mesh primitive
            // arr of descriptors (bit)
            if (version > 0x31b && version < 0x32b) {
                // region ids? int[]
            }
            if (version > 0x32a) {
                // uv scales v3[]
            }

            throw new SerializationException("PMaterialOverride below 0x360 not supported!");
        }

        if (version >= 0x360) {
            override.overrides = serializer.array(override.overrides, RegionOverride.class);
            override.mesh = serializer.resource(override.mesh, ResourceType.MESH);
            if (subVersion >= 0x15f) {
                override.color = serializer.i32(override.color);
                if (subVersion >= 0x191) 
                    override.brightness = serializer.i8(override.brightness);
            }
        }

        return override;
    }
    
    @Override public int getAllocatedSize() { 
        int size = PMaterialOverride.BASE_ALLOCATION_SIZE;
        if (this.overrides != null)
            for (RegionOverride override : this.overrides)
                size += override.getAllocatedSize();
        return size;
    }
}
