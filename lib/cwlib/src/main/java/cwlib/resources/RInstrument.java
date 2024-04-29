package cwlib.resources;

import org.joml.Vector2f;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.instrument.Sample;

public class RInstrument implements Resource
{
    public static final int BASE_ALLOCATION_SIZE = 0x200;

    public static final int MAX_SAMPLES = 0x8;
    public static final int MAX_PARAMS = 0x1b;
    public static final int MAX_ARPEGGIO = 0x20;
    public static final int MAX_SPLITS = 0x9;

    private final Sample[] samples = new Sample[MAX_SAMPLES];
    private final GUID[] sampleGUIDs = new GUID[MAX_SAMPLES];
    private final int[] splitNotes = new int[MAX_SPLITS];  // KEY NUMBER (PIANO) -> https://www.inspiredacoustics.com/en/MIDI_note_numbers_and_center_frequencies

    private int numStack = 1;
    private final Vector2f[] params = new Vector2f[MAX_PARAMS];
    private final byte[] arpeggio = new byte[MAX_ARPEGGIO];
    private boolean arpeggiate = false;

    public RInstrument()
    {
        for (int i = 0; i < MAX_SAMPLES; ++i)
            this.samples[i] = new Sample();
        for (int i = 0; i < MAX_PARAMS; ++i)
            this.params[i] = new Vector2f().zero();
        for (int i = 0; i < MAX_ARPEGGIO; ++i)
            this.arpeggio[i] = 0xf;
        this.splitNotes[0] = 87;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        serializer.i32(MAX_SAMPLES);
        for (int i = 0; i < MAX_SAMPLES; ++i)
            samples[i] = serializer.struct(samples[i], Sample.class);

        serializer.i32(MAX_SAMPLES);
        for (int i = 0; i < MAX_SAMPLES; ++i)
            sampleGUIDs[i] = serializer.guid(sampleGUIDs[i]);

        serializer.i32(MAX_SPLITS);
        for (int i = 0; i < MAX_SPLITS; ++i)
            splitNotes[i] = serializer.s32(splitNotes[i]);

        numStack = serializer.s32(numStack);

        for (int i = 0; i < MAX_PARAMS; ++i)
        {
            serializer.i32(2); // Technically a float array, but it's always of length 2
            params[i] = serializer.v2(params[i]);
        }

        serializer.i32(MAX_ARPEGGIO);
        for (int i = 0; i < MAX_ARPEGGIO; ++i)
            arpeggio[i] = serializer.i8(arpeggio[i]);

        arpeggiate = serializer.bool(arpeggiate);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
        serializer.struct(this, RInstrument.class);
        return new SerializationData(
            serializer.getBuffer(),
            revision,
            compressionFlags,
            ResourceType.INSTRUMENT,
            SerializationType.BINARY,
            serializer.getDependencies()
        );
    }

    public int getNumStack()
    {
        return this.numStack;
    }

    public void setNumStack(int numStack)
    {
        this.numStack = numStack;
    }

    public boolean getArpeggiate()
    {
        return this.arpeggiate;
    }

    public void setArpeggiate(boolean arpeggiate)
    {
        this.arpeggiate = arpeggiate;
    }

    public Sample[] getSamples()
    {
        return this.samples;
    }

    public GUID[] getSampleGUIDs()
    {
        return this.sampleGUIDs;
    }

    public int[] getSplitNotes()
    {
        return this.splitNotes;
    }

    public Vector2f[] getParams()
    {
        return this.params;
    }

    public byte[] getArppegio()
    {
        return this.arpeggio;
    }
}
