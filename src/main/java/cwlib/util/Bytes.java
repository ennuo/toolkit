package cwlib.util;

import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.data.ResourceDescriptor;
import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

import java.util.Arrays;

import org.joml.Vector3f;

public final class Bytes {
    /**
     * Hexadecimal characters used for hex/string conversion.
     */
    private static final char[] HEX_ARRAY = ("0123456789ABCDEF".toCharArray());

    /**
     * Converts a byte array into a hex string.
     * Implementation sourced from <a href="https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java">here</a>.
     * @param bytes Byte array to hexify
     * @return Hex representation of byte array
     */
    public static String toHex(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException("Can't convert null byte array to hexadecimal string!");
        final char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; ++i) {
            int b = bytes[i] & 0xFF;
            hex[i * 2] = Bytes.HEX_ARRAY[b >>> 4];
            hex[(i * 2) + 1] = Bytes.HEX_ARRAY[b & 0xF];
        }
        return String.valueOf(hex);
    }

    /**
     * Converts an integer into a hex string.
     * @param value Integer to hexify
     * @return Hex representation of integer
     */
    public static String toHex(int value) {
        return Bytes.toHex(Bytes.toBytesBE(value));
    }
    
    /**
     * Converts a hex string into a byte array.
     * @param v Hex string
     * @return Byte array from hex string
     */
    public static byte[] fromHex(String v) {
        int length = v.length();
        final byte[] b = new byte[length / 2];
        for (int i = 0; i < length; i += 2)
            b[i / 2] = (byte) (
                (Character.digit(v.charAt(i), 16) << 4) + Character.digit(v.charAt(i + 1), 16)
            );
        return b;
    }

    /**
     * Converts a big-order byte array to a short primitive.
     * @param b 2-byte array containing big-order short
     * @return the short from the byte array
     */
    public static short toShortBE(byte[] b) {
        if (b == null)
            throw new NullPointerException("Can't read data type from null byte array!");
        return (short) ((b[0] & 0xFF) << 8 | (b[1] & 0xFF) << 0);
    }

    /**
     * Converts a little-order byte array to a short primitive.
     * @param b 2-byte array containing litte-order short
     * @return the short from the byte array
     */
    public static short toShortLE(byte[] b) {
        if (b == null)
            throw new NullPointerException("Can't read data type from null byte array!");
        return (short) ((b[0] & 0xFF) << 0 | (b[1] & 0xFF) << 8);
    }
    
    /**
     * Converts a big-order byte array to an integer primitive.
     * @param b 4-byte array containing big-order integer
     * @return the integer from the byte array
     */
    public static int toIntegerBE(byte[] b) {
        if (b == null)
            throw new NullPointerException("Can't read data type from null byte array!");
        return (int) (
            (b[0] & 0xFF) << 24 | 
            (b[1] & 0xFF) << 16 | 
            (b[2] & 0xFF) << 8 | 
            (b[3] & 0xFF) << 0
        );
    }

    /**
     * Converts a little-order byte array to an integer primitive.
     * @param b 4-byte array containing little-order integer
     * @return the integer from the byte array
     */
    public static int toIntegerLE(byte[] b) {
        if (b == null)
            throw new NullPointerException("Can't read data type from null byte array!");
        return (int) (
            (b[0] & 0xFF) << 0 |
            (b[1] & 0xFF) << 8 |
            (b[2] & 0xFF) << 16 |
            (b[3] & 0xFF) << 24
        );
    }
    
        /**
     * Converts a short into a big-order byte array.
     * @param v short primitive
     * @return the big-order byte array containing the short
     */
    public static byte[] toBytesBE(short v) {
        return new byte[] {
            (byte) (v >>> 8),
            (byte) (v & 0xFF)
        };
    }

    /**
     * Converts a short into a little-order byte array.
     * @param v short primitive
     * @return the little-order byte array containing the short
     */
    public static byte[] toBytesLE(short v) {
        return new byte[] {
            (byte) (v & 0xFF),
            (byte) (v >>> 8)
        };
    }

    /**
     * Converts a integer into a big-order byte array.
     * @param v integer primitive
     * @return the big-order byte array containing the integer
     */
    public static byte[] toBytesBE(int v) {
        return new byte[] {
            (byte) (v >>> 24), 
            (byte) (v >>> 16), 
            (byte) (v >>> 8), 
            (byte) (v & 0xFF)
        };
    }

    /**
     * Converts a integer into a little-order byte array.
     * @param v integer primitive
     * @return the little-order byte array containing the integer
     */
    public static byte[] toBytesLE(int v) {
        return new byte[] {
            (byte) (v & 0xFF),
            (byte) (v >>> 8),
            (byte) (v >>> 16),
            (byte) (v >>> 24),
        };
    }
    
    public static byte[] getIntegerBuffer(long value, byte compressionFlags) {
        MemoryOutputStream output = new MemoryOutputStream(0x8, compressionFlags);
        output.u32(value);
        output.shrink();
        return output.getBuffer();
    }

    public static byte[] getResourceReference(ResourceDescriptor res, Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(0x1c + 0x4, revision, compressionFlags);
        serializer.resource(res, ResourceType.INVALID, true);
        return serializer.getBuffer();
    }
    
    /**
     * Converts an array of 32 bit big endian integers to a byte array.
     * @param data Integer array
     * @return Converted byte array
     */
    public static byte[] fromIntArrayBE(int[] data) {
        if (data == null) 
            throw new NullPointerException("Integer stream cannot be null!");
        byte[] output = new byte[data.length * 4];
        for (int i = 0; i < data.length; ++i) {
            int v = data[i]; int dest = (i * 4);
            output[dest] = (byte) (v >>> 24);
            output[dest + 1] = (byte) (v >>> 16);
            output[dest + 2] = (byte) (v >>> 8);
            output[dest + 3] = (byte) (v & 0xFF);
        }
        return output;
    }

    /**
     * Converts a big endian byte array to an array of 32 bit integers.
     * @param data Byte array
     * @return Converted integer array
     */
    public static int[] toIntArrayBE(byte[] data) {
        if (data == null) 
            throw new NullPointerException("Byte stream cannot be null!");
        if (data.length % 4 != 0)
            throw new IllegalArgumentException("Byte stream length must be divisible by 4!");
        int[] output = new int[data.length / 4];
        for (int i = 0; i < output.length; ++i) {
            int src = (i * 4);
            output[i] = (data[src] & 0xFF) << 24 | 
                        (data[src + 1] & 0xFF) << 16 | 
                        (data[src + 2] & 0xFF) << 8 | 
                        (data[src + 3] & 0xFF) << 0;
        }
        return output;
    }

    /**
     * Splits a byte array into a series of chunks.
     * Implementation sourced from <a href="https://stackoverflow.com/questions/3405195/divide-array-into-smaller-parts/26695737">here</a>.
     * @param data Byte array to split
     * @param size Size of each byte chunk
     * @return Chunked byte arrays
     */
    public static byte[][] split(byte[] data, int size) {
        byte[][] out = new byte[(int) Math.ceil(data.length / (double) size)][size];
        int start = 0;
        for (int i = 0; i < out.length; ++i) {
            int end = Math.min(data.length, start + size);
            out[i] = Arrays.copyOfRange(data, start, end);
            start += size;
        }
        return out;
    }

    /**
     * Combines a series of byte arrays into one.
     * Implementation sourced from <a href="https://stackoverflow.com/questions/5513152/easy-way-to-concatenate-two-byte-arrays">here</a>.
     * @param arrays Byte arrays to combine
     * @return Combined byte arrays.
     */
    public static byte[] combine(byte[]... arrays) {
        int totalLength = 0;
        for (int i = 0; i < arrays.length; i++)
            totalLength += arrays[i].length;
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (int i = 0; i < arrays.length; i++) {
            System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
            currentIndex += arrays[i].length;
        }
        return result;
    }

    /**
     * Replaces all instances of pattern inside bytearray with another.
     * Does in-place modification if the lengths are the same, otherwise
     * a new array is created.
     * @param source Buffer to replace patterns in
     * @param original Original pattern to replace
     * @param replacement Data to replace original pattern with
     * @return Bytearray with replaced patterns
     */
    public static byte[] replace(byte[] source, byte[] original, byte[] replacement) {
        if (Arrays.equals(original, replacement)) return source;

        int[] offsets = Matcher.indicesOf(source, original);

        // If the original/replacement buffer are the same length,
        // we can save time and memory by just copying into
        // those regions.
        if (original.length == replacement.length) {
            for (int offset : offsets)
                System.arraycopy(replacement, 0, source, offset, replacement.length);
            return source;
        }

        int diff = replacement.length - original.length;
        byte[] buffer = new byte[source.length - (original.length * offsets.length) + (replacement.length * offsets.length)];

        int sourceOffset = 0;
        int destOffset = 0;
        for (int i = 0; i < offsets.length; ++i) {
            int offset = offsets[i];
            int dest = offset + (diff * i);

            System.arraycopy(source, sourceOffset, buffer, sourceOffset + (diff * i), offset - sourceOffset);
            System.arraycopy(replacement, 0, buffer, dest, replacement.length);

            sourceOffset = offset + original.length;
            destOffset = dest + replacement.length;
        }

        int remaining = source.length - sourceOffset;
        if (remaining != 0)
            System.arraycopy(source, sourceOffset, buffer, destOffset, remaining);

        return buffer;
    }

    /**
     * Gets a byte array containing a ULEB-128 encoded value.
     * @param value Value to encode
     * @return Encoded byte array
     */
    public static byte[] packULEB128(long value) {
        MemoryOutputStream stream = new MemoryOutputStream(0x10, CompressionFlags.USE_ALL_COMPRESSION);
        return stream.u32(value).shrink().getBuffer();
    }

    /**
     * Unpacks a 11/11/10 normal value.
     * @param value Value to unpack
     * @return Unpacked vector
     */
    public static Vector3f unpackNormal32(long value) {
        // There's probably a much better way to handle 
        // the fact that this is probably just signed data
        // but this works, so maybe I'll come back to it 
        // at some point, who knows.

        Vector3f output = new Vector3f(0, 0, 0);
        
        float x = (float) (value & 0x3ffl);
        boolean x_sign = ((value >>> 10l) & 1l) > 0l;
        
        float y = (float) ((value >>> 11l) & 0x3ffl);
        boolean y_sign = ((value >>> 21l) & 1l) > 0l;
        
        float z = (float) ((value >>> 22l) & 0x1ffl);
        boolean z_sign = ((value >>> 31l & 1l)) > 0l;

        if (x_sign) output.x = -((1023f - x) / 1023f);
        else output.x = ((x / 1023f));

        if (y_sign) output.y = -((1023f - y) / 1023f);
        else output.y = (y / 1023f);
        
        if (z_sign) output.z = -((511f - z) / 511f);
        else output.z = (z / 511f);
        
        return output;
    }

    /**
     * Unpacks a 12/11/1 normal value.
     * @param normal Value to unpack
     * @return Unpacked vector
     */
    public static Vector3f unpackNormal24(int normal) {
        float x = (float) (normal & 0x7ff);
        x = ((normal & 0x800) != 0) ? (-(0x800 - x) / 0x7ff) : (x / 0x7ff);

        float y = (float) ((normal >> 12) & 0x3ff);
        y = (((normal >> 12) & 0x400) != 0) ? (-(0x400 - y) / 0x3ff) : (y / 0x3ff);

        float z = (float) (Math.pow(-1, (normal >> 23)) * 
            (Math.sqrt((1 - (Math.pow(x, 2) + Math.pow(y, 2))))));

        return new Vector3f(x, y, z);
    }

    // Unpacks a 11/11/10 normal value.

    /**
     * Packs a 11/11/10 normal value.
     * @param normal Vertex normal to pack
     * @return Packed normal
     */
    public static int packNormal32(Vector3f normal) {
        if (normal == null)
            throw new NullPointerException("Can't pack null vertex normal!");

        int x = Math.round(normal.x * 0x3ff) & 0x7ff;
        int y = Math.round(normal.y * 0x3ff) & 0x7ff;
        int z = Math.round(normal.z * 0x1ff) & 0x3ff;

        return (x | (y << 11) | (z << 22));
    }

    /**
     * Packs a 12/11/1 normal value.
     * @param normal Vertex normal to pack
     * @return Packed normal
     */
    public static int packNormal24(Vector3f normal) {
        if (normal == null)
            throw new NullPointerException("Can't pack null vertex normal!");

        int x = Math.round(normal.x * 0x7ff) & 0xfff;
        int y = Math.round(normal.y * 0x3ff) & 0x7ff;
        int z = (normal.z < 0) ? 1 : 0;

        return (x | (y << 12) | (z << 23));
    }
}
