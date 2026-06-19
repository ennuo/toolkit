package cwlib.io.streams;

import cwlib.enums.CompressionFlags;
import cwlib.ex.SerializationException;
import cwlib.io.ValueEnum;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;
import cwlib.util.BinaryPrimitives;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Big-endian binary output stream.
 */
public class MemoryOutputStream
{
    private static final float[] IDENTITY = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
    private static final byte[] EMPTY = new byte[] {};

    private byte[] _buffer;
    private int _offset = 0;
    private int _length = 0;
    private byte _compressionFlags;
    private boolean _isLittleEndian = false;

    public MemoryOutputStream()
    {
        _buffer = EMPTY;
        _offset = 0;
        _length = 0;
    }

    public MemoryOutputStream(byte compressionFlags)
    {
        _buffer = EMPTY;
        _offset = 0;
        _length = 0;
    }

    /**
     * Creates a memory output stream with specified size.
     *
     * @param capacity Size of stream
     */
    public MemoryOutputStream(int capacity)
    {
        _buffer = new byte[capacity];
        _length = 0;
    }

    /**
     * Creates a memory output stream with specified size and compression flags.
     *
     * @param capacity             Size of stream
     * @param compressionFlags Flags for compression methods used
     */
    public MemoryOutputStream(int capacity, byte compressionFlags)
    {
        _buffer = new byte[capacity];
        _compressionFlags = compressionFlags;
    }

    private void ensureCapacity(int value)
    {
        if (_buffer.length >= value) return;

        int capacity = Math.max(value, _buffer.length * 2);
        _buffer = Arrays.copyOf(_buffer, capacity);
    }

    /**
     * Writes an arbitrary number of bytes to the stream.
     *
     * @param value Bytes to write
     * @return This output stream
     */
    public final MemoryOutputStream bytes(byte[] value)
    {
        if (value == null || value.length == 0) return this;
        ensureCapacity(_offset + value.length);
        System.arraycopy(value, 0, _buffer, _offset, value.length);
        advance(value.length);
        return this;
    }

    /**
     * Writes a byte array to the stream.
     *
     * @param value Bytes to write
     * @return This output stream
     */
    public final MemoryOutputStream bytearray(byte[] value)
    {
        if (value == null) return this.i32(0);
        this.i32(value.length);
        return this.bytes(value);
    }

    /**
     * Writes a boolean to the stream.
     *
     * @param value Boolean to write
     * @return This output stream
     */
    public final MemoryOutputStream bool(boolean value)
    {
        return this.u8(value ? 1 : 0);
    }

