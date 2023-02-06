package cwlib.util;

import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.types.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.streams.MemoryInputStream;
import gr.zdimensions.jsquish.Squish;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import cwlib.enums.CellGcmEnumForGtf;
import cwlib.external.DDSReader;

import org.imgscalr.Scalr;

public class Images {
    private static int toNearest(int x) {
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x ^ (x >> 1);
    }

    public static BufferedImage toBump(BufferedImage image) {
        Color c = new Color(image.getRGB(0, 0), true);
        boolean isNormal = !(c.getRed() == c.getGreen() && c.getRed() == c.getBlue());
        if (isNormal) {
            for (int x = 0; x < image.getWidth(); ++x) {
                for (int y = 0; y < image.getHeight(); ++y) {
                    c = new Color(image.getRGB(x, y), true);
                    int green = c.getGreen();
                    int red = c.getRed();
                    image.setRGB(x, y, new Color(
                        green,
                        green,
                        green,
                        255 - red
                    ).getRGB());
                }
            }
        }
        return image;
    }
    
    private static byte[] getRGBA(BufferedImage image) {
        int[] ARGB = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        byte[] RGBA = new byte[ARGB.length * 4];
        for (int i = 0; i < ARGB.length; ++i) {
            RGBA[(4 * i) + 0] = (byte) ((ARGB[i] >> 16) & 0xff);
            RGBA[(4 * i) + 1] = (byte) ((ARGB[i] >> 8) & 0xff);
            RGBA[(4 * i) + 2] = (byte) ((ARGB[i]) & 0xff);
            RGBA[(4 * i) + 3] = (byte) ((ARGB[i] >> 24) & 0xff);
        }
        return RGBA;
    }


    private static byte[] toDDS(BufferedImage image, Squish.CompressionType type, boolean generateMips) {
        int width = toNearest(image.getWidth());
        int height = toNearest(image.getHeight());
        
        int originalWidth = width, originalHeight = height;

        CellGcmEnumForGtf format = null;
        switch (type) {
            case DXT1: format = CellGcmEnumForGtf.DXT1; break;
            case DXT3: format = CellGcmEnumForGtf.DXT3; break;
            case DXT5: format = CellGcmEnumForGtf.DXT5; break;
        }

        if (image.getWidth() != width || image.getHeight() != height)
            image = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width, height);
        byte[] dds = Squish.compressImage(getRGBA(image), width, height, null, type);

        int mipCount = 1;
        if (generateMips) {
            while (true) {
                width = toNearest(width - 1);
                height = toNearest(height - 1);
                image = Scalr.resize(image, Scalr.Method.AUTOMATIC, width, height);
                dds = Bytes.combine(dds, Squish.compressImage(getRGBA(image), width, height, null, type));
                mipCount += 1;
                if (width == 1|| height == 1) break;
            }
        }
        
