package cwlib.util;

import cwlib.resources.RTexture;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.io.streams.MemoryInputStream;
import gr.zdimensions.jsquish.Squish;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import cwlib.external.DDSReader;
import toolkit.windows.Toolkit;

import org.imgscalr.Scalr;

public class Images {
    public static byte[] toGTF(BufferedImage image) {
        MemoryInputStream data = new MemoryInputStream(toDDS(image));
        if (data.data == null) return null;

        data.seek(0x80);

        byte[] DDS = data.bytes(data.length - 0x80);

        if (DDS == null)
            return null;

        DDS = Compressor.deflateData(DDS);

        if (DDS == null) {
            System.err.println("Failed to compress DDS!");
            return null;
        }

        MemoryOutputStream output = new MemoryOutputStream(0x1C + DDS.length);
        output.str("GTF ");
        if (image.getColorModel().hasAlpha()) output.u8(0x88);
        else output.u8(0x86);

        output.bytes(new byte[] { 0x0A, 0x02, 0x00, 0x00, 0x00, (byte) 0xAA, (byte) 0xE4 });

        output.i16((short) image.getWidth());
        output.i16((short) image.getHeight());

        output.bytes(new byte[] { 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });

        output.bytes(DDS);

        return output.buffer;
    }
    
    private static int toNearest(int x) {
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x ^ (x >> 1);
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


    private static byte[] toDDS(BufferedImage image) {
        int width = toNearest(image.getWidth());
        int height = toNearest(image.getHeight());
        
        int originalWidth = width, originalHeight = height;
        
        Squish.CompressionType type = Squish.CompressionType.DXT1;
        int format = 0x86;
        if (image.getColorModel().hasAlpha()) {
            format = 0x88;
            type = Squish.CompressionType.DXT5;
        }

        image = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width, height);
        byte[] DDS = Squish.compressImage(getRGBA(image), width, height, null, type);
        int mipCount = 0;
        while (true) {
            width = toNearest(width - 1);
            height = toNearest(height - 1);
            image = Scalr.resize(image, Scalr.Method.AUTOMATIC, width, height);
            DDS = Bytes.Combine(DDS, Squish.compressImage(getRGBA(image), width, height, null, type));
            mipCount += 1;
            if (width == 1|| height == 1) break;
        }
        
        return Bytes.Combine(RTexture.getDDSHeader(format, originalWidth, originalHeight, mipCount), DDS);
    }

    public static byte[] toTEX(BufferedImage image) {
        byte[] DDS = toDDS(image);

        if (DDS == null) {
            System.err.println("Failed to convert BufferedImage to DDS!");
            return null;
        }

        DDS = Compressor.getCompressedStream(DDS);

        if (DDS == null) {
            System.err.println("Failed to compress DDS!");
            return null;
        }

        MemoryOutputStream output = new MemoryOutputStream(0x4 + DDS.length);
        output.str("TEX ");
        output.bytes(DDS);

        return output.buffer;
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
        InputStream stream = new ByteArrayInputStream(image);
        BufferedImage output = null;
        try {
            output = ImageIO.read(stream);
            stream.close();
        } catch (IOException ex) {
            Logger.getLogger(Toolkit.class.getName()).log(Level.SEVERE, (String) null, ex);
            return null;
        }
        return output;
    }
    
    public static BufferedImage multiply(BufferedImage source, BufferedImage sample) {
        if (sample.getWidth() != source.getWidth() || sample.getHeight() != source.getHeight())
            sample = Scalr.resize(sample, Scalr.Mode.FIT_EXACT, source.getWidth(), source.getHeight());
        for (int x = 0; x < source.getWidth(); ++x) {
            for (int y = 0; y < source.getHeight(); ++y) {
                Color sourceColor = new Color(source.getRGB(x, y), true);
                Color sampleColor = new Color(sample.getRGB(x, y), true);
                Color o = new Color(
                        Math.round(sourceColor.getRed() * (float)(sampleColor.getRed() / 255f)),
                        Math.round(sourceColor.getGreen() * (float)(sampleColor.getGreen() / 255f)),
                        Math.round(sourceColor.getBlue() * (float)(sampleColor.getBlue() / 255f)),
                        sourceColor.getAlpha()
                );
                source.setRGB(x, y, o.getRGB());
            }
        }
        
        return source;
    }
    
    public static BufferedImage getTiledImage(BufferedImage image, int scaleX, int scaleY) {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int scaledWidth = image.getWidth() / scaleX;
        int scaledHeight = image.getHeight() / scaleY;
        image = Scalr.resize(image, Scalr.Mode.FIT_EXACT, scaledWidth, scaledHeight);
        Graphics2D graphics = output.createGraphics();
        for (int x = 0; x < scaleX; ++x)
            for (int y = 0; y < scaleY; ++y)
                graphics.drawImage(image, x * scaledWidth, y * scaledHeight, null);
        graphics.dispose();
        return output;
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

    public static ImageIcon getGroupIcon(BufferedImage master) {
        BufferedImage res = null;
        try {
            BufferedImage image = ImageIO.read(Toolkit.class.getResource("/polaroid.png"));
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
                master = ImageIO.read(Toolkit.class.getResource("/default.png"));
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
            String path = "/lbp1slot.png";
            if (revision > 0x272) {
                if (revision <= 0x3f8) path = "/lbp2slot.png";
                else path = "/lbp3slot.png";
            }

            BufferedImage image = ImageIO.read(Toolkit.class.getResource(path));
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