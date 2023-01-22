package cwlib.resources;

import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import cwlib.enums.CompressionFlags;
import cwlib.ex.SerializationException;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.font.GlyphInfo;
import cwlib.types.data.Revision;
import cwlib.util.Compressor;
import cwlib.util.FileIO;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;

public class RFontFace {
    public int revision;
    public short[] glyphIndex = new short[128];
    public int[] glyphPageUsed = new int[16];
    public GlyphInfo[] glyphs;
    public byte[] data;
    public boolean isCompressed;

    public RFontFace(byte[] data) {
        MemoryInputStream stream = new MemoryInputStream(data, CompressionFlags.USE_NO_COMPRESSION);
        if (stream.i32() != 0x464E5462)
            throw new SerializationException("Not a supported RFontFace resource!");
        this.revision = stream.i32();
        int dataOffset = stream.i32();
        stream.i32(); // Padding presumably

        Serializer serializer = new Serializer(stream, new Revision(this.revision));
        this.glyphIndex = serializer.shortarray(this.glyphIndex);
        this.glyphPageUsed = serializer.intarray(this.glyphPageUsed);
        this.glyphs = serializer.array(this.glyphs, GlyphInfo.class);

        // Probably not even close to the right revision
        // but like good enough
        if (this.revision > 0x272)
            this.isCompressed = stream.bool();

        this.data = stream.bytes(stream.getLength() - stream.getOffset());
    }

    public GlyphInfo getGlyph(char c) {
        for (GlyphInfo info : this.glyphs) {
            if (info.character == ((short)c))
                return info;
        }
        return null;
    }

    public BufferedImage getGlyph(GlyphInfo info) {
        byte[] glyph = this.getGlyphData(info);

        int w = info.boxW & 0xff;
        int h = info.boxH & 0xff;

        int[] pixels = new int[w * h];

        if (this.isCompressed) {
            for (int i = 0; i < pixels.length; ++i) {
                int value = glyph[i] & 0xff;
                pixels[i] = new Color(value, value, value, 0xff).getRGB();
            }
        } else {
            for (int i = 0; i < pixels.length; ++i) {
                int green = glyph[(i * 2) + 0] & 0xff;
                int blue = glyph[(i * 2) + 1] & 0xff;
                pixels[i] = new Color(0x0, green, blue, 0xff).getRGB();

            }
        }

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, w, h, pixels, 0, w);
        return image;
    }

    public byte[] getGlyphData(GlyphInfo info) {
        int imageSize = (info.boxW & 0xff) * (info.boxH & 0xff);
        if (this.isCompressed) {
            int size = ((this.data[info.offset] & 0xFF) << 8 | (this.data[info.offset + 1] & 0xFF) << 0);
            byte[] stream = Arrays.copyOfRange(this.data, info.offset + 2, info.offset + size);
            return Compressor.inflateData(stream, imageSize);
        }
        return Arrays.copyOfRange(this.data, info.offset, info.offset + (imageSize * 2));
    }

    public boolean export(String path) {
        int maxColumns = (int) (Math.floor(Math.sqrt(this.glyphs.length)));
        int col = 0, x = 0, y = 0, maxHeightInRow = 0;
        int w = 0, h = 0;
        for (GlyphInfo info : this.glyphs) {
            if (col == maxColumns) {
                y += maxHeightInRow;

                maxHeightInRow = 0;
                col = 0;
                x = 0;
            }

            info.cacheX = x;
            info.cacheY = y;

            int sh = (info.boxH & 0xff);
            int sw = (info.boxW & 0xff);

            if ((x + sw) > w) w = (x + sw);
            if ((y + sh) > h) h = (y + sh);

            if (sh > maxHeightInRow) 
                maxHeightInRow = sh;
            
            col++;
            x += (info.boxW & 0xff);
        }

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
        for (GlyphInfo info : this.glyphs) {
            BufferedImage glyph = this.getGlyph(info); 
            g.drawImage(glyph, null, info.cacheX, info.cacheY);
        }

        try { 
            ImageIO.write(image, "png", new File(path));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
