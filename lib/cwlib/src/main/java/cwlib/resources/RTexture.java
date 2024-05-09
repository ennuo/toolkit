package cwlib.resources;

import cwlib.types.SerializedResource;
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

public class RTexture
{
    /**
     * GCM texture information.
     */
    private CellGcmTexture info;

    /**
     * Original texture data stored in this texture.
     */
    private byte[] imageData;

    /**
     * DDS texture data, from either conversion or compressed texture files.
     */
    private byte[] ddsData;

    /**
     * Whether the texture contains non-color data.
     */
    public boolean noSRGB = false;

    /**
     * Cached texture image.
     */
    private BufferedImage cachedImage;

    public RTexture(SerializedResource resource)
    {
        this.process(resource);
    }

    public RTexture(byte[] data)
    {
        if (data == null || data.length < 4)
            throw new IllegalArgumentException("Texture data isn't valid!");
        
        int magic = Bytes.toIntegerBE(data);
        this.imageData = data;
        switch (magic)
        {
            case 0xffd8ffe0:
            case 0x89504e47:
                InputStream stream = new ByteArrayInputStream(data);
                try
                {
                    this.cachedImage = ImageIO.read(stream);
                    stream.close();
                }
                catch (IOException ex)
                {
                    throw new SerializationException("An error occured reading BufferedImage");
                }

                return;
            case 0x44445320:
                this.cachedImage = Images.fromDDS(data);
                return;
        }

        this.process(new SerializedResource(data));
    }

    private void process(SerializedResource resource)
    {
        this.info = resource.getTextureInfo();
        ResourceType type = resource.getResourceType();
        if (type != ResourceType.TEXTURE && type != ResourceType.GTF_TEXTURE)
            throw new SerializationException("Invalid resource provided to RTexture");
        this.imageData = resource.getStream().getBuffer();
        switch (resource.getSerializationType())
        {
            case COMPRESSED_TEXTURE:
                if (type == ResourceType.TEXTURE)
                {
                    this.ddsData = this.imageData;
                    this.cachedImage = Images.fromDDS(this.imageData);

                    // Compressed textures have a 4-byte footer that identifies whether it's
                    // a bump, volume, or srgb texture.
                    byte[] footer = Arrays.copyOfRange(this.imageData, this.imageData.length - 4, this.imageData.length);
                    if (Bytes.toIntegerBE(footer) == 0x42554D50)
                        noSRGB = true;
                }
                else this.parseGTF();
                break;
            case GTF_SWIZZLED:
            case GXT_SWIZZLED:
                if (type != ResourceType.GTF_TEXTURE)
                    throw new SerializationException("Invalid ResourceType in GXT/GTF " +
                                                     "swizzled " +
                                                     "load");
                this.parseGXT();
                break;
            default:
                throw new SerializationException("Invalid serialization type in RTexture " +
                                                 "resource!");
        }

        if (this.info != null && this.info.isBumpTexture())
            noSRGB = true;
    }

    public void parseGXT()
    {
        byte[] header = this.getDDSHeader();
        byte[] gtf = this.imageData;
        
        if (info.getFormat().isDXT())
            gtf = DDS.unswizzleGxtCompressed(info, this.imageData);
    
        byte[] DDS = new byte[gtf.length + header.length];
        System.arraycopy(header, 0, DDS, 0, header.length);
        System.arraycopy(gtf, 0, DDS, header.length, gtf.length);

        this.ddsData = DDS;
        this.cachedImage = this.getImage();
    }

    /**
     * Assigns the properties of this class from the GTF resource header.
     */
    public void parseGTF()
    {
        CellGcmEnumForGtf format = this.info.getFormat();

        byte[] gtf = this.imageData;
        if (!format.isDXT() || this.info.getMethod() == SerializationType.GTF_SWIZZLED || this.info.getMethod() == SerializationType.GXT_SWIZZLED)
            gtf = DDS.convertSwizzleGtf(info, this.imageData, true);

        byte[] header = this.getDDSHeader();
        byte[] DDS = new byte[gtf.length + header.length];
        System.arraycopy(header, 0, DDS, 0, header.length);
        System.arraycopy(gtf, 0, DDS, header.length, gtf.length);

        this.ddsData = DDS;        
        this.cachedImage = this.getImage();
    }


    /**
     * Convert texture to BufferedImage
     *
     * @return Converted texture
     */
    public BufferedImage getImage()
    {
        if (this.cachedImage != null)
            return this.cachedImage;
        return Images.fromDDS(this.ddsData);
    }

    /**
     * Scales texture to 320x320 and creates an ImageIcon.
     *
     * @return 320x320 ImageIcon of Texture
     */
    public ImageIcon getImageIcon()
    {
        return this.getImageIcon(320, 320);
    }

    /**
     * Scales texture to specified W/H and creates an ImageIcon.
     *
     * @param width  Desired width
     * @param height Desired height
     * @return Scaled ImageIcon of Texture
     */
    public ImageIcon getImageIcon(int width, int height)
    {
        if (cachedImage == null) cachedImage = this.getImage();
        if (cachedImage != null)
            return Images.getImageIcon(cachedImage, width, height);
        return null;
    }

    /**
     * Generates a DDS header from attributes of Texture instance.
     *
     * @return Generated DDS header
     */
    public byte[] getDDSHeader()
    {
        return DDS.getDDSHeader(this.info);
    }

    public CellGcmTexture getInfo()
    {
        return this.info;
    }

    public byte[] getImageData()
    {
        return this.imageData;
    }

    public byte[] getDDSFileData()
    {
        return this.ddsData;
    }
}
