package cwlib.resources;

import cwlib.types.Resource;
import cwlib.util.Bytes;
import cwlib.util.DDS;
import cwlib.util.Images;
import cwlib.enums.CellGcmEnumForGtf;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.structs.texture.CellGcmTexture;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import cwlib.external.DDSReader;
import cwlib.singleton.ResourceSystem;

public class RTexture {
    private CellGcmTexture info;
    private byte[] data;
    public boolean noSRGB = false;

    private BufferedImage cached;

    public RTexture(Resource resource) { this.process(resource); }
    public RTexture(byte[] data) {
        if (data == null || data.length < 4) {
            // System.out.println("No data provided to Texture constructor");
            return;
        }

        int magic = Bytes.toIntegerBE(data);
        this.data = data;
        switch (magic) {
            case 0xffd8ffe0:
            case 0x89504e47:
                InputStream stream = new ByteArrayInputStream(data);
                try {
                    this.cached = ImageIO.read(stream);
                    stream.close();
                } catch (IOException ex) {
                    throw new SerializationException("An error occured reading BufferedImage");
                }
                return;
            case 0x44445320:
                this.cached = Images.fromDDS(data);
                return;
        }

        this.process(new Resource(data));
    }

    private void process(Resource resource) {
        this.info = resource.getTextureInfo();
        ResourceType type = resource.getResourceType();
        if (type != ResourceType.TEXTURE && type != ResourceType.GTF_TEXTURE)
            throw new SerializationException("Invalid resource provided to RTexture");
        this.data = resource.getStream().getBuffer();
        switch (resource.getSerializationType()) {
            case COMPRESSED_TEXTURE:
                if (type == ResourceType.TEXTURE) {
                    // ResourceSystem.println("Texture", "Detected COMPRESSED_TEXTURE, decompressing to DDS");
                    this.cached = Images.fromDDS(this.data);

                    byte[] footer = Arrays.copyOfRange(this.data, this.data.length - 4, this.data.length);
                    if (Bytes.toIntegerBE(footer) == 0x42554D50)
                        noSRGB = true;
                } else {
                    // ResourceSystem.println("Texture", "Detected GTF_TEXTURE, generating DDS header");
                    this.parseGTF();
                }
                break;
            case GTF_SWIZZLED:
            case GXT_SWIZZLED:
                if (type != ResourceType.GTF_TEXTURE)
                    throw new SerializationException("Invalid ResourceType in GXT/GTF swizzled load");
                // ResourceSystem.println("Texture", "Converting GXT texture to DDS.");
                this.parseGXT();
                break;
            default: throw new SerializationException("Invalid serialization type in RTexture resource!");
        }

        if (this.info != null && this.info.isBumpTexture())
            noSRGB = true;
    }

    public void parseGXT() {
        this.unswizzleCompressed();
        byte[] header = this.getDDSHeader();
        byte[] gtf = this.data;

        byte[] DDS = new byte[gtf.length + header.length];
        System.arraycopy(header, 0, DDS, 0, header.length);
        System.arraycopy(gtf, 0, DDS, header.length, gtf.length);
        
        this.data = DDS;
        this.cached = this.getImage();
    }

    /**
     * Assigns the properties of this class from the GTF resource header.
     */
    public void parseGTF() {
        byte[] header = this.getDDSHeader();
        byte[] gtf = this.data;

        byte[] DDS = new byte[gtf.length + header.length];
        System.arraycopy(header, 0, DDS, 0, header.length);
        System.arraycopy(gtf, 0, DDS, header.length, gtf.length);
        
        this.data = DDS;
        
        CellGcmEnumForGtf format = this.info.getFormat();
        if (format == CellGcmEnumForGtf.A8R8G8B8 || format == CellGcmEnumForGtf.B8 || 
            this.info.getMethod() == SerializationType.GTF_SWIZZLED || 
            this.info.getMethod() == SerializationType.GXT_SWIZZLED) 
            this.unswizzle();
        else this.cached = this.getImage();
    }

