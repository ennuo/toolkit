package ennuo.craftworld.utilities;

import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.types.data.ResourceDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class Compressor {

    public static byte[] CompressData(byte[] data) {
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

        } catch (IOException ex) {
            return null;
        }
    }

    public static byte[] CompressRaw(byte[] data) {
        if (data == null) return new byte[] {};
        byte[][] chunks = Bytes.Split(data, 0x8000);

        short[] compressedSize = new short[chunks.length];
        short[] uncompressedSize = new short[chunks.length];

        byte[][] zlibStreams = new byte[chunks.length][];
        
        for (int i = 0; i < chunks.length; ++i) {
            byte[] compressed = CompressData(chunks[i]);
            zlibStreams[i] = compressed;
            compressedSize[i] = (short) compressed.length;
            uncompressedSize[i] = (short) chunks[i].length;
        }

        Output output = new Output(2 + (chunks.length * 4), 0);
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

    public static byte[] CompressStaticMesh(byte[] data, int revision, ResourceDescriptor[] dependencies) {
        Output output = new Output(0xD);
        output.str("SMHb");
        output.i32f(revision);
        output.i32f(0xD + data.length);
        output.u8(0);

        return Bytes.Combine(output.buffer, data, Dependinate(dependencies));

    }

    public static byte[] Compress(byte[] data, String magic, int revision, ResourceDescriptor[] dependencies) {
        if (magic.equals("SMHb")) return CompressStaticMesh(data, revision, dependencies);
        byte[] compressed = CompressRaw(data);

        boolean legacy = revision < 0x272;
        int size = 40;
        byte[] flags = (legacy) ? new byte[] { 1, 0, 1 } : new byte[] { 0, 0, 0, 0, 7, 1, 0, 1 };
        if (revision <= 0x188) 
            flags = new byte[] { 0, 1 };
        if (revision <= 0x272 && revision > 0x26e)
            flags = new byte[] { 0x4c, 0x44, 0x00, 0x17, 7, 1, 0, 1 };
        else if (revision == 0x3e2)
            flags = new byte[] { 0x44, 0x31, 0x00, (byte) 0x87, 0x07, 0x01, 0x01, 0x01 };
        Output output = new Output(size);
        output.str(magic);
        output.i32(revision);
        output.i32(output.offset + 4 + compressed.length + flags.length);
        output.bytes(flags);
        output.shrink();
        return Bytes.Combine(new byte[][] {
            output.buffer, compressed, Dependinate(dependencies)
        });
    }

    private static byte[] Dependinate(ResourceDescriptor[] resources) {
        Output output = new Output(0x1C * resources.length + 4);

        output.i32(resources.length);
        for (ResourceDescriptor resource: resources) {
            output.resource(resource, true);
            output.i32(resource.type.value);
        }

        output.shrink();
        return output.buffer;

    }
}
