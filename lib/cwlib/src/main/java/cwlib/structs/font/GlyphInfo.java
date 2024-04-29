package cwlib.structs.font;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class GlyphInfo implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public short character;
    public short advance;
    public byte boxLeft, boxTop;
    public byte boxW, boxH;
    public int offset;
    public short nextGlyph;

    public transient int cacheX, cacheY;

    @Override
    public void serialize(Serializer serializer)
    {
        character = serializer.i16(character);
        advance = serializer.i16(advance);
        boxLeft = serializer.i8(boxLeft);
        boxTop = serializer.i8(boxTop);
        boxW = serializer.i8(boxW);
        boxH = serializer.i8(boxH);
        offset = serializer.i32(offset);
        nextGlyph = serializer.i16(nextGlyph);
    }

    @Override
    public int getAllocatedSize()
    {
        return GlyphInfo.BASE_ALLOCATION_SIZE;
    }
}
