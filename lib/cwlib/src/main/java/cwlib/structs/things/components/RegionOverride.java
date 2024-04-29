package cwlib.structs.things.components;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class RegionOverride implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public int region;

    @GsonRevision(min = 0x360)
    public ResourceDescriptor materialPlan;

    public ResourceDescriptor material;

    @GsonRevision(min = 0x31c)
    public Vector3f uvScale = new Vector3f(10.0f, 10.0f, 10.0f);

    @GsonRevision(lbp3 = true, min = 0x158)
    public int color;

    @GsonRevision(lbp3 = true, min = 0x158)
    public byte brightness;

    @Override
    public void serialize(Serializer serializer)
    {
        region = serializer.i32(region);
        materialPlan = serializer.resource(materialPlan, ResourceType.PLAN, true);
        material = serializer.resource(material, ResourceType.GFX_MATERIAL);
        uvScale = serializer.v3(uvScale);

        if (serializer.getRevision().getSubVersion() >= 0x158)
        {
            color = serializer.i32(color);
            brightness = serializer.i8(brightness);
        }
    }


    @Override
    public int getAllocatedSize()
    {
        return RegionOverride.BASE_ALLOCATION_SIZE;
    }
}
