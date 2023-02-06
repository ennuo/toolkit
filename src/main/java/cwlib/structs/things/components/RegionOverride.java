package cwlib.structs.things.components;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class RegionOverride implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public int region;

    @GsonRevision(min=0x360)
    public ResourceDescriptor materialPlan;
    
    public ResourceDescriptor material;

    @GsonRevision(min=0x31c)
    public Vector3f uvScale = new Vector3f(10.0f, 10.0f, 10.0f);

    @GsonRevision(lbp3=true, min=0x158)
    public int color;
    
    @GsonRevision(lbp3=true, min=0x158)
    public byte brightness;

    @SuppressWarnings("unchecked")
    @Override public RegionOverride serialize(Serializer serializer, Serializable structure) {
        RegionOverride override = (structure == null) ? new RegionOverride() : (RegionOverride) structure;

        override.region = serializer.i32(override.region);
        override.materialPlan = serializer.resource(override.materialPlan, ResourceType.PLAN, true);
        override.material = serializer.resource(override.material, ResourceType.GFX_MATERIAL);
        override.uvScale = serializer.v3(override.uvScale);
        
        if (serializer.getRevision().getSubVersion() >= 0x158) {
            override.color = serializer.i32(override.color);
            override.brightness = serializer.i8(override.brightness);
        }
        
        return override;
    }




    @Override public int getAllocatedSize() { return RegionOverride.BASE_ALLOCATION_SIZE; }
}
