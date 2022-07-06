package cwlib.resources;

import org.joml.Vector2f;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.instrument.Sample;

public class RInstrument implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x200;

    public static final int MAX_SAMPLES = 0x8;
    public static final int MAX_PARAMS = 0x1b;
    public static final int MAX_ARPEGGIO = 0x20;
    public static final int MAX_SPLITS = 0x9;

    private Sample[] samples = new Sample[MAX_SAMPLES];
    private GUID[] sampleGUIDs = new GUID[MAX_SAMPLES];
    private int[] splitNotes = new int[MAX_SPLITS];  // KEY NUMBER (PIANO) -> https://www.inspiredacoustics.com/en/MIDI_note_numbers_and_center_frequencies 
    private int numStack = 1;
    private Vector2f[] params = new Vector2f[MAX_PARAMS];
    private byte[] arpeggio = new byte[MAX_ARPEGGIO];
    private boolean arpeggiate = false;

    public RInstrument() {
        for (int i = 0; i < MAX_SAMPLES; ++i)
            this.samples[i] = new Sample();
        for (int i = 0; i < MAX_PARAMS; ++i)
            this.params[i] = new Vector2f().zero();
        for (int i = 0; i < MAX_ARPEGGIO; ++i)
            this.arpeggio[i] = 0xf;
        this.splitNotes[0] = 87;
    }

    @SuppressWarnings("unchecked")
    @Override public RInstrument serialize(Serializer serializer, Serializable structure) {
        RInstrument inst = (structure == null) ? new RInstrument() : (RInstrument) structure;

        serializer.i32(MAX_SAMPLES);
        for (int i = 0; i < MAX_SAMPLES; ++i)
            inst.samples[i] = serializer.struct(inst.samples[i], Sample.class);

        serializer.i32(MAX_SAMPLES);
        for (int i = 0; i < MAX_SAMPLES; ++i)
            inst.sampleGUIDs[i] = serializer.guid(inst.sampleGUIDs[i]);

        serializer.i32(MAX_SPLITS);
        for (int i = 0; i < MAX_SPLITS; ++i)
            inst.splitNotes[i] = serializer.s32(inst.splitNotes[i]);
        
        inst.numStack = serializer.s32(inst.numStack);

        for (int i = 0; i < MAX_PARAMS; ++i) {
            serializer.i32(2); // Technically a float array, but it's always of length 2
            serializer.v2(inst.params[i]);
        }

        serializer.i32(MAX_ARPEGGIO);
        for (int i = 0; i < MAX_ARPEGGIO; ++i)
            inst.arpeggio[i] = serializer.i8(inst.arpeggio[i]);
        
        inst.arpeggiate = serializer.bool(inst.arpeggiate);

        return inst;
    }
    
    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
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

    public int getNumStack() { return this.numStack; }
    public void setNumStack(int numStack) { this.numStack = numStack; }

    public boolean getArpeggiate() { return this.arpeggiate; }
    public void setArpeggiate(boolean arpeggiate) { this.arpeggiate = arpeggiate; }

    public Sample[] getSamples() { return this.samples; }
    public GUID[] getSampleGUIDs() { return this.sampleGUIDs; }
    public int[] getSplitNotes() { return this.splitNotes; }
    public Vector2f[] getParams() { return this.params; }
    public byte[] getArppegio() { return this.arpeggio; }
}
