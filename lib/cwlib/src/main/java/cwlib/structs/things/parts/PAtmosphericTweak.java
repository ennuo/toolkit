package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PAtmosphericTweak implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int atmosType;
    public float intensity;
    public float directionStrength;
    public int inputAction;

    @GsonRevision(lbp3 = true, min = 0x136)
    public float maxParticles;

    @GsonRevision(lbp3 = true, min = 0xb4)
    public float currentInput;

    @GsonRevision(lbp3 = true, min = 0x19c)
    public float intensityOff, directionStrengthOff;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        atmosType = serializer.s32(atmosType);
        intensity = serializer.f32(intensity);
        directionStrength = serializer.f32(directionStrength);
        inputAction = serializer.s32(inputAction);

        if (subVersion < 0x1a8)
            serializer.bool(false); // disableAudio

        if (subVersion > 0x135)
            maxParticles = serializer.f32(maxParticles);
        if (subVersion > 0xb3)
            currentInput = serializer.f32(currentInput);

        if (subVersion >= 0xb9 && subVersion < 0xd4)
        {
            serializer.u8(0);
            serializer.f32(0);
        }

        if (subVersion > 0x19b)
        {
            intensityOff = serializer.f32(intensityOff);
            directionStrengthOff = serializer.f32(directionStrengthOff);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return PAtmosphericTweak.BASE_ALLOCATION_SIZE;
    }
}