    private int getMortonNumber(int x, int y, int width, int height) {
        int logW = 31 - Integer.numberOfLeadingZeros(width);
        int logH = 31 - Integer.numberOfLeadingZeros(height);

        int d = Integer.min(logW, logH);
        int m = 0;

        for (int i = 0; i < d; ++i)
            m |= ((x & (1 << i)) << (i + 1)) | ((y & (1 << i)) << i);

        if(width < height)
            m |= ((y & ~(width  - 1)) << d);
        else
            m |= ((x & ~(height - 1)) << d);

        return m;
    }

    /**
     * Unswizzles each DXT1/5 compressed block in a Vita GXT texture.
     */
    private void unswizzleCompressed() {
        byte[] pixels = new byte[this.data.length];

        int blockWidth = 4, blockHeight = 4;
        int bpp = 4;
        if (this.info.getFormat().equals(CellGcmEnumForGtf.DXT5))
            bpp = 8;

        int base = 0;

        int width = Integer.max(this.info.getWidth(), blockWidth);
        int height = Integer.max(this.info.getHeight(), blockHeight);

        int log2width = 1 << (31 - Integer.numberOfLeadingZeros(width + (width - 1)));
        int log2height = 1 << (31 - Integer.numberOfLeadingZeros(height + (height - 1)));
        
        for (int i = 0; i < this.info.getMipCount(); ++i) {
            int w = ((width + blockWidth - 1) / blockWidth);
            int h = ((height + blockHeight - 1) / blockHeight);
            int blockSize = bpp * blockWidth * blockHeight;
    
            int log2w = 1 << (31 - Integer.numberOfLeadingZeros(w + (w - 1)));
            int log2h = 1 << (31 - Integer.numberOfLeadingZeros(h + (h - 1)));
    
            int mx = getMortonNumber(log2w - 1, 0, log2w, log2h);
            int my = getMortonNumber(0, log2h - 1, log2w, log2h);
    
            int pixelSize = blockSize / 8;
    
            int oy = 0, tgt = base;
            for (int y = 0; y < h; ++y) {
                int ox = 0;
                for (int x = 0; x < w; ++x) {
                    int offset = base + ((ox + oy) * pixelSize);
                    System.arraycopy(this.data, offset, pixels, tgt, pixelSize);
                    tgt += pixelSize;
                    ox = (ox - mx) & mx;
                }
                oy = (oy - my) & my;
            }

            base += ((bpp * log2width * log2height) / 8);

            width = width > blockWidth ? width / 2 : blockWidth;
            height = height > blockHeight ? height / 2 : blockHeight;

            log2width = log2width > blockWidth ? log2width / 2 : blockWidth;
            log2height = log2height > blockHeight ? log2height / 2 : blockHeight;
        }

        this.data = pixels;
    }

    /**
     * Unswizzles the texture's pixel data
     */
    private void unswizzle() {
        int[] pixels = DDSReader.read(this.data, DDSReader.ARGB, 0);
        pixels = DDS.unswizzle(pixels, this.info.getHeight(), this.info.getWidth());
        
        for (int i = 0; i < pixels.length; ++i) {
            int pixel = pixels[i];
            pixels[i] = (pixel & 0xff) << 24 | (pixel & 0xff00) << 8 | (pixel & 0xff0000) >> 8 | (pixel >> 24) & 0xff;
        }
        
        this.cached = new BufferedImage(this.info.getWidth(), this.info.getHeight(), BufferedImage.TYPE_INT_ARGB);
        if (this.cached != null)
            this.cached.setRGB(0, 0, this.info.getWidth(), this.info.getHeight(), pixels, 0, this.info.getWidth());
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
        if (cached == null) cached = this.getImage();
        if (cached != null)
            return Images.getImageIcon(cached, width, height);
        return null;
    }

    /**
     * Generates a DDS header from attributes of Texture instance.
     * @return Generated DDS header
     */
    public byte[] getDDSHeader() {
        //System.out.println(String.format("DDS Type: %s (%s)", Bytes.toHex(this.info.getFormat().getValue()), this.info.getFormat().name()));
        //System.out.println(String.format("Image Width: %spx", this.info.getWidth()));
        //System.out.println(String.format("Image Height: %spx", this.info.getHeight()));
        return DDS.getDDSHeader(this.info);
    }

    public CellGcmTexture getInfo() { return this.info; }
    public byte[] getData() { return this.data; }
}
