package cwlib.io.streams;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.io.ValueEnum;
import cwlib.enums.CompressionFlags;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;

/**
 * Big-endian binary input stream.
 */
public class MemoryInputStream {
    public static enum SeekMode {
        Begin,
        Relative,
        End
    }
    
    private final byte[] buffer;

    private int offset = 0;
    private final int length;
    private byte compressionFlags;

    private boolean isLittleEndian = false;

    /**
     * Creates a memory input stream from byte array.
     * @param buffer Byte array to use as source
     */
    public MemoryInputStream(byte[] buffer) {
        if (buffer == null)
            throw new NullPointerException("Buffer supplied to MemoryInputStream cannot be null!");
        this.buffer = buffer;
        this.length = buffer.length;
    }

    /**
     * Creates a memory input stream from byte array with compression flags.
     * @param buffer Byte array to use as source
     * @param compressionFlags Flags for compression methods used
     */
    public MemoryInputStream(byte[] buffer, byte compressionFlags) {
        this(buffer);
        this.compressionFlags = compressionFlags;
    }

    /**
     * Creates a memory input stream from file at path.
     * @param path Location to read data from
     */
    public MemoryInputStream(String path) {
        if (path == null)
            throw new NullPointerException("Path supplied to MemoryInputStream cannot be null!");
        final byte[] data = FileIO.read(path);
        if (data == null)
            throw new IllegalArgumentException("File provided could not be read!");
        this.buffer = data;
        this.length = data.length;
    }

    /**
     * Creates a memory input stream from file at path with compression flags.
     * @param path Location to read data from
     * @param compressionFlags Flags for compression methods used
     */
    public MemoryInputStream(String path, byte compressionFlags) {
        this(path);
        this.compressionFlags = compressionFlags;
    }

    /**
     * Reads an arbitrary number of bytes from the stream.
     * @param size Number of bytes to read from the stream
     * @return Bytes read from the stream
     */
    public final byte[] bytes(int size) {
        this.offset += size;
        return Arrays.copyOfRange(this.buffer, this.offset - size, this.offset);
    }

    /**
     * Reads a byte array from the stream.
     * @return Bytes read from the stream
     */
    public final byte[] bytearray() {
        int size = this.i32();
        return this.bytes(size);
    }

    /**
     * Reads a boolean from the stream.
     * @return Boolean read from the stream
     */
    public final boolean bool() { return (this.i8() != 0); }

    /**
     * Reads an array of booleans from the stream.
     * @return Boolean array read from the stream
     */
    public final boolean[] boolarray() {
        int count = this.i32();
        boolean[] elements = new boolean[count];
        for (int i = 0; i < count; ++i)
            elements[i] = this.bool();
        return elements;
    }

    /**
     * Reads a byte from the stream.
     * @return Byte read from the stream
     */
    public final byte i8() { return this.buffer[this.offset++]; }

    /**
     * Reads an unsigned byte from the stream as an integer.
     * @return Byte read from the stream
     */
    public final int u8() { return this.buffer[this.offset++] & 0xFF; }

    /**
     * Reads a short from the stream.
     * @return Short read from the stream
     */
    public final short i16() { 
        byte[] bytes = this.bytes(2);
        if (this.isLittleEndian) return Bytes.toShortLE(bytes);
        return Bytes.toShortBE(bytes);
    }

    /**
     * Reads an unsigned short from the stream as an integer.
     * @return Short read from the stream
     */
    public final int u16() { return (this.i16() & 0xFFFF); } 

