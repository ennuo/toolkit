package cwlib.structs.instrument;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Sample implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int baseNote = 48; // MIDI NOTE NUMBERS -> https://www.inspiredacoustics.com/en/MIDI_note_numbers_and_center_frequencies 
    public float baseBpm = 149.5f;
    public boolean pitched = true;
    public boolean fitBpm;
    public float fineTune;

    @SuppressWarnings("unchecked")
    @Override public Sample serialize(Serializer serializer, Serializable structure) {
        Sample sample = (structure == null) ? new Sample() : (Sample) structure;

        sample.baseNote = serializer.s32(sample.baseNote);
        sample.baseBpm = serializer.f32(sample.baseBpm);
        sample.pitched = serializer.bool(sample.pitched);

        // Don't know why this is here twice? They both write to the same variable.
        sample.fitBpm = serializer.bool(sample.fitBpm);
        sample.fitBpm = serializer.bool(sample.fitBpm);

        sample.fineTune = serializer.f32(sample.fineTune);

        return sample;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
