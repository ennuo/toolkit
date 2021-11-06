package ennuo.craftworld.resources;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.utilities.Images;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.resources.enums.Metadata.CompressionType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.npe.dds.DDSReader;

public class Texture {
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
    
    private int type, width, height, mipCount;
    public byte[] data;

    public BufferedImage cached;

    public boolean parsed = true;

    public Texture(byte[] data) {
        if (data == null || data.length < 4) {
            System.out.println("No data provided to Texture constructor");
            return;
        }
        
        Resource resource = new Resource(data);
        int magic = resource.i24();
        resource.offset = 0;
        switch (magic) {
            case 0xffd8ff:
            case 0x89504e:
                InputStream stream = new ByteArrayInputStream(data);
                try {
                    this.cached = ImageIO.read(stream);
                    stream.close();
                    this.parsed = true;
                } catch (IOException ex) {
                    System.err.println("An error occured reading BufferedImage");
                    this.parsed = false;
                }
                return;
            case 0x444453:
                this.cached = Images.fromDDS(data);
                this.parsed = true;
                return;
        }

        if (resource.type == null) this.parsed = false;
        else switch (resource.type) {
            case LEGACY_TEXTURE:
                System.out.println("Decompressing TEX to DDS");
                this.data = resource.decompress(true);
                this.cached = Images.fromDDS(this.data);
                break;
            case GTF_TEXTURE:
                System.out.println("Converting GTF texture to DDS");
                this.parseGTF(resource);
                break;
            case GXT_SIMPLE_TEXTURE:
            case GXT_EXTENDED_TEXTURE:
                System.out.println("Converting GXT texture to DDS.");
                System.out.println("Unswizzling isn't correctly implemented, so this will probably be broken!");
                this.parseGXT(resource);
                break;
            default:
                this.parsed = false;
                break;
        }
    }
    
    /**
     * Assigns the properties of this class from the GXT resource header.
     * @param gxt GXT resource instance
     */
    public void parseGXT(Resource gxt) {
        if (gxt.type == CompressionType.GXT_SIMPLE_TEXTURE) {
            gxt.seek(0x4); // Skip magic
            this.type = gxt.i8() & 0xFF;
            this.mipCount = gxt.i8() & 0xFF;
            gxt.seek(0xC); // Skip to W/H
        } else {
            gxt.seek(0x18); // Skip non-necessary information
            this.type = gxt.i8() & 0xFF;
            this.mipCount = gxt.i8() & 0xFF;
            gxt.seek(0x20); // Skip to W/H
        }
        this.width = gxt.i16();
        this.height = gxt.i16();
        byte[] header = this.getDDSHeader();
        
        gxt.decompress(true);
        
        byte[] DDS = new byte[gxt.data.length + header.length];
        System.arraycopy(header, 0, DDS, 0, header.length);
        System.arraycopy(gxt.data, 0, DDS, header.length, gxt.length);
        
        this.data = DDS;
        
        // TODO(Jun): The unswizzling process is different for Vita,
        // or I'm assuming the wrong DDS type, either way, the textures
        // come out corrupted regardless and this should be looked into.
        
        this.unswizzle();
    }

    /**
     * Assigns the properties of this class from the GTF resource header.
     * @param gtf GTF resource instance
     */
    public void parseGTF(Resource gtf) {
        gtf.seek(4); // Skip the magic header.
        this.type = gtf.i8() & 0xFF;
        this.mipCount = gtf.i8() & 0xFF;
        gtf.forward(6); // We don't need either dimension or remap fields
        this.width = gtf.i16();
        this.height = gtf.i16();
        byte[] header = this.getDDSHeader();
        
        gtf.decompress(true);
        
        byte[] DDS = new byte[gtf.data.length + header.length];
        System.arraycopy(header, 0, DDS, 0, header.length);
        System.arraycopy(gtf.data, 0, DDS, header.length, gtf.length);
        
        this.data = DDS;
        if (this.type == 0x85 || this.type == 0x81) 
            this.unswizzle();
        else this.cached = this.getImage();
    }

    /**
     * Unswizzles the texture's pixel data
     */
    public void unswizzle() {
        int[] pixels = DDSReader.read(this.data, DDSReader.ARGB, 0);
        pixels = this.unswizzleData(pixels);
        this.cached = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        if (this.cached != null)
            this.cached.setRGB(0, 0, this.width, this.height, pixels, 0, this.width);
    }