    /**
     * Reads an unsigned 24-bit integer from the stream.
     * @return Integer read from the stream
     */
    public final int u24() {
        final byte[] b = this.bytes(3);
        if (this.isLittleEndian)
            return (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | b[0] & 0xFF;
        return (b[0] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | b[2] & 0xFF;
    }

    /**
     * Reads a 32-bit integer from the stream, compressed depending on flags.
     * @param force32 Whether or not to read as a 32-bit integer, regardless of compression flags.
     * @return Integer read from the stream
     */
    public final int i32(boolean force32) {
        if (force32 || (this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) == 0) {
            byte[] bytes = this.bytes(4);
            if (this.isLittleEndian) return Bytes.toIntegerLE(bytes);
            return Bytes.toIntegerBE(bytes);
        }
        return (int) (this.uleb128() & 0xFFFFFFFF);
    }

    /**
     * Reads a signed 32-bit integer from the stream, compressed depending on flags.
     * This function modifies the value written to the stream to fit an unsigned value, prefer i32
     * @return Signed integer read from the stream
     */
    public final int s32() {
        if (((this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) == 0))
            return this.i32(true);
        long v = this.uleb128();
        return (int) ((v >> 1 ^ -(v & 1)) & 0xFFFFFFFF);
    }

    /**
     * Reads a long as an unsigned integer from the stream, compressed depending on flags.
     * @param force64 Whether or not to read as a 32-bit integer, regardless of compression flags.
     * @return Unsigned integer read from the stream
     */
    public final long u32(boolean force32) {
        if (force32 || (this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) == 0)
            return this.i32(true) & 0xFFFFFFFFl;
        return this.uleb128();
    }

    /**
     * Reads a long from the stream, compressed depending on flags.
     * @param force64 Whether or not to read as a 64-bit long, regardless of compression flags.
     * @return Long read from the stream
     */
    public final long i64(boolean force64) {
        if (force64 || (this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) == 0) {
            final byte[] b = this.bytes(8);
            if (this.isLittleEndian) {
                return	(b[7] & 0xFFL) << 56L |
                        (b[6] & 0xFFL) << 48L |
                        (b[5] & 0xFFL) << 40L |
                        (b[4] & 0xFFL) << 32L |
                        (b[3] & 0xFFL) << 24L |
                        (b[2] & 0xFFL) << 16L |
                        (b[1] & 0xFFL) << 8L |
                        (b[0] & 0xFFL) << 0L;
            }
            return	(b[0] & 0xFFL) << 56L |
                    (b[1] & 0xFFL) << 48L |
                    (b[2] & 0xFFL) << 40L |
                    (b[3] & 0xFFL) << 32L |
                    (b[4] & 0xFFL) << 24L |
                    (b[5] & 0xFFL) << 16L |
                    (b[6] & 0xFFL) << 8L |
                    (b[7] & 0xFFL) << 0L;
        }
        return this.uleb128();
    }

    /**
     * Reads an integer from the stream.
     * @return Integer read from the stream
     */
    public final int i32() { return this.i32(false); }

    /**
     * Reads a long as an unsigned integer from the stream.
     * @return Unsigned integer read from the stream
     */
    public final long u32() { return this.u32(false); }

    /**
     * Reads a long from the stream.
     * @return Long read from the stream
     */
    public final long i64() { return this.i64(false); }

    /**
     * Reads a variable length quantity from the stream.
     * @return Long value read from the stream
     */
    public final long uleb128() {
        long result = 0, i = 0;
        while (true) {
            long b = (long) (this.u8() & 0xFFl);
            result |= (b & 0x7fl) << 7l * i;
            if ((b & 0x80l) == 0l)
                break;
            ++i;
        }
        return result >>> 0;
    }

    /**
     * Reads a 16-bit integer array from the stream.
     * @return Short array read from the stream
     */
    public final short[] shortarray() {
        int count = this.i32();
        short[] elements = new short[count];
        for (int i = 0; i < count; ++i)
            elements[i] = this.i16();
        return elements;
    }

    /**
     * Reads a 32-bit integer array from the stream.
     * @return Integer array read from the stream
     */
    public final int[] intarray() {
        int count = this.i32();
        int[] elements = new int[count];
        for (int i = 0; i < count; ++i)
            elements[i] = this.i32();
        return elements;
    }

    /**
     * Reads a 64-bit integer array from the stream.
     * @return Long array read from the stream
     */
    public final long[] longarray() {
        int count = this.i32();
        long[] elements = new long[count];
        for (int i = 0; i < count; ++i)
            elements[i] = this.i64();
        return elements;
    }

    /**
     * Reads a 16 bit floating point number from the stream.
     * https://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
     * @return Float read from the stream
     */
    public final float f16() {
        int half = this.u16();
        int mant = half & 0x03ff;
        int exp = half & 0x7c00;
        if (exp == 0x7c00) exp = 0x3fc00;
        else if (exp != 0) {
            exp += 0x1c000;
            if (mant == 0 && exp > 0x1c400)
                return Float.intBitsToFloat((half & 0x8000) << 16 | exp << 13 | 0x3ff);
        } else if (mant != 0) {
            exp = 0x1c400;
            do {
                mant <<= 1;
                exp -= 0x400;
            } while ((mant & 0x400) != 0);
            mant &= 0x3ff;
        }
        return Float.intBitsToFloat((half & 0x8000) << 16 | (exp | mant) << 13);
    }

    /**
     * Reads a 32 bit floating point number from the stream.
     * @return Float read from the stream
     */
    public final float f32() { return Float.intBitsToFloat(this.i32(true)); }

    /**
     * Reads a 32-bit floating point number array from the stream.
     * @return Float array read from the stream
     */
    public final float[] floatarray() {
        int count = this.i32();
        float[] elements = new float[count];
        for (int i = 0; i < count; ++i)
            elements[i] = this.f32();
        return elements;
    }

    /**
     * Reads a 2-dimensional floating point vector from the stream.
     * @return Vector2f read from the stream
     */
    public final Vector2f v2() { return new Vector2f(this.f32(), this.f32()); }

    /**
     * Reads a 3-dimensional floating point vector from the stream.
     * @return Vector3f read from the stream
     */
    public final Vector3f v3() { return new Vector3f(this.f32(), this.f32(), this.f32()); }

    /**
     * Reads a 4-dimensional floating point vector from the stream.
     * @return Vector4f read from the stream
     */
    public final Vector4f v4() { return new Vector4f(this.f32(), this.f32(), this.f32(), this.f32()); }

    /**
     * Reads an array of 4-dimensional floating point vectors from the stream.
     * @return Vector array read from the stream
     */
    public final Vector4f[] vectorarray() {
        int count = this.i32();
        Vector4f[] elements = new Vector4f[count];
        for (int i = 0; i < count; ++i)
            elements[i] = this.v4();
        return elements;
    }

    /**
     * Reads a Matrix4x4 from the stream, compressed depending on flags.
     * @return Matrix4x4 read from the stream
     */
    public Matrix4f m44() {
        final float[] matrix = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

        int flags = 0xFFFF;
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_MATRICES) != 0)
            flags = this.i16();
        
        for (int i = 0; i < 16; ++i)
            if ((flags & (1 << i)) != 0)
                matrix[i] = this.f32();
        
        final Matrix4f mat = new Matrix4f();
        mat.set(matrix);
        return mat;
    }

