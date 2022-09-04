package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PSequencer implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public float tempo, swing, echoFeedback, echoTime, echoMix;

    @GsonRevision(min=0x371)
    public int reverbSettings;

    @GsonRevision(min=0x36f)
    public boolean loop;

    public float startPoint;
    public int numChannels;
    private float[] volume = new float[6];

    @GsonRevision(min=0x36a)
    public float playHead;
    @GsonRevision(min=0x36a)
    public boolean isPlaying;

    @GsonRevision(min=0x36d)
    public boolean musicSequencer;

    @GsonRevision(lbp3=true,min=0x29)
    public boolean animationSequencer;

    @GsonRevision(min=0x372)
    public int behavior;

    @GsonRevision(min=0x3a1)
    public int triggerPlayer;

    @GsonRevision(min=0x3a1)
    public Thing previewThing;

    @SuppressWarnings("unchecked")
    @Override public PSequencer serialize(Serializer serializer, Serializable structure) {
        PSequencer sequencer = (structure == null) ? new PSequencer() : (PSequencer) structure;

        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();

        sequencer.tempo = serializer.f32(sequencer.tempo);
        sequencer.swing = serializer.f32(sequencer.swing);
        sequencer.echoFeedback = serializer.f32(sequencer.echoFeedback);
        sequencer.echoTime = serializer.f32(sequencer.echoTime);
        sequencer.echoMix = serializer.f32(sequencer.echoMix);

        if (version > 0x370)
            sequencer.reverbSettings = serializer.i32(sequencer.reverbSettings);
        if (version > 0x36e)
            sequencer.loop = serializer.bool(sequencer.loop);

        sequencer.startPoint = serializer.f32(sequencer.startPoint);

        sequencer.numChannels = serializer.i32(sequencer.numChannels);
        for (int i = 0; i < 6; ++i)
            sequencer.volume[i] = serializer.f32(sequencer.volume[i]);
        
        if (version > 0x369) {
            sequencer.playHead = serializer.f32(sequencer.playHead);
            sequencer.isPlaying = serializer.bool(sequencer.isPlaying);
        }
        if (version > 0x36c)
            sequencer.musicSequencer = serializer.bool(sequencer.musicSequencer);
        if (subVersion > 0x28)
            sequencer.animationSequencer = serializer.bool(sequencer.animationSequencer);

        if (version > 0x371)
            sequencer.behavior = serializer.i32(sequencer.behavior);
        
        if (version > 0x3a0) {
            sequencer.triggerPlayer = serializer.i32(sequencer.triggerPlayer);
            sequencer.previewThing = serializer.thing(sequencer.previewThing);
        }

        return sequencer;
    }

    @Override public int getAllocatedSize() { return PSequencer.BASE_ALLOCATION_SIZE; }
    
    public float[] getVolume() { return this.volume; }
}
