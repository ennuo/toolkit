package cwlib.structs.font;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class GlyphInfo implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public short character;
    public short advance;
    public byte boxLeft, boxTop;
    public byte boxW, boxH;
    public int offset;
    public short nextGlyph;

    public transient int cacheX, cacheY;

    @SuppressWarnings("unchecked")
    @Override public GlyphInfo serialize(Serializer serializer, Serializable structure) {
        GlyphInfo info = (structure == null) ? new GlyphInfo() : (GlyphInfo) structure;

        info.character = serializer.i16(info.character);
        info.advance = serializer.i16(info.advance);
        info.boxLeft = serializer.i8(info.boxLeft);
        info.boxTop = serializer.i8(info.boxTop);
        info.boxW = serializer.i8(info.boxW);
        info.boxH = serializer.i8(info.boxH);
        info.offset = serializer.i32(info.offset);
        info.nextGlyph = serializer.i16(info.nextGlyph);

        return info;
    }

    @Override public int getAllocatedSize() {
        return GlyphInfo.BASE_ALLOCATION_SIZE;
    }
}
