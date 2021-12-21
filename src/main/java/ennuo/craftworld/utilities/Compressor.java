package ennuo.craftworld.utilities;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compressor {
    public static byte[] deflateData(byte[] data) {
        try {
            Deflater deflater = new Deflater();
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
    
    public static void decompressData(Data data) {
        data.i16(); // Some flag? Always 0x0001
        short chunks = data.i16();
        
        int[] compressed = new int[chunks];
        int[] decompressed = new int[chunks];
        int decompressedSize = 0;
        for (int i = 0; i < chunks; ++i) {
            compressed[i] = data.u16();
            decompressed[i] = data.u16();
            decompressedSize += decompressed[i];
        }
        
        Output inflateStream = new Output(decompressedSize);
        for (int i = 0; i < chunks; ++i) {
            byte[] deflatedData = data.bytes(compressed[i]);
            if (compressed[i] == decompressed[i]) {
                inflateStream.bytes(deflatedData);
                continue;
            }
            byte[] inflatedData = Compressor.inflateData(deflatedData, decompressed[i]);
            if (inflatedData == null) { data.setData(null); return; }
            inflateStream.bytes(inflatedData);
        }
        data.setData(inflateStream.buffer);
    }

    public static byte[] getCompressedStream(byte[] data) {
        if (data == null) return new byte[] {};
        byte[][] chunks = Bytes.Split(data, 0x8000);

        short[] compressedSize = new short[chunks.length];
        short[] uncompressedSize = new short[chunks.length];

        byte[][] zlibStreams = new byte[chunks.length][];
        
        for (int i = 0; i < chunks.length; ++i) {
            byte[] compressed = Compressor.deflateData(chunks[i]);
            zlibStreams[i] = compressed;
            compressedSize[i] = (short) compressed.length;
            uncompressedSize[i] = (short) chunks[i].length;
        }

        Output output = new Output(4 + (chunks.length * 4), 0);
        output.i16((short) 1);
        output.i16((short) zlibStreams.length);

        for (int i = 0; i < zlibStreams.length; ++i) {
            output.i16(compressedSize[i]);
            output.i16(uncompressedSize[i]);
        }

        byte[] compressed = Bytes.Combine(zlibStreams);
        return Bytes.Combine(new byte[][] {
            output.buffer, compressed
        });
    }
}
