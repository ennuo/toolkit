package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PFader implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public boolean includeRigidConnectors;
    @GsonRevision(lbp3 = true, min = 0xe0)
    public boolean includeLights;
    public boolean requirePlayerObscuration;
    public float fadeAmount;
    @GsonRevision(lbp3 = true, min = 0x17)
    public boolean faderDisabled;
    @GsonRevision(lbp3 = true, min = 0x39)
    public float fadeTimeSeconds;
    @GsonRevision(lbp3 = true, min = 0x3b)
    public byte inputBehavior;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        includeRigidConnectors = serializer.bool(includeRigidConnectors);
        if (subVersion >= 0xe0)
            includeLights = serializer.bool(includeLights);
        requirePlayerObscuration = serializer.bool(requirePlayerObscuration);
        fadeAmount = serializer.f32(fadeAmount);
        if (subVersion >= 0x17)
            faderDisabled = serializer.bool(faderDisabled);
        if (subVersion >= 0x39)
            fadeTimeSeconds = serializer.f32(fadeTimeSeconds);
        if (subVersion >= 0x3b)
            inputBehavior = serializer.i8(inputBehavior);
    }

    @Override
    public int getAllocatedSize()
    {
        return PFader.BASE_ALLOCATION_SIZE;
    }
}
