package cwlib.structs.things.components.world;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class GlobalAudioSettings implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int reverb;
    public float musicVolume = 1, sfxVolume = 1, backgroundVolume = 1, dialogueVolume = 1;

    @GsonRevision(min = 0x342)
    public float sfxReverbSend, dialogueReverbSend;

    @GsonRevision(min = 0x347)
    public float musicReverbSend, sfxFilterLP, dialogueFilterLP, musicFilterLP;

    @GsonRevision(lbp3 = true, min = 0xd8)
    public float sfxFilterHP, dialogueFilterHP, musicFilterHP;

    @GsonRevision(lbp3 = true, min = 0xe2)
    public String ambianceTrack;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        reverb = serializer.s32(reverb);
        if (version < 0x347)
            serializer.f32(0);
        musicVolume = serializer.f32(musicVolume);
        sfxVolume = serializer.f32(sfxVolume);
        backgroundVolume = serializer.f32(backgroundVolume);
        dialogueVolume = serializer.f32(dialogueVolume);

        if (version >= 0x342)
        {
            sfxReverbSend = serializer.f32(sfxReverbSend);
            dialogueReverbSend = serializer.f32(dialogueReverbSend);
        }

        if (version >= 0x347)
        {
            musicReverbSend = serializer.f32(musicReverbSend);

            sfxFilterLP = serializer.f32(sfxFilterLP);
            dialogueFilterLP = serializer.f32(dialogueFilterLP);
            musicFilterLP = serializer.f32(musicFilterLP);
        }

        if (subVersion >= 0xd8)
        {
            sfxFilterHP = serializer.f32(sfxFilterHP);
            dialogueFilterHP = serializer.f32(dialogueFilterHP);
            musicFilterHP = serializer.f32(musicFilterHP);
        }

        if (subVersion >= 0xe2)
            ambianceTrack = serializer.str(ambianceTrack);
    }

    @Override
    public int getAllocatedSize()
    {
        return GlobalAudioSettings.BASE_ALLOCATION_SIZE;
    }
}