    /**
     * Unswizzles pixel data.
     * @param swizzled Swizzled pixels
     * @return Unswizzled pixels
     */
    public int[] unswizzleData(int[] swizzled) {
        
        // NOTE(Jun): For original source, see:
        // https://github.com/RPCS3/rpcs3/blob/3d49976b3c0f2d2fe5fbd9dba0419c13b389c6ba/rpcs3/Emu/RSX/rsx_utils.h
        
        int[] unswizzled = new int[swizzled.length];
        
        int log2width = (int) (Math.log(this.width) / Math.log(2));
        int log2height = (int) (Math.log(this.height) / Math.log(2));
        
        int xMask = 0x55555555;
	int yMask = 0xAAAAAAAA;
        
        int limitMask = (log2width < log2height) ? log2width : log2height;
        limitMask = 1 << (limitMask << 1);
        
        
        xMask = (xMask | ~(limitMask - 1));
        yMask = (yMask & (limitMask - 1));
        
        int offsetY = 0, offsetX = 0, offsetX0 = 0, yIncr = limitMask, adv = this.width;
        
        for (int y = 0; y < this.height; ++y) {
            offsetX = offsetX0;
            for (int x = 0; x < this.width; ++x) {
                unswizzled[(y * adv) + x] = swizzled[offsetY + offsetX];
                offsetX = (offsetX - xMask) & xMask;
            }
            offsetY = (offsetY - yMask) & yMask;
            if (offsetY == 0) offsetX0 += yIncr;
        }

        return unswizzled;
    }

    /**
     * Convert texture to BufferedImage
     * @return Converted texture
     */
    public BufferedImage getImage() {
        if (this.cached != null) 
            return this.cached;
        return Images.fromDDS(this.data);
    }

    /**
     * Scales texture to 320x320 and creates an ImageIcon.
     * @return 320x320 ImageIcon of Texture
     */
    public ImageIcon getImageIcon() { return this.getImageIcon(320, 320); }
    
    /**
     * Scales texture to specified W/H and creates an ImageIcon.
     * @param width Desired width
     * @param height Desired height
     * @return Scaled ImageIcon of Texture
     */
    public ImageIcon getImageIcon(int width, int height) {
        if (cached == null)
            cached = getImage();
        if (cached != null)
            return Images.getImageIcon(cached, width, height);
        else return null;
    }

    /**
     * Generates a DDS header.
     * @param format Texture type (CellGcmEnumForGtf)
     * @param width Width of DDS image
     * @param height Height of DDS image
     * @param mips Number of mipmaps in DDS image
     * @return Generated DDS header.
     */
    public static byte[] getDDSHeader(int format, int width, int height, int mips) {
        // NOTE(Jun): For details on the DDS header structure, see:
        // https://docs.microsoft.com/en-us/windows/win32/direct3ddds/dds-header
        
        Output header = new Output(0x80);
        header.str("DDS ");
        header.u32LE(0x7C); // dwSize
        header.u32LE(Texture.DDS_HEADER_FLAGS_TEXTURE | ((mips != 0) ? Texture.DDS_HEADER_FLAGS_MIPMAP : 0));
        header.u32LE(height);
        header.u32LE(width);
        header.u32LE(0); // dwPitchOrLinearSize
        header.u32LE(0); // dwDepth
        header.u32LE(mips + 1);
        for (int i = 0; i < 11; ++i)
            header.u32LE(0); // dwReserved[11]
        
        // DDS_PIXELFORMAT
        int[] pixelFormat = null;
        switch (format) {
            case 0x81: pixelFormat = Texture.DDSPF_B8; break;
            case 0x82: pixelFormat = Texture.DDSPF_A1R5G5B5; break;
            case 0x83: pixelFormat = Texture.DDSPF_A4R4G4B4; break;
            case 0x84: pixelFormat = Texture.DDSPF_R5G6B5; break;
            case 0x85: pixelFormat = Texture.DDSPF_A8R8G8B8; break;
            case 0x86: pixelFormat = Texture.DDSPF_DXT1; break;
            case 0x87: pixelFormat = Texture.DDSPF_DXT3; break;
            case 0x88: pixelFormat = Texture.DDSPF_DXT5; break;
            default: throw new Error("Unknown or unimplemented DDS Type!");
        }
        for (int value : pixelFormat)
            header.u32LE(value);
        
        int surfaceFlags = Texture.DDS_SURFACE_FLAGS_TEXTURE;
        if (mips != 0) surfaceFlags |= Texture.DDS_SURFACE_FLAGS_MIPMAP;
        header.u32LE(surfaceFlags);
        
        header.u32LE(0);
        
        for (int i = 0; i < 3; ++i)
            header.u32LE(0); // dwReserved
        
        return header.buffer;
    }

    /**
     * Generates a DDS header from attributes of Texture instance.
     * @return Generated DDS header
     */
    public byte[] getDDSHeader() {
        System.out.println(String.format("DDS Type: %s (%s)", Bytes.toHex(this.type), this.getDDSType()));
        System.out.println(String.format("Image Width: %spx", this.width));
        System.out.println(String.format("Image Height: %spx", this.height));
        return this.getDDSHeader(this.type, this.width, this.height, this.mipCount);
    }

    /**
     * Gets texture type name from CellGcmEnumForGtf.
     * @return Name of texture type
     */
    public String getDDSType() {
        switch (this.type) {
            case 0x81: return "B8";
            case 0x82: return "A1R5G5B5";
            case 0x83: return "A4R4G4B4";
            case 0x84: return "R5G6B5";
            case 0x85: return "A8R8G8B8";
            case 0x86: return "DXT1";
            case 0x87: return "DXT3";
            case 0x88: return "DXT5";
        }
        return "UNKNOWN";
    }
}
