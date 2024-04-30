package cwlib.structs.things.parts;

import org.joml.Vector3f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.things.components.RegionOverride;
import cwlib.types.data.ResourceDescriptor;

public class PMaterialOverride implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public RegionOverride[] overrides;
    public ResourceDescriptor mesh;

    @GsonRevision(lbp3 = true, min = 0x15f)
    public int color;

    @GsonRevision(lbp3 = true, min = 0x191)
    public byte brightness;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        if (version < 0x360)
        {
            if (serializer.isWriting())
            {
                MemoryOutputStream stream = serializer.getOutput();
                if (overrides != null && overrides.length != 0)
                {
                    stream.i32(overrides.length);
                    for (RegionOverride region : overrides)
                    {
                        Primitive primitive = new Primitive();
                        primitive.setRegion(region.region);
                        serializer.struct(primitive, Primitive.class);
                    }
                    stream.i32(overrides.length);
                    for (RegionOverride region : overrides)
                        serializer.resource(region.material,
                            ResourceType.GFX_MATERIAL, true);
                    if (version > 0x31b)
                    {
                        stream.i32(overrides.length);
                        if (version < 0x32b)
                        {
                            for (RegionOverride region : overrides)
                                stream.f32(region.uvScale.x);
                        }
                        else
                        {
                            for (RegionOverride region : overrides)
                                stream.v3(region.uvScale);
                        }
                    }
                }
                else
                {
                    stream.i32(0); // mesh primitives
                    stream.i32(0); // materials
                    if (version > 0x31b) stream.i32(0); // uv scales
                }
            }
            else
            {
                MemoryInputStream stream = serializer.getInput();
                Primitive[] primitives = serializer.array(null, Primitive.class);
                ResourceDescriptor[] materials = new ResourceDescriptor[stream.i32()];
                for (int i = 0; i < materials.length; ++i)
                    materials[i] = serializer.resource(null, ResourceType.GFX_MATERIAL,
                        true);
                Vector3f[] uvScales = new Vector3f[version > 0x31b ? stream.i32() : 0];
                if (version > 0x32a)
                {
                    for (int i = 0; i < uvScales.length; ++i)
                        uvScales[i] = stream.v3();
                }
                else
                {
                    for (int i = 0; i < uvScales.length; ++i)
                    {
                        float scale = stream.f32();
                        uvScales[i] = new Vector3f(scale, scale, scale);
                    }
                }

                overrides = new RegionOverride[primitives.length];
                for (int i = 0; i < overrides.length; ++i)
                {
                    RegionOverride region = new RegionOverride();
                    region.region = primitives[i].getRegion();
                    region.material = materials[i];
                    if (version > 0x31b)
                        region.uvScale = uvScales[i];
                    overrides[i] = region;
                }
            }
            mesh = serializer.resource(mesh, ResourceType.MESH);
        }

        if (version >= 0x360)
        {
            overrides = serializer.array(overrides, RegionOverride.class);
            mesh = serializer.resource(mesh, ResourceType.MESH);
            if (subVersion >= 0x15f)
            {
                color = serializer.i32(color);
                if (subVersion >= 0x191)
                    brightness = serializer.i8(brightness);
            }
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PMaterialOverride.BASE_ALLOCATION_SIZE;
        if (this.overrides != null)
            for (RegionOverride override : this.overrides)
                size += override.getAllocatedSize();
        return size;
    }
}
