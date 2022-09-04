package cwlib.structs.things.components.world;

import cwlib.enums.Branch;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class GlobalSettings implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x14;

    public float lightingFactor = 1.0f, colorCorrectionFactor, fogFactor, fogTintFactor, darknessFactor;
    
    @GsonRevision(branch=0x4431, min=0x78)
    public boolean nonLinearFog; // Vita
    
    @SuppressWarnings("unchecked")
    @Override public GlobalSettings serialize(Serializer serializer, Serializable structure) {
        GlobalSettings settings = (structure == null) ? new GlobalSettings() : (GlobalSettings) structure;

        settings.lightingFactor = serializer.f32(settings.lightingFactor);
        settings.colorCorrectionFactor = serializer.f32(settings.colorCorrectionFactor);
        settings.fogFactor = serializer.f32(settings.fogFactor);
        settings.fogTintFactor = serializer.f32(settings.fogTintFactor);
        settings.darknessFactor = serializer.f32(settings.darknessFactor);
        if (serializer.getRevision().has(Branch.DOUBLE11, 0x78))
            settings.nonLinearFog = serializer.bool(settings.nonLinearFog);
        
        return settings;
    }

    @Override public int getAllocatedSize() { return GlobalSettings.BASE_ALLOCATION_SIZE; }
}
