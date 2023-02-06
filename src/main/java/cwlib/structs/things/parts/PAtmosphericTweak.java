package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PAtmosphericTweak implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int atmosType;
    public float intensity;
    public float directionStrength;
    public int inputAction;

    @GsonRevision(lbp3=true, min=0x136)
    public float maxParticles;

    @GsonRevision(lbp3=true, min=0xb4)
    public float currentInput;

    @GsonRevision(lbp3=true, min=0x19c)
    public float intensityOff, directionStrengthOff;

    @SuppressWarnings("unchecked")
    @Override public PAtmosphericTweak serialize(Serializer serializer, Serializable structure) {
        PAtmosphericTweak tweak = (structure == null) ? new PAtmosphericTweak() : (PAtmosphericTweak) structure;

        int subVersion = serializer.getRevision().getSubVersion();

        tweak.atmosType = serializer.s32(tweak.atmosType);
        tweak.intensity = serializer.f32(tweak.intensity);
        tweak.directionStrength = serializer.f32(tweak.directionStrength);
        tweak.inputAction = serializer.s32(tweak.inputAction);

        if (subVersion < 0x1a8)
            serializer.bool(false); // disableAudio

        if (subVersion > 0x135)
            tweak.maxParticles = serializer.f32(tweak.maxParticles);
        if (subVersion > 0xb3)
            tweak.currentInput = serializer.f32(tweak.currentInput);
        
        if (subVersion >= 0xb9 && subVersion < 0xd4) {
            serializer.u8(0);
            serializer.f32(0);
        }

        if (subVersion > 0x19b) {
            tweak.intensityOff = serializer.f32(tweak.intensityOff);
            tweak.directionStrengthOff = serializer.f32(tweak.directionStrengthOff);
        }
        
        return tweak;
    }

    @Override public int getAllocatedSize() {
        return PAtmosphericTweak.BASE_ALLOCATION_SIZE;
    }
}
