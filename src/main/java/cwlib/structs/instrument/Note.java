package cwlib.structs.instrument;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryOutputStream;

public class Note implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    // Each type is actually a byte, but that's pretty inconvenient, since
    // there's no unsigned type
    public int x = 0, y = 0, volume = 0x60, timbre = 0x40;
    public boolean triplet, end;

    public Note(){};
    public Note(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @SuppressWarnings("unchecked")
    @Override public Note serialize(Serializer serializer, Serializable structure) {
        Note note = (structure == null) ? new Note() : (Note) structure;

        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            stream.u8((note.x & 0x7f) | ((note.triplet) ? 0x80 : 0x0));
            stream.u8((note.y & 0x7f) | ((note.end) ? 0x80 : 0x0));
            stream.u8(note.volume & 0xff);
            stream.u8(note.timbre & 0xff);
            return note;
        }

        byte[] struct = serializer.getInput().bytes(4);
        
        note.triplet = (struct[0] >> 0x7) != 0;
        note.x = (struct[0] & 0x7f);

        note.end = (struct[1] >> 0x7) != 0;
        note.y = (struct[1] & 0x7f);

        note.volume = struct[0x2];
        note.timbre = struct[0x3];

        return note;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
