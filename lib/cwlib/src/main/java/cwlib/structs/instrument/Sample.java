package cwlib.structs.instrument;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Sample implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int baseNote = 48; // MIDI NOTE NUMBERS -> https://www.inspiredacoustics.com/en/MIDI_note_numbers_and_center_frequencies

    public float baseBpm = 149.5f;
    public boolean pitched = true;
    public boolean fitBpm;
    public float fineTune;

    @Override
    public void serialize(Serializer serializer)
    {
        baseNote = serializer.s32(baseNote);
        baseBpm = serializer.f32(baseBpm);
        pitched = serializer.bool(pitched);

        // Don't know why this is here twice? They both write to the same variable.
        fitBpm = serializer.bool(fitBpm);
        fitBpm = serializer.bool(fitBpm);

        fineTune = serializer.f32(fineTune);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
