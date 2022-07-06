package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.instrument.Note;
import cwlib.types.data.ResourceDescriptor;

public class PInstrument implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public ResourceDescriptor instrument;
    public String name;
    public int color, loops = 1, key, scale;
    public float level = 1.0f, pan = 0.5f, echoSend, reverbSend;
    public short scrollX, scrollY, cursorX, cursorY;
    public Note[] notes;
    public ResourceDescriptor icon;
    
    @SuppressWarnings("unchecked")
    @Override public PInstrument serialize(Serializer serializer, Serializable structure) {
        PInstrument instrument = (structure == null) ? new PInstrument() : (PInstrument) structure;

        instrument.instrument = serializer.resource(instrument.instrument, ResourceType.INSTRUMENT);

        instrument.name = serializer.wstr(instrument.name);

        instrument.color = serializer.i32(instrument.color);
        instrument.loops = serializer.i32(instrument.loops);
        instrument.key = serializer.i32(instrument.key);
        instrument.scale = serializer.i32(instrument.scale);

        instrument.level = serializer.f32(instrument.level);
        instrument.pan = serializer.f32(instrument.pan);
        instrument.echoSend = serializer.f32(instrument.echoSend);
        instrument.reverbSend = serializer.f32(instrument.reverbSend);

        instrument.scrollX = serializer.i16(instrument.scrollX);
        instrument.scrollY = serializer.i16(instrument.scrollY);
        instrument.cursorX = serializer.i16(instrument.cursorX);
        instrument.cursorY = serializer.i16(instrument.cursorY);

        instrument.notes = serializer.array(instrument.notes, Note.class);

        instrument.icon = serializer.resource(instrument.icon, ResourceType.TEXTURE);

        return instrument;
    }

    @Override public int getAllocatedSize() {
        int size = PInstrument.BASE_ALLOCATION_SIZE;
        if (this.notes != null) size += (this.notes.length * Note.BASE_ALLOCATION_SIZE);
        return size;
    }
}