    /**
     * Reads a string of specified size from the stream.
     * @param size Size of string to read
     * @return String value read from the stream
     */
    public final String str(int size) {
        if (size == 0) return "";
        return new String(this.bytes(size)).replace("\0", "");
    }

    /**
     * Reads a wide string of specified size from the stream.
     * @param size Size of string to read
     * @return String value read from the stream
     */
    public final String wstr(int size) {
        if (size == 0) return "";
        return new String(this.bytes(size * 2), StandardCharsets.UTF_16BE).replace("\0", "");
    }

    /**
     * Reads a length-prefixed string from the stream.
     * @return String value read from the stream
     */
    public final String str() { return this.str(this.s32()); }

    /**
     * Reads a length-prefixed wide string from the stream.
     * @return String value read from the stream
     */
    public final String wstr() { return this.wstr(this.s32()); }

    /**
     * Reads a SHA1 hash from the stream.
     * @return SHA1 hash read from the stream
     */
    public final SHA1 sha1() { return new SHA1(this.bytes(0x14)); }

    /**
     * Reads a GUID (uint32_t) from the stream.
     * @return GUID read from the stream
     */
    public final GUID guid() { return this.guid(false); }

    /**
     * Reads a GUID (uint32_t) from the stream.
     * @param force32 Whether or not to read as a 32 bit integer, regardless of compression flags.
     * @return GUID read from the stream
     */
    public final GUID guid(boolean force32) {
        long number = this.u32(force32);
        if (number == 0) return null;
        return new GUID(number); 
    }

