package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PTransition implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int colorIndex;

    @GsonRevision(lbp3 = true, min = 0x1a)
    public String label;

    @GsonRevision(lbp3 = true, min = 0x19)
    public boolean enabled;

    @GsonRevision(lbp3 = true, min = 0x17d)
    public boolean showColor, playAudio;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        colorIndex = serializer.s32(colorIndex);

        if (subVersion <= 0xf)
            serializer.i32(0);

        if (subVersion >= 0x1a)
            label = serializer.wstr(label);

        if (subVersion > 0x18)
            enabled = serializer.bool(enabled);

        if (subVersion >= 0x14e && subVersion < 0x156)
            serializer.i32(0);

        if (subVersion > 0x17c)
        {
            showColor = serializer.bool(showColor);
            playAudio = serializer.bool(playAudio);
        }
    }


    @Override
    public int getAllocatedSize()
    {
        int size = PTransition.BASE_ALLOCATION_SIZE;
        if (this.label != null)
            size += (this.label.length() * 0x2);
        return size;
    }
}
