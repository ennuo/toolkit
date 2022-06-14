package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PAudioWorld implements Serializable {
    public String soundName;
    public float initialVolume, initialPitch, initialParam1;
    public float maxFalloff, impactTolerance;
    public boolean triggerByFalloff;
    public boolean triggerByImpact;
    public boolean triggerBySwitch;
    public boolean triggerByDestroy;
    public boolean paramAffectVol;
    public boolean paramAffectPitch;
    public boolean paramAffectParam;
    public boolean isLocal;
    public boolean hideInPlayMode;
    
    public PAudioWorld serialize(Serializer serializer, Serializable structure) {
        PAudioWorld audioWorld = (structure == null) ? new PAudioWorld() : (PAudioWorld) structure;
        
        audioWorld.soundName = serializer.str8(audioWorld.soundName);
        
        audioWorld.initialVolume = serializer.f32(audioWorld.initialVolume);
        audioWorld.initialPitch = serializer.f32(audioWorld.initialPitch);
        audioWorld.initialParam1 = serializer.f32(audioWorld.initialParam1);
        
        audioWorld.maxFalloff = serializer.f32(audioWorld.maxFalloff);
        audioWorld.impactTolerance = serializer.f32(audioWorld.impactTolerance);
        
        audioWorld.triggerByFalloff = serializer.bool(audioWorld.triggerByFalloff);
        audioWorld.triggerByImpact = serializer.bool(audioWorld.triggerByImpact);
        audioWorld.triggerBySwitch = serializer.bool(audioWorld.triggerBySwitch);
        audioWorld.triggerByDestroy = serializer.bool(audioWorld.triggerByDestroy);
        
        audioWorld.paramAffectVol = serializer.bool(audioWorld.paramAffectVol);
        audioWorld.paramAffectPitch = serializer.bool(audioWorld.paramAffectPitch);
        audioWorld.paramAffectParam = serializer.bool(audioWorld.paramAffectParam);
        
        audioWorld.isLocal = serializer.bool(audioWorld.isLocal);
        
        audioWorld.hideInPlayMode = serializer.bool(audioWorld.hideInPlayMode);
        
        return audioWorld;
    }
    
}
