package cwlib.structs.texture;

import cwlib.enums.SerializationType;
import cwlib.io.streams.MemoryInputStream;

public class CellGcmTexture {
    public int format;
    public int mipmap;
    public byte dimension;
    public byte cubemap;
    public int remap;
    public short width, height, depth;
    public byte location;
    public int pitch, offset;
    
    public CellGcmTexture(MemoryInputStream data, SerializationType method) {
        
        // NOTE(Aidan): I don't want to grab the full structure for this right now,
        // and it's not really necessary for anything either, so I guess I'll finish
        // it at some other point
        
        if (method == SerializationType.GXT_EXTENDED)
            data.offset += 0x14;
        
        this.getTextureInfo(data);
    }
    
    private void getTextureInfo(MemoryInputStream data) {
        this.format = data.i8() & 0xFF;
        this.mipmap = data.i8() & 0xFF;
        this.dimension = data.i8();
        this.cubemap = data.i8();
        this.remap = data.i32f();
        this.width = data.i16();
        this.height = data.i16();
        this.depth = data.i16();
        this.location = data.i8();
        data.i8(); // padding
        this.pitch = data.i32f();
        this.offset = data.i32f();
    }
}
