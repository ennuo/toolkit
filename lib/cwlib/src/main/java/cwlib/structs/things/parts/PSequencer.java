package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PSequencer implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public float tempo, swing, echoFeedback, echoTime, echoMix;

    @GsonRevision(min = 0x371)
    public int reverbSettings;

    @GsonRevision(min = 0x36f)
    public boolean loop;

    public float startPoint;
    public int numChannels;
    private final float[] volume = new float[6];

    @GsonRevision(min = 0x36a)
    public float playHead;
    @GsonRevision(min = 0x36a)
    public boolean isPlaying;

    @GsonRevision(min = 0x36d)
    public boolean musicSequencer;

    @GsonRevision(lbp3 = true, min = 0x29)
    public boolean animationSequencer;

    @GsonRevision(min = 0x372)
    public int behavior;

    @GsonRevision(min = 0x3a1)
    public int triggerPlayer;

    @GsonRevision(min = 0x3a1)
    public Thing previewThing;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        tempo = serializer.f32(tempo);
        swing = serializer.f32(swing);
        echoFeedback = serializer.f32(echoFeedback);
        echoTime = serializer.f32(echoTime);
        echoMix = serializer.f32(echoMix);

        if (version > 0x370)
            reverbSettings = serializer.i32(reverbSettings);
        if (version > 0x36e)
            loop = serializer.bool(loop);

        startPoint = serializer.f32(startPoint);

        numChannels = serializer.i32(numChannels);
        for (int i = 0; i < 6; ++i)
            volume[i] = serializer.f32(volume[i]);

        if (version > 0x369)
        {
            playHead = serializer.f32(playHead);
            isPlaying = serializer.bool(isPlaying);
        }
        if (version > 0x36c)
            musicSequencer = serializer.bool(musicSequencer);
        if (subVersion > 0x28)
            animationSequencer = serializer.bool(animationSequencer);

        if (version > 0x371)
            behavior = serializer.i32(behavior);

        if (version > 0x3a0)
        {
            triggerPlayer = serializer.i32(triggerPlayer);
            previewThing = serializer.thing(previewThing);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return PSequencer.BASE_ALLOCATION_SIZE;
    }

    public float[] getVolume()
    {
        return this.volume;
    }
}
