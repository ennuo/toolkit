package cwlib.structs.instrument;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryOutputStream;

public class Note implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    // Each type is actually a byte, but that's pretty inconvenient, since
    // there's no unsigned type
    public int x = 0, y = 0, volume = 0x60, timbre = 0x40;
    public boolean triplet, end;

    public Note() { }

    public Note(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        if (serializer.isWriting())
        {
            MemoryOutputStream stream = serializer.getOutput();
            stream.u8((x & 0x7f) | ((triplet) ? 0x80 : 0x0));
            stream.u8((y & 0x7f) | ((end) ? 0x80 : 0x0));
            stream.u8(volume & 0xff);
            stream.u8(timbre & 0xff);
            return;
        }

        byte[] struct = serializer.getInput().bytes(4);

        triplet = (struct[0] >> 0x7) != 0;
        x = (struct[0] & 0x7f);

        end = (struct[1] >> 0x7) != 0;
        y = (struct[1] & 0x7f);

        volume = struct[0x2];
        timbre = struct[0x3];
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
