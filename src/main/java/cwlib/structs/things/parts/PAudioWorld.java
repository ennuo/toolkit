package cwlib.structs.things.parts;

import cwlib.enums.PlayMode;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;

public class PAudioWorld implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public String soundName;
    public float initialVolume, initialPitch, initialParam1;
    public float maxFalloff, impactTolerance;

    @GsonRevision(max=0x2c3)
    public boolean triggerByFalloff = true, triggerByImpact, triggerBySwitch;
    @GsonRevision(min=0x1ad, max=0x2c3)
    public boolean triggerByDestroy;

    @GsonRevision(min=0x2c4)
    public PlayMode playMode = PlayMode.TRIGGER_BY_FALLOFF;

    public boolean paramAffectVol;
    public boolean paramAffectPitch;
    public boolean paramAffectParam;

    @GsonRevision(min=0x165)
    public boolean isLocal;
    @GsonRevision(min=0x198)
    public boolean hideInPlayMode;

    @GsonRevision(min=0x2c4)
    public int behavior;
    
    @GsonRevision(min=0x380)
    public GUID soundNames;
    @GsonRevision(min=0x165)
    public int meshColor;
    @GsonRevision(lbp3=true,min=0x178)
    public int categoryGUID;
    @GsonRevision(lbp3=true,min=0x191)
    public boolean activatedLastFrame;

    @SuppressWarnings("unchecked")
    @Override public PAudioWorld serialize(Serializer serializer, Serializable structure) {
        PAudioWorld audio = (structure == null) ? new PAudioWorld() : (PAudioWorld) structure;

        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        audio.soundName = serializer.str(audio.soundName);
        
        audio.initialVolume = serializer.f32(audio.initialVolume);
        audio.initialPitch = serializer.f32(audio.initialPitch);
        audio.initialParam1 = serializer.f32(audio.initialParam1);
        
        audio.maxFalloff = serializer.f32(audio.maxFalloff);
        audio.impactTolerance = serializer.f32(audio.impactTolerance);
        
        if (version < 0x2c4) {
            audio.triggerByFalloff = serializer.bool(audio.triggerByFalloff); // = play mode 0
            audio.triggerByImpact = serializer.bool(audio.triggerByImpact); // = play mode 1 
            if (version < 0x165)
                serializer.bool(false); // unk
            audio.triggerBySwitch = serializer.bool(audio.triggerBySwitch);
            if (version < 0x165)
                serializer.bool(false);
            if (version >= 0x1ad)
                audio.triggerByDestroy = serializer.bool(audio.triggerByDestroy); // = play mode 2

            // Not going to depreciate these just yet
            if (!serializer.isWriting()) {
                if (audio.triggerByImpact) audio.playMode = PlayMode.TRIGGER_BY_IMPACT;
                else if (audio.triggerByDestroy) audio.playMode = PlayMode.TRIGGER_BY_DESTROY;
            }
            
        } else {
            audio.playMode = serializer.enum32(audio.playMode);

            if (!serializer.isWriting()) {
                if (audio.playMode == PlayMode.TRIGGER_BY_DESTROY) audio.triggerByDestroy = true;
                else if (audio.playMode == PlayMode.TRIGGER_BY_IMPACT) audio.triggerByImpact = true;
                else audio.triggerByFalloff = true;
            }
        }

        audio.paramAffectVol = serializer.bool(audio.paramAffectVol);
        audio.paramAffectPitch = serializer.bool(audio.paramAffectPitch);
        audio.paramAffectParam = serializer.bool(audio.paramAffectParam);
        
        if (version >= 0x165)
            audio.isLocal = serializer.bool(audio.isLocal);
        if (version >= 0x198)
            audio.hideInPlayMode = serializer.bool(audio.hideInPlayMode);

        if (version >= 0x2c4)
            audio.behavior = serializer.i32(audio.behavior);

        if (version >= 0x380)
            audio.soundNames = serializer.guid(audio.soundNames);

        if (version >= 0x380)
            audio.meshColor = serializer.i32(audio.meshColor);

        if (subVersion >= 0x178)
            audio.categoryGUID = serializer.i32(audio.categoryGUID);
        if (subVersion >= 0x191)
            audio.activatedLastFrame = serializer.bool(audio.activatedLastFrame);
        
        return audio;
    }

    @Override public int getAllocatedSize() {
        int size = PAudioWorld.BASE_ALLOCATION_SIZE;
        if (this.soundName != null)
            size += (this.soundName.length());
        return size;
    }
}
