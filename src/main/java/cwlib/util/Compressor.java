package cwlib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import cwlib.ex.SerializationException;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

/**
 * Zlib compression utilities.
 */
public final class Compressor {
    /**
     * Compresses a buffer.
     * @param data Data to compress
     * @return Compressed data
     */
    public static byte[] deflateData(byte[] data) {
        try {
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            ByteArrayOutputStream stream = new ByteArrayOutputStream(data.length);
            deflater.setInput(data);
            deflater.finish();
            byte[] chunk = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(chunk);
                stream.write(chunk, 0, count);
            }

            stream.close();
            return stream.toByteArray();
        } catch (IOException ex) { return null; }
    }
    
    /**
     * Decompresses a buffer.
     * @param data Compressed data
     * @param size Size of decompressed data
     * @return Decompressed data
     */
    public static byte[] inflateData(byte[] data, int size) {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            byte[] output = new byte[size];
            inflater.inflate(output);
            inflater.end();
            return output;
        } catch (DataFormatException  ex) { return null; }
    }
    
    /**
     * Decompresses zlib data from a stream.
     * @param stream Stream to decompress
     * @param endOffset Offset of the end of the compressed streams
     * @return Decompressed data
     */
    public static byte[] decompressData(MemoryInputStream stream, int endOffset) {
        stream.i16(); // Some flag? Always 0x0001
        short chunks = stream.i16();

        if (chunks == 0)
            return stream.bytes(endOffset - stream.getOffset());
        
        int[] compressed = new int[chunks];
        int[] decompressed = new int[chunks];
        int decompressedSize = 0;
        for (int i = 0; i < chunks; ++i) {
            compressed[i] = stream.u16();
            decompressed[i] = stream.u16();
            decompressedSize += decompressed[i];
        }
        
        MemoryOutputStream inflateStream = new MemoryOutputStream(decompressedSize);
        for (int i = 0; i < chunks; ++i) {
            byte[] deflatedData = stream.bytes(compressed[i]);
            if (compressed[i] == decompressed[i]) {
                inflateStream.bytes(deflatedData);
                continue;
            }
            byte[] inflatedData = Compressor.inflateData(deflatedData, decompressed[i]);
            if (inflatedData == null)
                throw new SerializationException("An error occurred while inflating data!");
            inflateStream.bytes(inflatedData);
        }
        
        return inflateStream.getBuffer();
    }

    /**
     * Compresses a buffer into multiple zlib streams of size 0x8000.
     * @param data Data to compress
     * @param isCompressed Additional check for compression, used in low resource revisions
     * @return Compressed zlib streams
     */
    public static byte[] getCompressedStream(byte[] data, boolean isCompressed) {
        if (data == null) return new byte[] {};
        if (!isCompressed)
            return Bytes.combine(new byte[] { 0x00, 0x00, 0x00, 0x00 }, data);
        
        byte[][] chunks = Bytes.split(data, 0x8000);

        short[] compressedSize = new short[chunks.length];
        short[] uncompressedSize = new short[chunks.length];

        byte[][] zlibStreams = new byte[chunks.length][];
        
        for (int i = 0; i < chunks.length; ++i) {
            byte[] compressed = Compressor.deflateData(chunks[i]);
            zlibStreams[i] = compressed;
            compressedSize[i] = (short) compressed.length;
            uncompressedSize[i] = (short) chunks[i].length;
        }

        MemoryOutputStream output = new MemoryOutputStream(4 + (chunks.length * 4));
        output.u16(1); // Some flag? Always 0x0001
        output.u16(zlibStreams.length);

        for (int i = 0; i < zlibStreams.length; ++i) {
            output.i16(compressedSize[i]);
            output.i16(uncompressedSize[i]);
        }

        byte[] compressed = Bytes.combine(zlibStreams);
        return Bytes.combine(new byte[][] {
            output.getBuffer(), compressed
        });
    }
}
