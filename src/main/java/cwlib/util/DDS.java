package cwlib.util;

import cwlib.enums.CellGcmEnumForGtf;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.texture.CellGcmTexture;

public class DDS {
    public static int DDS_HEADER_FLAGS_TEXTURE = 0x00001007;
    public static int DDS_HEADER_FLAGS_MIPMAP = 0x00020000;
    public static int DDS_SURFACE_FLAGS_TEXTURE = 0x00001000;
    public static int DDS_SURFACE_FLAGS_MIPMAP = 0x00400008;

    public static int DDS_FOURCC = 0x4;
    public static int DDS_RGB = 0x40;
    public static int DDS_RGBA = 0x41;
    public static int DDS_LUMINANCE = 0x00020000;
    public static int DDS_LUMINANCEA = 0x00020001;
    
    public static int[] DDSPF_DXT1 = { 0x20, DDS_FOURCC, 0x31545844, 0, 0, 0, 0, 0 };
    public static int[] DDSPF_DXT3 = { 0x20, DDS_FOURCC, 0x33545844, 0, 0, 0, 0, 0 };
    public static int[] DDSPF_DXT5 = { 0x20, DDS_FOURCC, 0x35545844, 0, 0, 0, 0, 0 };
    public static int[] DDSPF_A8R8G8B8 = { 0x20, DDS_RGBA, 0, 32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000 };
    public static int[] DDSPF_R5G6B5 = { 0x20, DDS_RGB, 0, 16, 0x0000f800, 0x000007e0, 0x0000001f, 0x00000000 };
    public static int[] DDSPF_A4R4G4B4 = { 0x20, DDS_RGBA, 0, 16, 0x00000f00, 0x000000f0, 0x0000000f, 0x0000f000 };
    public static int[] DDSPF_A16B16G16R16F = { 0x20, DDS_FOURCC, 113, 0, 0, 0, 0, 0 };
    public static int[] DDSPF_A8L8 = { 0x20, DDS_LUMINANCEA, 0, 16, 0xff, 0, 0, 0xff00 };
    public static int[] DDSPF_L8 = { 0x20, DDS_LUMINANCE, 0, 8, 0xff, 0, 0, 0 };
    public static int[] DDSPF_B8 = { 0x20, DDS_LUMINANCE, 0, 8, 0, 0, 0x000000ff, 0 };
    public static int[] DDSPF_A1R5G5B5 = { 0x20, DDS_RGBA, 0, 16, 0x00007c00, 0x000003e0, 0x0000001f, 0x00008000 };

    /**
     * Generates a DDS header.
     * @param texture Texture header data
     * @return Generated DDS header.
     */
    public static byte[] getDDSHeader(CellGcmTexture texture) {
        return DDS.getDDSHeader(
            texture.getFormat(), 
            texture.getWidth(), 
            texture.getHeight(), 
            texture.getMipCount()
        );
    }

    /**
     * Generates a DDS header.
     * @param format DDS format for PS3
     * @param width Width of texture
     * @param height Height of texture
     * @param mips Mip level count
     * @return Generated DDS header
     */
    public static byte[] getDDSHeader(CellGcmEnumForGtf format, int width, int height, int mips) {
        // For details on the DDS header structure, see:
        // https://docs.microsoft.com/en-us/windows/win32/direct3ddds/dds-header
        
        MemoryOutputStream header = new MemoryOutputStream(0x80);
        header.setLittleEndian(true);

        header.str("DDS ", 4);
        header.u32(0x7C); // dwSize
        header.u32(DDS.DDS_HEADER_FLAGS_TEXTURE | ((mips != 0) ? DDS.DDS_HEADER_FLAGS_MIPMAP : 0));
        header.u32(height);
        header.u32(width);
        header.u32(0); // dwPitchOrLinearSize
        header.u32(0); // dwDepth
        header.u32(mips);
        for (int i = 0; i < 11; ++i)
            header.u32(0); // dwReserved[11]
        
        // DDS_PIXELFORMAT
        int[] pixelFormat = null;
        switch (format) {
            case B8: pixelFormat = DDS.DDSPF_B8; break;
            case A1R5G5B5: pixelFormat = DDS.DDSPF_A1R5G5B5; break;
            case A4R4G4B4: pixelFormat = DDS.DDSPF_A4R4G4B4; break;
            case R5G6B5: pixelFormat = DDS.DDSPF_R5G6B5; break;
            case A8R8G8B8: pixelFormat = DDS.DDSPF_A8R8G8B8; break;
            case DXT1: pixelFormat = DDS.DDSPF_DXT1; break;
            case DXT3: pixelFormat = DDS.DDSPF_DXT3; break;
            case DXT5: pixelFormat = DDS.DDSPF_DXT5; break;
            default: throw new RuntimeException("Unknown or unimplemented DDS Type!");
        }
        for (int value : pixelFormat)
            header.u32(value);
        
        int surfaceFlags = DDS.DDS_SURFACE_FLAGS_TEXTURE;
        if (mips != 0) surfaceFlags |= DDS.DDS_SURFACE_FLAGS_MIPMAP;
        header.u32(surfaceFlags);
        
        header.u32(0);
        
        for (int i = 0; i < 3; ++i)
            header.u32(0); // dwReserved
        
        return header.getBuffer();
    }

    /**
     * Unswizzles pixel data
     * @param pixels Pixel data
     * @param height Height of texture
     * @param width Width of texture
     * @return Unswizzled pixels
     */
    public static int[] unswizzle(int[] pixels, int height, int width) {
        // NOTE(Aidan): For original source, see:
        // https://github.com/RPCS3/rpcs3/blob/3d49976b3c0f2d2fe5fbd9dba0419c13b389c6ba/rpcs3/Emu/RSX/rsx_utils.h
        
        int[] unswizzled = new int[pixels.length];
        
        int log2width = (int) (Math.log(width) / Math.log(2));
        int log2height = (int) (Math.log(height) / Math.log(2));
        
        int xMask = 0x55555555;
        int yMask = 0xAAAAAAAA;
        
        int limitMask = (log2width < log2height) ? log2width : log2height;
        limitMask = 1 << (limitMask << 1);
        
        
        xMask = (xMask | ~(limitMask - 1));
        yMask = (yMask & (limitMask - 1));
        
        int offsetY = 0, offsetX = 0, offsetX0 = 0, yIncr = limitMask, adv = width;
        
        for (int y = 0; y < height; ++y) {
            offsetX = offsetX0;
            for (int x = 0; x < width; ++x) {
                unswizzled[(y * adv) + x] = pixels[offsetY + offsetX];
                offsetX = (offsetX - xMask) & xMask;
            }
            offsetY = (offsetY - yMask) & yMask;
            if (offsetY == 0) offsetX0 += yIncr;
        }

        return unswizzled;
    }
}
