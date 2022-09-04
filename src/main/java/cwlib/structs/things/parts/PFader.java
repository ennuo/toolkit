package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PFader implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;
    
    public boolean includeRigidConnectors;
    @GsonRevision(lbp3=true,min=0xe0) public boolean includeLights;
    public boolean requirePlayerObscuration;
    public float fadeAmount;
    @GsonRevision(lbp3=true,min=0x17) public boolean faderDisabled;
    @GsonRevision(lbp3=true,min=0x39) public float fadeTimeSeconds;
    @GsonRevision(lbp3=true,min=0x3b) public byte inputBehavior;

    @SuppressWarnings("unchecked")
    @Override public PFader serialize(Serializer serializer, Serializable structure) {
        PFader fader = (structure == null) ? new PFader() : (PFader) structure;
        
        int subVersion = serializer.getRevision().getSubVersion();

        fader.includeRigidConnectors = serializer.bool(fader.includeRigidConnectors);
        if (subVersion >= 0xe0)
            fader.includeLights = serializer.bool(fader.includeLights);
        fader.requirePlayerObscuration = serializer.bool(fader.requirePlayerObscuration);
        fader.fadeAmount = serializer.f32(fader.fadeAmount);
        if (subVersion >= 0x17)
            fader.faderDisabled = serializer.bool(fader.faderDisabled);
        if (subVersion >= 0x39)
            fader.fadeTimeSeconds = serializer.f32(fader.fadeTimeSeconds);
        if (subVersion >= 0x3b)
            fader.inputBehavior = serializer.i8(fader.inputBehavior);
        
        return fader;
    }

    @Override public int getAllocatedSize() { return PFader.BASE_ALLOCATION_SIZE; }
}