        return Bytes.combine(
            DDS.getDDSHeader(format, originalWidth, originalHeight, mipCount), 
            dds
        );
    }

    public static byte[] toGTF(BufferedImage image, Squish.CompressionType type, boolean noSRGB, boolean generateMips) {
        byte[] dds = toDDS(image, type, generateMips);
        CellGcmTexture info = new CellGcmTexture(dds, noSRGB);
        dds = Arrays.copyOfRange(dds, 0x80, dds.length);
        return Resource.compress(new SerializationData(dds, info));
    }

    public static byte[] toTEX(BufferedImage image, Squish.CompressionType type, boolean noSRGB, boolean generateMips) {
        byte[] dds = toDDS(image, type, generateMips);
        if (noSRGB)
            dds = Bytes.combine(dds, "BUMP".getBytes());
        return Resource.compress(new SerializationData(dds));
    }

    public static BufferedImage fromDDS(byte[] DDS) {
        try {
            int[] pixels = DDSReader.read(DDS, DDSReader.ARGB, 0);
            int width = DDSReader.getWidth(DDS), height = DDSReader.getHeight(DDS);
            BufferedImage image = new BufferedImage(width, height, 2);
            if (image != null) {
                image.setRGB(0, 0, width, height, pixels, 0, width);
                return image;
            }
        } catch (Exception ex) {
            System.err.println("There was an error when converting DDS to BufferedImage.");
        }
        return null;
    }

    public static ImageIcon getImageIcon(BufferedImage image) {
        return getImageIcon(image, 320, 320);
    }
    public static ImageIcon getImageIcon(BufferedImage image, int width, int height) {
        return getImageScaled(image, width, height);
    }

    public static ImageIcon getImageScaled(BufferedImage image, int w, int h) {
        int width = image.getWidth(), height = image.getHeight();
        if (width > w || height > h) {
            if (width > height)
                return new ImageIcon(image.getScaledInstance(w, h / 2, 4));
            if (width < height)
                return new ImageIcon(image.getScaledInstance(w / 2, h, 4));
            return new ImageIcon(image.getScaledInstance(w, h, 4));
        }
        return new ImageIcon(image.getScaledInstance(width, height, 4));
    }

    public static BufferedImage toImage(byte[] image) {
        try (InputStream stream = new ByteArrayInputStream(image)) {
            return ImageIO.read(stream);
        } catch (IOException ex) { return null; }
    }

    public static BufferedImage getOverlayedImage(BufferedImage background, BufferedImage foreground, int x, int y, boolean drawForegroundRelative) {
        BufferedImage res = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = res.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!drawForegroundRelative) {
            g.drawImage(background, x, y, null);
            g.drawImage(foreground, 0, 0, null);
        } else {
            g.drawImage(background, 0, 0, null);
            g.drawImage(foreground, x, y, null);
        }

        g.dispose();

        return res;
    }

    public static BufferedImage getBufferedImageScaled(BufferedImage image, int width, int height) {
        if (image.getWidth() != image.getHeight())
            image = image.getSubimage(image.getWidth() / 4, 0, image.getHeight(), image.getHeight());

        Image img = image.getScaledInstance(width, height, 4);

        BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D b = i.createGraphics();
        b.drawImage(img, 0, 0, null);
        b.dispose();

        return i;
    }

    public static ImageIcon getAdventureIcon(BufferedImage master) {
        BufferedImage overlay = null;
        BufferedImage mask = null;
        try {
            if (master == null)
                master = ImageIO.read(Images.class.getResource("/images/slots/backdrop.png"));
            overlay = ImageIO.read(Images.class.getResource("/images/slots/adventure.png"));
            mask = ImageIO.read(Images.class.getResource("/images/slots/adventure_mask.png"));
        } catch (IOException ex) { return null; }

        master = getBufferedImageScaled(master, 185, 185);

        BufferedImage masked = new BufferedImage(185, 185, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = masked.createGraphics();
        applyQualityRenderingHints(g2d);
        g2d.drawImage(master, -10, -10, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
        g2d.drawImage(mask, -10, -10, null);
        g2d.dispose();

        master = Images.getOverlayedImage(masked, overlay, 45, 46, false);
        return Images.getImageIcon(master, 128, 128);
    }

    public static ImageIcon getGroupIcon(BufferedImage master) {
        BufferedImage res = null;
        try {
            BufferedImage image = ImageIO.read(Images.class.getResource("/images/slots/polaroid.png"));
            if (master == null) return new ImageIcon(image);
            master = getBufferedImageScaled(master, 104, 104);
            res = Images.getOverlayedImage(image, master, 73, 65, true);

        } catch (IOException ex) {
            Logger.getLogger(Images.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return Images.getImageScaled(res, 128, 128);
    }

    public static ImageIcon getSlotIcon(BufferedImage master, int revision) {
        if (master == null) {
            try {
                master = ImageIO.read(Images.class.getResource("/images/slots/backdrop.png"));
            } catch (IOException ex) {
                Logger.getLogger(Images.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        master = getBufferedImageScaled(master, 162, 162);
        int diameter = Math.min(master.getWidth(), master.getHeight());
        BufferedImage mask = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = mask.createGraphics();
        applyQualityRenderingHints(g2d);
        g2d.fillOval(0, 0, diameter - 1, diameter - 1);
        g2d.dispose();

        BufferedImage masked = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        g2d = masked.createGraphics();
        applyQualityRenderingHints(g2d);

        int x = (diameter - master.getWidth()) / 2;
        int y = (diameter - master.getHeight()) / 2;
        g2d.drawImage(master, x, y, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
        g2d.drawImage(mask, 0, 0, null);
        g2d.dispose();


        BufferedImage res = null;
        try {
            String path = "/images/slots/lbp1.png";
            if (revision > 0x272) {
                if (revision <= 0x3f8) path = "/images/slots/lbp2.png";
                else path = "/images/slots/lbp3.png";
            }

            BufferedImage image = ImageIO.read(Images.class.getResource(path));
            res = Images.getOverlayedImage(masked, image, 48, 51, false);

        } catch (IOException ex) {
            Logger.getLogger(Images.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Images.getImageScaled(res, 128, 128);
        //return new ImageIcon(getBufferedImageScaled(res, 220, 220));
    }

    public static void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

}