    /**
     * Reads an 8-bit integer from the stream and resolves the enum value.
     * @param <T> Type of enum
     * @param enumeration Enum class
     * @return Resolved enum constant
     */
    public final <T extends Enum<T> & ValueEnum<Byte>> T enum8(Class<T> enumeration) {
        byte number = this.i8();
        List<T> constants = Arrays.asList(enumeration.getEnumConstants());
        for (T constant : constants)
            if (constant.getValue().equals(number))
                return constant;
        return null;
    }

    /**
     * Reads an 32-bit integer from the stream and resolves the enum value.
     * @param <T> Type of enum
     * @param enumeration Enum class
     * @return Resolved enum constant
     */
    public final <T extends Enum<T> & ValueEnum<Integer>> T enum32(Class<T> enumeration) {
        return this.enum32(enumeration, false);
    }

    /**
     * Reads an 32-bit integer from the stream and resolves the enum value.
     * @param <T> Type of enum
     * @param enumeration Enum class
     * @param signed Whether or not to read a signed value
     * @return Resolved enum constant
     */
    public final <T extends Enum<T> & ValueEnum<Integer>> T enum32(Class<T> enumeration, boolean signed) {
        int number = (signed) ? this.s32() : this.i32();
        List<T> constants = Arrays.asList(enumeration.getEnumConstants());
        for (T constant : constants)
            if (constant.getValue().equals(number))
                return constant;
        return null;
    }

    /**
     * Reads a series of 8-bit integers from the stream and resolves them
     * as an enum array.
     * @param <T> Type of enum
     * @param enumeration Enum class
     * @return Resolved enum constant
     */
    @SuppressWarnings("unchecked")
    public final <T extends Enum<T> & ValueEnum<Byte>> T[] enumarray(Class<T> enumeration) {
        int count = this.i32();
        T[] elements = (T[]) Array.newInstance(enumeration, count);
        for (int i = 0; i < count; ++i)
            elements[i] = this.enum8(enumeration);
        return elements;
    }

    /**
     * Seeks to position relative to seek mode.
     * @param offset Offset relative to seek position
     * @param mode Seek origin
     */
    public final void seek(int offset, SeekMode mode) {
        if (mode == null)
            throw new NullPointerException("SeekMode cannot be null!");
        if (offset < 0) throw new IllegalArgumentException("Can't seek to negative offsets.");
        switch (mode) {
            case Begin: {
                if (offset > this.length)
                    throw new IllegalArgumentException("Can't seek past stream length.");
                this.offset = offset;
                break;
            }
            case Relative: {
                int newOffset = this.offset + offset;
                if (newOffset > this.length || newOffset < 0)
                    throw new IllegalArgumentException("Can't seek outside bounds of stream.");
                this.offset = newOffset;
                break;
            }
            case End: {
                if (offset < 0 || this.length - offset < 0)
                    throw new IllegalArgumentException("Can't seek outside bounds of stream.");
                this.offset = this.length - offset;
                break;
            }
        }
    }

    /**
     * Seeks ahead in stream relative to offset.
     * @param offset Offset to go to
     */
    public final void seek(int offset) { 
        this.seek(offset, SeekMode.Relative);
    }

    public final boolean isLittleEndian() { return this.isLittleEndian; }
    public final byte[] getBuffer() { return this.buffer; }
    public final int getOffset() { return this.offset; }
    public final int getLength() { return this.length; }
    public final byte getCompressionFlags() { return this.compressionFlags; }
    public void setLittleEndian(boolean value) { this.isLittleEndian = value; }
}