    /**
     * Writes an array of booleans to the stream.
     *
     * @param values Boolean array to write
     * @return This output stream
     */
    public final MemoryOutputStream boolarray(boolean[] values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (Byte.BYTES * values.length));
        this.i32(values.length);
        for (boolean value : values)
            this.bool(value);
        return this;
    }

    /**
     * Writes a byte to the stream.
     *
     * @param value Byte to write
     * @return This output stream
     */
    public final MemoryOutputStream i8(byte value)
    {
        ensureCapacity(_offset + Byte.BYTES);
        _buffer[_offset] = value;
        advance(Byte.BYTES);
        return this;
    }

    /**
     * Writes an integer to the stream as a byte.
     *
     * @param value Byte to write
     * @return This output stream
     */
    public final MemoryOutputStream u8(int value)
    {
        ensureCapacity(_offset + Byte.BYTES);
        _buffer[_offset] = (byte) (value & 0xFF);
        advance(Byte.BYTES);
        return this;
    }

    /**
     * Writes a short to the stream.
     *
     * @param value Short to write
     * @return This output stream
     */
    public final MemoryOutputStream i16(short value)
    {
        ensureCapacity(_offset + Short.BYTES);

        if (_isLittleEndian)
            BinaryPrimitives.writeInt16LittleEndian(_buffer, _offset, value);
        else
            BinaryPrimitives.writeInt16BigEndian(_buffer, _offset, value);

        advance(Short.BYTES);

        return this;
    }

    /**
     * Writes an integer to the stream as an unsigned short.
     *
     * @param value Short to write
     * @return This output stream
     */
    public final MemoryOutputStream u16(int value)
    {
        return this.i16((short) (value & 0xFFFF));
    }

    /**
     * Writes a 24-bit unsigned integer to the stream.
     *
     * @param value Integer to write
     * @return This output stream
     */
    public final MemoryOutputStream u24(int value)
    {
        final int BYTES = 3;
        ensureCapacity(_offset + BYTES);

        if (_isLittleEndian)
            BinaryPrimitives.writeInt24LittleEndian(_buffer, _offset, value);
        else
            BinaryPrimitives.writeInt24BigEndian(_buffer, _offset, value);

        advance(BYTES);

        return this;
    }

    /**
     * Writes a 32-bit integer to the stream, compressed depending on flags.
     *
     * @param value   Integer to write
     * @param force32 Whether or not to write as a 32-bit integer, regardless of compression
     *                flags.
     * @return This output stream
     */
    public final MemoryOutputStream i32(int value, boolean force32)
    {
        if (!force32 && ((_compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) != 0))
            return this.uleb128(Integer.toUnsignedLong(value));
        
        ensureCapacity(_offset + Integer.BYTES);
        if (_isLittleEndian) 
            BinaryPrimitives.writeInt32LittleEndian(_buffer, _offset, value);
        else
            BinaryPrimitives.writeInt32BigEndian(_buffer, _offset, value);

        advance(Integer.BYTES);

        return this;
    }

    /**
     * Writes a 32-bit signed integer to the stream, compressed depending on flags.
     * This function modifies the value written to the stream to fit an unsigned value, prefer i32
     *
     * @param value Signed integer to write
     * @return This output stream
     */
    public final MemoryOutputStream s32(int value)
    {
        if (((_compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) != 0))
            return this.uleb128(Integer.toUnsignedLong(value << 1 ^ (value >> 0x1f)));
            
        return this.i32(value, true);
    }

    /**
     * Writes a long as a 32-bit integer to the stream, compressed depending on flags.
     *
     * @param value   Integer to write
     * @param force32 Whether or not to write as a 32-bit integer, regardless of compression
     *                flags.
     * @return This output stream
     */
    public final MemoryOutputStream u32(long value, boolean force32)
    {
        if (!force32 && ((_compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) != 0))
            return this.uleb128(value & 0xFFFFFFFFL);

        ensureCapacity(_offset + Integer.BYTES);
        if (_isLittleEndian) 
            BinaryPrimitives.writeInt32LittleEndian(_buffer, _offset, (int) (value & 0xFFFFFFFF));
        else
            BinaryPrimitives.writeInt32BigEndian(_buffer, _offset, (int) (value & 0xFFFFFFFF));

        advance(Integer.BYTES);

        return this;
    }

    /**
     * Writes a long to the stream, compressed depending on flags.
     *
     * @param value   Long to write
     * @param force64 Whether or not to write as a 64-bit integer, regardless of compression
     *                flags.
     * @return This output stream
     */
    public final MemoryOutputStream u64(long value, boolean force64)
    {
        if (!force64 && ((_compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) != 0))
            return this.uleb128(value);

        ensureCapacity(_offset + Long.BYTES);
        if (_isLittleEndian) 
            BinaryPrimitives.writeInt64LittleEndian(_buffer, _offset, value);
        else
            BinaryPrimitives.writeInt64BigEndian(_buffer, _offset, value);
        
        advance(Long.BYTES);

        return this;
    }

    /**
     * Writes a 64-bit signed integer to the stream, compressed depending on flags.
     *
     * @param value   Long to write
     * @param force64 Whether or not to write as a 64-bit integer, regardless of compression
     *                flags.
     * @return This output stream
     */
    public final MemoryOutputStream s64(long value, boolean force64)
    {
        if (!force64 && ((_compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) != 0))
            return this.uleb128(value << 1L ^ (value >> 0x3f));
        return this.u64(value, true);
    }

    /**
     * Writes an integer to the stream.
     *
     * @param value Integer to write
     * @return This output stream
     */
    public final MemoryOutputStream i32(int value)
    {
        return this.i32(value, false);
    }

    /**
     * Writes a long as an unsigned integer to the stream.
     *
     * @param value Integer to write
     * @return This output stream
     */
    public final MemoryOutputStream u32(long value)
    {
        return this.u32(value, false);
    }

    /**
     * Writes a long to the stream.
     *
     * @param value Long to write
     * @return This output stream
     */
    public final MemoryOutputStream u64(long value)
    {
        return this.u64(value, false);
    }

    /**
     * Writes a "signed" long to the stream.
     *
     * @param value Long to write
     * @return This output stream
     */
    public final MemoryOutputStream s64(long value)
    {
        return this.s64(value, false);
    }

    /**
     * Writes a variable length quantity to the stream.
     *
     * @param value Long to write
     * @return This output stream
     */
    public final MemoryOutputStream uleb128(long value)
    {
        final int MAX_ULEB128_BYTE_OUTPUT = 5;
        ensureCapacity(_offset + MAX_ULEB128_BYTE_OUTPUT);

        int bytesWritten = 0;
        do
        {
            byte b = (byte) (value & 0x7f);
            value >>>= 7;
            if (value != 0L) b |= 0x80;
            _buffer[_offset + (bytesWritten++)] = b;
        } 
        while (value != 0);

        advance(bytesWritten);

        return this;
    }

    /**
     * Writes a 16-bit integer array to the stream.
     *
     * @param values Short array to write
     * @return This output stream
     */
    public final MemoryOutputStream shortarray(short[] values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (Short.BYTES * values.length));
        this.i32(values.length);
        for (short value : values)
            this.i16(value);
        return this;
    }

    /**
     * Writes a 32-bit integer array to the stream.
     *
     * @param values Integer array to write
     * @param signed Whether ot not to write signed integers
     * @return This output stream
     */
    public final MemoryOutputStream intarray(int[] values, boolean signed)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (Integer.BYTES * values.length));
        this.i32(values.length);
        for (int value : values)
        {
            if (signed) this.s32(value);
            else this.i32(value);
        }

        return this;
    }

    /**
     * Writes a 32-bit integer array to the stream.
     *
     * @param values Integer array to write
     * @return This output stream
     */
    public final MemoryOutputStream intarray(int[] values)
    {
        return this.intarray(values, false);
    }

    /**
     * Writes a 64-bit integer array to the stream.
     *
     * @param values Long array to write
     * @return This output stream
     */
    public final MemoryOutputStream longarray(long[] values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (Long.BYTES * values.length));
        this.i32(values.length);
        for (long value : values)
            this.u64(value);
        return this;
    }

    /**
     * Writes a GUID array to the stream.
     * 
     * @param values GUID array to write
     * @return This output stream
     */
    public final MemoryOutputStream guidarray(GUID[] values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (GUID.BYTES * values.length));
        this.i32(values.length);
        for (GUID value : values)
            this.guid(value);
        return this;
    }

    /**
     * Writes a hash array to the stream.
     * 
     * @param values Hash array to write
     * @return This output stream
     */
    public final MemoryOutputStream hasharray(SHA1[] values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (SHA1.BYTES * values.length));
        this.i32(values.length);
        for (SHA1 value : values)
            this.sha1(value);
        return this;
    }

    /**
     * Writes a GUID list to the stream.
     * 
     * @param values GUID list to write
     * @return This output stream
     */
    public final MemoryOutputStream guidlist(ArrayList<GUID> values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (GUID.BYTES * values.size()));
        this.i32(values.size());
        for (GUID value : values)
            this.guid(value);
        return this;
    }

    /**
     * Writes a hash list to the stream.
     * 
     * @param values Hash list to write
     * @return This output stream
     */
    public final MemoryOutputStream hashlist(ArrayList<SHA1> values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (SHA1.BYTES * values.size()));
        this.i32(values.size());
        for (SHA1 value : values)
            this.sha1(value);
        return this;
    }

    /**
     * Writes a 16 bit floating point number to the stream.
     * https://stackoverflow.com/questions/6162651/half-precision-floating-point-in-java
     *
     * @param value Float to write
     * @return This output stream
     */
    public final MemoryOutputStream f16(float value)
    {
        int fbits = Float.floatToIntBits(value);
        int sign = fbits >>> 16 & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;

        if (val >= 0x47800000)
        {
            if ((fbits & 0x7fffffff) >= 0x47800000)
            {
                if (val < 0x7f800000)
                    return this.u16(sign | 0x7c00);
                return this.u16(sign | 0x7c00 | (fbits & 0x007fffff) >>> 13);
            }
            return this.u16(sign | 0x7bff);
        }

        if (val >= 0x38800000)
            return this.u16(sign | val - 0x38000000 >>> 13);
        if (val < 0x33000000)
            return this.u16(sign);
        val = (fbits & 0x7fffffff) >>> 23;
        return this.u16(sign | ((fbits & 0x7fffff | 0x800000) + (0x800000 >>> val - 102) >>> 126 - val));
    }

    /**
     * Writes a 32 bit floating point number to the stream.
     *
     * @param value Float to write
     * @return This output stream
     */
    public final MemoryOutputStream f32(float value)
    {
        return this.i32(Float.floatToIntBits(value), true);
    }

    /**
     * Writes a 32-bit floating point number array to the stream.
     *
     * @param values Float array to write
     * @return This output stream
     */
    public final MemoryOutputStream floatarray(float[] values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (Float.BYTES * values.length));
        this.i32(values.length);
        for (float value : values)
            this.f32(value);
        return this;
    }

    /**
     * Writes a 2-dimensional floating point vector to the stream.
     *
     * @param value Vector2f to write
     * @return This output stream
     */
    public final MemoryOutputStream v2(Vector2f value)
    {
        if (value == null)
            return clear(8);

        this.f32(value.x);
        this.f32(value.y);
        return this;
    }

    /**
     * Writes a 3-dimensional floating point vector to the stream.
     *
     * @param value Vector3f to write
     * @return This output stream
     */
    public final MemoryOutputStream v3(Vector3f value)
    {
        if (value == null)
            return clear(12);

        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
        return this;
    }

    /**
     * Writes a 3-dimensional floating point vector to the stream.
     *
     * @param value Vector3f to write
     * @return This output stream
     */
    public final MemoryOutputStream v3(float x, float y, float z)
    {
        this.f32(x);
        this.f32(y);
        this.f32(z);
        return this;
    }

    /**
     * Writes a 3-dimensional floating point vector to the stream.
     *
     * @param value Vector3f to write
     * @return This output stream
     */
    public final MemoryOutputStream v3(Vector4f value)
    {
        if (value == null)
            return clear(12);

        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
        return this;
    }

    /**
     * Writes a 4-dimensional floating point vector to the stream.
     *
     * @param value Vector4f to write
     * @return This output stream
     */
    public final MemoryOutputStream v4(Vector4f value)
    {
        if (value == null)
            return clear(16);
        
        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
        this.f32(value.w);

        return this;
    }

    /**
     * Writes an array of 4-dimensional 32-bit floating point vectors to the stream.
     *
     * @param values Vector array to write
     * @return This output stream
     */
    public final MemoryOutputStream vectorarray(Vector4f[] values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (values.length * (Float.BYTES * 4)));
        this.i32(values.length);
        for (Vector4f value : values)
            this.v4(value);
        return this;
    }

    /**
     * Writes a Matrix4x4 to the stream, compressed depending on flags.
     *
     * @param value Matrix4x4 to write
     * @return This output stream
     */
    public final MemoryOutputStream m44(Matrix4f value)
    {
        if (value == null) value = new Matrix4f().identity();

        float[] values = new float[16];
        value.get(values);

        int flags = 0xFFFF;
        if ((_compressionFlags & CompressionFlags.USE_COMPRESSED_MATRICES) != 0)
        {
            flags = 0;
            for (int i = 0; i < 16; ++i)
                if (values[i] != IDENTITY[i])
                    flags |= (1 << i);
            this.i16((short) flags);
        }

        for (int i = 0; i < 16; ++i)
            if ((flags & (1 << i)) != 0)
                this.f32(values[i]);

        return this;
    }

    /**
     * Writes a string of fixed size to the stream.
     *
     * @param value String to write
     * @param size  Fixed size of string
     * @return This output stream
     */
    public final MemoryOutputStream str(String value, int size)
    {
        if (value == null) return this.bytes(new byte[size]);
        byte[] data = value.getBytes(StandardCharsets.US_ASCII);
        if (data.length > size)
            data = Arrays.copyOf(data, size);
        this.bytes(data);
        this.clear(size - data.length);
        return this;
    }

    /**
     * Writes a wide string of fixed size to the stream.
     *
     * @param value String to write
     * @param size  Fixed size of string
     * @return This output stream
     */
    public final MemoryOutputStream wstr(String value, int size)
    {
        size *= 2;
        if (value == null) return this.bytes(new byte[size]);
        byte[] string = value.getBytes(StandardCharsets.UTF_16BE);
        if (string.length > size)
            string = Arrays.copyOf(string, size);
        this.bytes(string);
        this.clear(size - string.length);
        return this;
    }

    /**
     * Writes a length-prefixed string to the stream.
     *
     * @param value String to write
     * @return This output stream
     */
    public final MemoryOutputStream str(String value)
    {
        if (value == null) return this.i32(0);
        byte[] string = value.getBytes(StandardCharsets.US_ASCII);
        this.s32(string.length);
        return this.bytes(string);
    }

    /**
     * Writes a length-prefixed wide string to the stream.
     *
     * @param value String to write
     * @return This output stream
     */
    public final MemoryOutputStream wstr(String value)
    {
        if (value == null) return this.i32(0);
        byte[] string = value.getBytes(StandardCharsets.UTF_16BE);
        this.s32(string.length / 2);
        return this.bytes(string);
    }

    /**
     * Writes a SHA1 hash to the stream.
     *
     * @param value SHA1 hash to write
     * @return This output stream
     */
    public final MemoryOutputStream sha1(SHA1 value)
    {
        if (value == null) return this.clear(0x14);
        return this.bytes(value.getHash());
    }

    /**
     * Writes a GUID (uint32_t) to the stream.
     *
     * @param value   GUID to write
     * @param force32 Whether or not to read as a 32 bit integer, regardless of compression flags.
     * @return This output stream
     */
    public final MemoryOutputStream guid(GUID value, boolean force32)
    {
        if (value == null) return this.u32(0, force32);
        return this.u32(value.getValue(), force32);
    }

    /**
     * Writes a GUID (uint32_t) to the stream.
     *
     * @param value GUID to write
     * @return This output stream
     */
    public final MemoryOutputStream guid(GUID value)
    {
        return this.guid(value, false);
    }


    /**
     * Writes an 8-bit enum value to the stream.
     *
     * @param <T>   Type of enum
     * @param value Enum value
     * @return This output stream
     */
    public final <T extends Enum<T> & ValueEnum<Byte>> MemoryOutputStream enum8(T value)
    {
        if (value == null) return this.u8(0);
        return this.i8(value.getValue().byteValue());
    }

    /**
     * Writes an 32-bit enum value to the stream.
     *
     * @param <T>   Type of enum
     * @param value Enum value
     * @return This output stream
     */
    public final <T extends Enum<T> & ValueEnum<Integer>> MemoryOutputStream enum32(T value)
    {
        if (value == null) return this.i32(0);
        return this.i32(value.getValue().intValue());
    }

    /**
     * Writes an 32-bit enum value to the stream.
     *
     * @param <T>    Type of enum
     * @param value  Enum value
     * @param signed Whether or not to write an s32
     * @return This output stream
     */
    public final <T extends Enum<T> & ValueEnum<Integer>> MemoryOutputStream enum32(T value,
                                                                                    boolean signed)
    {
        if (value == null) return this.i32(0);
        int v = value.getValue().intValue();
        if (signed) return this.s32(v);
        return this.i32(v);
    }

    /**
     * Writes an 32-bit enum value to the stream.
     *
     * @param <T>   Type of enum
     * @param value Enum value
     * @return This output stream
     */
    public final <T extends Enum<T> & ValueEnum<Byte>> MemoryOutputStream enumarray(T[] values)
    {
        if (values == null) return this.i32(0);
        ensureCapacity(_offset + Integer.BYTES + (Byte.BYTES * values.length));
        this.i32(values.length);
        for (T value : values)
            this.enum8(value);
        return this;
    }

    /**
     * Writes a series of null characters to the stream.
     *
     * @param size Number of bytes to write
     * @return This output stream
     */
    public final MemoryOutputStream clear(int size)
    {
        ensureCapacity(_offset + size);
        if (_offset < _length)
            Arrays.fill(_buffer, _offset, _offset + size, (byte)0);
        advance(size);
        return this;
    }

    /**
     * Shrinks the size of the buffer to the current offset.
     *
     * @return This output stream
     */
    public final MemoryOutputStream shrink()
    {
        _buffer = Arrays.copyOfRange(_buffer, 0, _length);
        return this;
    }

    public byte[] flush()
    {
        shrink();
        return _buffer;
    }
    
    /**
     * Seeks to position relative to seek mode.
     *
     * @param offset Offset relative to seek position
     * @param mode   Seek origin
     */
    public final void seek(int offset, SeekMode mode)
    {
        if (mode == null)
            throw new NullPointerException("SeekMode cannot be null!");
        
        switch (mode)
        {
            case Begin: _offset = offset; break;
            case Relative: _offset += offset; break;
            case End: _offset = _length + offset; break;
        }
    }

    /**
     * Seeks ahead in stream relative to offset.
     *
     * @param offset Offset to go to
     */
    public final void seek(int offset)
    {
        this.seek(offset, SeekMode.Relative);
    }

    public final void align(int a)
    {
        if ((_offset % a) != 0)
            clear(a - (_offset % a));
    }

    private final void advance(int offset)
    {
        _offset += offset;
        if (_offset > _length)
            _length = _offset;
    }

    public final void setLength(int length)
    {
        ensureCapacity(length);
        _length = length;
    }

    public final byte[] getBuffer()
    {
        return _buffer;
    }

    public final int getOffset()
    {
        return _offset;
    }

    public final int getLength()
    {
        return _length;
    }

    public final int getCapacity()
    {
        return _buffer.length;
    }

    public final byte getCompressionFlags()
    {
        return _compressionFlags;
    }

    public final boolean isLittleEndian()
    {
        return _isLittleEndian;
    }

    public final void setLittleEndian(boolean value)
    {
        _isLittleEndian = value;
    }
}
