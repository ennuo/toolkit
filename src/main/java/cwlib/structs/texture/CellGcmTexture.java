package cwlib.structs.texture;

import cwlib.enums.CellGcmEnumForGtf;
import cwlib.enums.SerializationType;
import cwlib.external.DDSReader;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

/**
 * Represents GTF texture information for PS3.
 */
public final class CellGcmTexture {
    private final CellGcmEnumForGtf format;
    private final byte mipmap;
    private final byte dimension;
    private final byte cubemap;
    private final int remap;
    private final short width, height, depth;
    private final byte location;
    private final byte flags;
    private final int pitch, offset;
    private SerializationType method = SerializationType.COMPRESSED_TEXTURE;

    public CellGcmTexture(byte[] dds, boolean noSRGB) {
        int type = DDSReader.getType(dds);
        switch (type) {
            case 0xFF: this.format = CellGcmEnumForGtf.B8; break;
            case 1146639409: this.format = CellGcmEnumForGtf.DXT1; break;
            case 1146639411: this.format = CellGcmEnumForGtf.DXT3; break;
            case 1146639413: this.format = CellGcmEnumForGtf.DXT5; break;
            case 65538: this.format = CellGcmEnumForGtf.A1R5G5B5; break;
            case 196610: this.format = CellGcmEnumForGtf.A4R4G4B4; break;
            case 327682: this.format = CellGcmEnumForGtf.R5G5B5; break;
            case 196612: this.format = CellGcmEnumForGtf.A8R8G8B8; break;
            default: throw new IllegalArgumentException("Invalid format!");
        }
        this.mipmap = (byte) DDSReader.getMipmap(dds);
        this.dimension = 2;
        this.cubemap = 0;
        this.remap = 0xaae4;
        this.width = (short) DDSReader.getWidth(dds);
        this.height = (short) DDSReader.getHeight(dds);
        this.depth = 1;
        this.location = 0;
        this.flags = (byte) ((noSRGB) ? 0x1 : 0x0);
        this.pitch = 0;
        this.offset = 0;
    }

    public CellGcmTexture(CellGcmEnumForGtf format, short width, short height, byte mips, boolean noSRGB) {
        this.format = format;
        this.mipmap = mips;
        this.dimension = 2;
        this.cubemap = 0;
        this.remap = 0xaae4;
        this.width = width;
        this.height = height;
        this.depth = 1;
        this.location = 0;
        this.flags = (byte) ((noSRGB) ? 0x1 : 0x0);
        this.pitch = 0;
        this.offset = 0;
    }
    
    /**
     * Deserializes TextureInfo from stream.
     * @param stream Stream to read texture info from
     * @param method Texture type
     */
    public CellGcmTexture(MemoryInputStream stream, SerializationType method) {
        this.method = method;
        
        // I don't want to grab the full structure for this right now,
        // and it's not really necessary for anything either, so I guess I'll finish
        // it at some other point

        if (method == SerializationType.GXT_SWIZZLED)
            stream.seek(0x14);

        this.format = CellGcmEnumForGtf.fromValue(stream.u8());
        this.mipmap = stream.i8();
        this.dimension = stream.i8();
        // if dimension > 2, MARK TEXTURE AS VOL(UME)TEX
        this.cubemap = stream.i8();
        this.remap = stream.i32(true);
        this.width = stream.i16();
        this.height = stream.i16();
        this.depth = stream.i16();
        this.location = stream.i8();

        // If (padding & 0x1) != 0, MARK TEXTURE AS BUMPTEX | NOSRGB_TEX
        // if (padding & 0x2) != 0, MARK TEXTURE as 0x20000000
        // if (padding & 0x4) != 0, MARK TEXTURE AS 0x40000000
        this.flags = stream.i8(); // padding

        this.pitch = stream.i32(true);
        this.offset = stream.i32(true);
    }

    /**
     * Writes this header to an output stream.
     * @param stream Memory output stream
     */
    public void write(MemoryOutputStream stream) {
        stream.u8(this.format.getValue());
        stream.i8(this.mipmap);
        stream.i8(this.dimension);
        stream.i8(this.cubemap);
        stream.i32(this.remap, true);
        stream.i16(this.width);
        stream.i16(this.height);
        stream.i16(this.depth);
        stream.i8(this.location);
        stream.i8(this.flags);
        stream.i32(this.pitch, true);
        stream.i32(this.offset, true);
    }

    public CellGcmEnumForGtf getFormat() { return this.format; }
    public int getMipCount() { return this.mipmap & 0xFF; }
    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }
    public int getDepth() { return this.depth; }
    public SerializationType getMethod() { return this.method; }

    public boolean isBumpTexture() { return (this.flags & 0x1) != 0; }
    public boolean isVolumeTexture() { return (this.dimension > 2); }

    public void setMethod(SerializationType method) {
        this.method = method;
    }
}
