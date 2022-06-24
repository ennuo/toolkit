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
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import cwlib.external.DDSReader;

public class RTexture {
    private CellGcmTexture info;
    private byte[] data;

    private BufferedImage cached;

    public RTexture(Resource resource) { this.process(resource); }
    public RTexture(byte[] data) {
        if (data == null || data.length < 4) {
            System.out.println("No data provided to Texture constructor");
            return;
        }

        int magic = Bytes.toIntegerBE(data);
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
                    System.out.println("Decompressing TEX to DDS");
                    this.cached = Images.fromDDS(this.data);
                } else {
                    System.out.println("Converting GTF texture to DDS");
                    this.parseGTF();
                }
                break;
            case GTF_SWIZZLED:
            case GXT_SWIZZLED:
                if (type != ResourceType.GTF_TEXTURE)
                    throw new SerializationException("Invalid ResourceType in GXT/GTF swizzled load");
                System.out.println("Converting GXT texture to DDS.");
                System.out.println("Unswizzling isn't correctly implemented, so this will probably be broken!");
                this.parseGTF();
                break;
            default: throw new SerializationException("Invalid serialization type in RTexture resource!");
        }
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

        // TODO(Aidan): The unswizzling process is different for Vita,
        // or I'm assuming the wrong DDS type, either way, the textures
        // come out corrupted regardless and this should be looked into.
        CellGcmEnumForGtf format = this.info.getFormat();
        if (format == CellGcmEnumForGtf.A8R8G8B8 || format == CellGcmEnumForGtf.B8 || 
            this.info.getMethod() == SerializationType.GTF_SWIZZLED || 
            this.info.getMethod() == SerializationType.GXT_SWIZZLED) 
            this.unswizzle();
        else this.cached = this.getImage();
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
        System.out.println(String.format("DDS Type: %s (%s)", Bytes.toHex(this.info.getFormat().getValue()), this.info.getFormat().name()));
        System.out.println(String.format("Image Width: %spx", this.info.getWidth()));
        System.out.println(String.format("Image Height: %spx", this.info.getHeight()));
        return DDS.getDDSHeader(this.info);
    }

    public CellGcmTexture getInfo() { return this.info; }
}
