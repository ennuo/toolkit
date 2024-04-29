package cwlib.structs.things.components.world;

import cwlib.enums.Branch;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class GlobalSettings implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    public float lightingFactor = 1.0f, colorCorrectionFactor, fogFactor, fogTintFactor,
        darknessFactor;

    @GsonRevision(branch = 0x4431, min = 0x78)
    public boolean nonLinearFog; // Vita

    @Override
    public void serialize(Serializer serializer)
    {
        lightingFactor = serializer.f32(lightingFactor);
        colorCorrectionFactor = serializer.f32(colorCorrectionFactor);
        fogFactor = serializer.f32(fogFactor);
        fogTintFactor = serializer.f32(fogTintFactor);
        darknessFactor = serializer.f32(darknessFactor);
        if (serializer.getRevision().has(Branch.DOUBLE11, 0x78))
            nonLinearFog = serializer.bool(nonLinearFog);
    }

    @Override
    public int getAllocatedSize()
    {
        return GlobalSettings.BASE_ALLOCATION_SIZE;
    }
}
