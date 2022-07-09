package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class GlobalAudioSettings implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int reverb;
    public float musicVolume = 1, sfxVolume = 1, backgroundVolume = 1, dialogueVolume = 1;
    public float sfxReverbSend, dialogueReverbSend, musicReverbSend;
    public float sfxFilterLP, dialogueFilterLP, musicFilterLP;
    public float sfxFilterHP, dialogueFilterHP, musicFilterHP;
    public String ambianceTrack;
    
    @SuppressWarnings("unchecked")
    @Override public GlobalAudioSettings serialize(Serializer serializer, Serializable structure) {
        GlobalAudioSettings settings = (structure == null) ? new GlobalAudioSettings() : (GlobalAudioSettings) structure;

        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        // 0x2ef

        settings.reverb = serializer.s32(settings.reverb);
        if (version < 0x347)
            serializer.f32(0);
        settings.musicVolume = serializer.f32(settings.musicVolume);
        settings.sfxVolume = serializer.f32(settings.sfxVolume);
        settings.backgroundVolume = serializer.f32(settings.backgroundVolume);
        settings.dialogueVolume = serializer.f32(settings.dialogueVolume);

        if (version >= 0x342) {
            settings.sfxReverbSend = serializer.f32(settings.sfxReverbSend);
            settings.dialogueReverbSend = serializer.f32(settings.dialogueReverbSend);
        }

        if (version >= 0x347) {
            settings.musicReverbSend = serializer.f32(settings.musicReverbSend);

            settings.sfxFilterLP = serializer.f32(settings.sfxFilterLP);
            settings.dialogueFilterLP = serializer.f32(settings.sfxFilterLP);
            settings.musicFilterLP = serializer.f32(settings.sfxFilterLP);
        }

        if (subVersion >= 0xd8) {
            settings.sfxFilterHP = serializer.f32(settings.sfxFilterHP);
            settings.dialogueFilterHP = serializer.f32(settings.dialogueFilterHP);
            settings.musicFilterHP = serializer.f32(settings.musicFilterHP);
        }

        if (subVersion >= 0xe2)
            settings.ambianceTrack = serializer.str(settings.ambianceTrack);
        
        return settings;
    }

    @Override public int getAllocatedSize() { return GlobalAudioSettings.BASE_ALLOCATION_SIZE; }
}
