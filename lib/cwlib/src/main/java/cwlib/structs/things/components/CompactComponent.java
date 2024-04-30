package cwlib.structs.things.components;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class CompactComponent implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public Thing thing;
    public float x, y, angle, scaleX = 1, scaleY = 1;
    public boolean flipped;

    @Override
    public void serialize(Serializer serializer)
    {
        thing = serializer.thing(thing);
        x = serializer.f32(x);
        y = serializer.f32(y);
        angle = serializer.f32(angle);
        scaleX = serializer.f32(scaleX);
        scaleY = serializer.f32(scaleY);
        flipped = serializer.bool(flipped);
    }

    @Override
    public int getAllocatedSize()
    {
        return CompactComponent.BASE_ALLOCATION_SIZE;
    }
}
