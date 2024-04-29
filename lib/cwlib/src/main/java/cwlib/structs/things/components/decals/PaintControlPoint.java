package cwlib.structs.things.components.decals;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Squished paint positional data.
 */
public class PaintControlPoint implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    public byte x, y, startRadius, endRadius;

    @Override
    public void serialize(Serializer serializer)
    {
        x = serializer.i8(x);
        y = serializer.i8(y);
        startRadius = serializer.i8(startRadius);
        endRadius = serializer.i8(endRadius);
    }

    @Override
    public int getAllocatedSize()
    {
        return PaintControlPoint.BASE_ALLOCATION_SIZE;
    }
}