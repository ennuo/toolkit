package cwlib.structs.things.parts;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.instrument.Note;
import cwlib.types.data.ResourceDescriptor;

public class PInstrument implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public ResourceDescriptor instrument;

    @GsonRevision(min = 0x35b)
    public String name;

    public int color, loops = 1, key, scale;
    public float level = 1.0f, pan = 0.5f, echoSend, reverbSend;

    @GsonRevision(min = 0x389)
    public short scrollX, scrollY, cursorX, cursorY;

    public ArrayList<Note> notes = new ArrayList<>();

    @GsonRevision(min = 0x379)
    public ResourceDescriptor icon;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();
        instrument = serializer.resource(instrument, ResourceType.INSTRUMENT);

        if (version >= 0x35b)
            name = serializer.wstr(name);

        color = serializer.i32(color);
        loops = serializer.i32(loops);
        key = serializer.i32(key);
        scale = serializer.i32(scale);

        level = serializer.f32(level);
        pan = serializer.f32(pan);
        echoSend = serializer.f32(echoSend);
        reverbSend = serializer.f32(reverbSend);

        if (version >= 0x389)
        {
            scrollX = serializer.i16(scrollX);
            scrollY = serializer.i16(scrollY);
            cursorX = serializer.i16(cursorX);
            cursorY = serializer.i16(cursorY);
        }

        notes = serializer.arraylist(notes, Note.class);

        if (version >= 0x379)
            icon = serializer.resource(icon, ResourceType.TEXTURE);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PInstrument.BASE_ALLOCATION_SIZE;
        if (this.notes != null) size += (this.notes.size() * Note.BASE_ALLOCATION_SIZE);
        return size;
    }
}
