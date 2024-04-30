package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class PTagSynchroniser implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int type, colorIndex;

    @GsonRevision(min = 0x2dc)
    public String name;

    public float radius, minRadius, scaleX, scaleY;
    public boolean useLayers, visible;

    @Override
    public void serialize(Serializer serializer)
    {
        type = serializer.s32(type);

        colorIndex = serializer.s32(colorIndex);
        if (serializer.getRevision().getVersion() >= 0x2dc)
            name = serializer.wstr(name);

        radius = serializer.f32(radius);
        minRadius = serializer.f32(minRadius);
        scaleX = serializer.f32(scaleX);
        scaleY = serializer.f32(scaleY);

        useLayers = serializer.bool(useLayers);
        visible = serializer.bool(visible);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PTagSynchroniser.BASE_ALLOCATION_SIZE;
        if (this.name != null) size += (this.name.length() * 0x2);
        return size;
    }
}
