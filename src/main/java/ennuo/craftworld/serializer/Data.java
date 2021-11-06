package ennuo.craftworld.memory;

import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.enums.RType;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Data {
    /**
     * Max revision before starting to use variable length integers and matrices.
     */
    public static int ENCODED_REVISION = 0x271;

    public String path;

    public byte[] data;
    public int offset;
    public int length;
    
    public int revision = 0x271;
    
    /**
     * Checks if the revision of the output indicates that it should use variable length types.
     * @return True if the resource is encoded
     */
    public boolean isEncoded() {
        return this.revision > Output.ENCODED_REVISION && !(this.revision >= 0x273 && this.revision <= 0x297);
    }

    /**
     * Creates a memory input stream from byte array
     * @param data Byte array to use as source
     */
    public Data(byte[] data) { this.setData(data); }
    
    /**
     * Creates a memory input stream from byte array with specified revision.
     * @param data Byte array to use as source
     * @param revision Revision of the stream
     */
    public Data(byte[] data, int revision) {
        this.setData(data);
        this.revision = revision;
    }

    /**
     * Creates a memory input stream from file at path.
     * @param path Path of the file to read
     */
    public Data(String path) {
        this.path = path;
        byte[] data = FileIO.read(path);
        this.setData(data);
    }

    /**
     * Creates a memory input stream from file at path with specified revision.
     * @param path Path of the file to read
     * @param revision Revision of the stream
     */
    public Data(String path, int revision) {
        this.path = path;
        this.revision = revision;
        byte[] data = FileIO.read(path);
        this.setData(data);
    }

    /**
     * Resets this stream with specified buffer.
     * @param buffer Data buffer to use as a stream source
     */
    public void setData(byte[] buffer) {
        this.data = buffer;
        if (this.data != null)
            this.length = buffer.length;
        else this.length = 0;
        this.offset = 0;
    }

    /**
     * Reads an arbitrary number of bytes from the stream.
     * @return Bytes read from the stream
     */
    public byte[] bytes(int size) {
        this.offset += size;
        if ((this.offset >= (data.length + 1)) || size == 0) 
            return new byte[] {};
        return Arrays.copyOfRange(this.data, this.offset - size, this.offset);
    }

    /**
     * Reads a boolean from the stream.
     * @return Boolean read from the stream
     */
    public boolean bool() { return this.i8() != 0; }

    /**
     * Reads a byte from the stream.
     * @return Byte read from the stream
     */
    public byte i8() {
        this.offset++;
        return this.data[this.offset - 1];
    }
    
    /**
     * Reads an array of bytes from the stream
     * @return Array of bytes read from the stream
     */
    public byte[] i8a() {
        byte[] values = new byte[this.i32()];
        for (int i = 0; i < values.length; ++i)
            values[i] = this.i8();
        return values;
    }

    /**
     * Peeks at the next integer in the stream without advancing.
     * @return Integer read from the stream
     */
    public int peek() {
        int offset = this.offset;
        int value = this.i32();
        this.seek(offset);
        return value;
    }
    
    /**
     * Reads a 16 bit floating point number from the stream.
     * @return Float read from the stream
     */
    public float f16() {
        short half = this.i16();
        return Float.intBitsToFloat(((half & 0x8000) << 16) | (((half & 0x7c00) + 0x1C000) << 13) | ((half & 0x03FF) << 13));
    }

    /**
     * Reads a short from the stream.
     * @return Short read from the stream
     */
    public short i16() {
        byte[] buffer = this.bytes(2);
        return (short)((buffer[0] << 8) | buffer[1] & 0xFF);
    }

    /**
     * Reads a short from the stream in little endian.
     * @return Short read from the stream
     */
    public int u16() {
        byte[] buffer = this.bytes(2);
        return buffer[0] << 8 & 0xFF00 | buffer[1] & 0xFF;
    }
    
    /**
     * Reads a 24-bit integer from the stream
     * @return Integer read from the stream
     */
    public int i24() {
        byte[] buffer = this.bytes(3);
        return (buffer[0] & 0xFF) << 16 | (buffer[1] & 0xFF) << 8 | buffer[2] & 0xFF;
    }

    /**
     * Reads a 32-bit integer from the stream, encoded depending on the revision.
     * @return Integer read from the stream
     */
    public int i32() {
        if (this.isEncoded()) 
            return (int) (this.varint() & 0xFFFFFFFF);
        return this.i32f();
    }
    
    /**
     * Reads an array of unsigned 32-bit integers from the stream, encoded depending on the revision.
     * @return Array of unsigned integers read from the stream
     */
    public long[] u32a() {
        long[] values = new long[this.i32()];
        for (int i = 0; i < values.length; ++i)
            values[i] = this.u32();
        return values;
    }

    /**
     * Reads a long as an unsigned integer from the stream, encoded depending on the revision.
     * @return Unsigned integer read from the stream
     */
    public long u32() {
        if (this.isEncoded()) return this.varint();
        return this.u32f();
    }

    /**
     * Reads an integer as an unsigned short from the stream.
     * @return Unsigned short read from the stream
     */
    public int i32f() {
        byte[] buffer = this.bytes(4);
        return buffer[0] << 24 | (buffer[1] & 0xFF) << 16 | (buffer[2] & 0xFF) << 8 | buffer[3] & 0xFF;
    }

    /**
     * Reads a long as an unsigned integer from the stream as 32-bit regardless of revision.
     * @return Unsigned integer read from the stream
     */
    public long u32f() {
        return ByteBuffer.wrap(this.bytes(4))
            .order(ByteOrder.BIG_ENDIAN)
            .getInt() & 0xFFFFFFFFL;
    }

    /**
     * Reads a 32 bit floating point number from the stream.
     * @return Float read from the stream
     */
    public float f32() { return Float.intBitsToFloat(this.i32f()); }

    /**
     * Reads an array of 32 bit floating point numbers from the stream.
     * @return Array of floats read from the stream
     */
    public float[] f32a() {
        float[] values = new float[this.i32()];
        for (int i = 0; i < values.length; ++i)
            values[i] = this.f32();
        return values;
    }

    /**
     * Reads a 2-dimensional floating point vector from the stream.
     * @return Vector2f read from the stream
     */
    public Vector2f v2() { return new Vector2f(this.f32(), this.f32()); }

    /**
     * Reads a 3-dimensional floating point vector from the stream.
     * @return Vector3f read from the stream
     */
    public Vector3f v3() { return new Vector3f(this.f32(), this.f32(), this.f32()); }

    /**
     * Reads a 4-dimensional floating point vector from the stream.
     * @return Vector4f read from the stream
     */
    public Vector4f v4() { return new Vector4f(this.f32(), this.f32(), this.f32(), this.f32()); }

    /**
     * Reads a Matrix4x4 from the stream, encoded depending on the revision.
     * @return Matrix4x4 read from the stream
     */
    public float[] matrix() {
        float[] matrix = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        int flags = 0xFFFF;
        if (this.isEncoded()) 
            flags = this.i16();
        for (int i = 0; i < 16; ++i)
            if (((flags >>> i) & 1) != 0)
                matrix[i] = this.f32();
        return matrix;
    }

    /**
     * Reads a resource reference from the stream with a short flag.
     * @param type Type of resource reference
     * @return Resource reference read from the stream
     */
    public ResourcePtr resource(RType type) { return this.resource(type, false); }

    /**
     * Reads a resource reference from the stream
     * @param rType Type of resource reference
     * @param useSingleByteFlag Whether the resource reference should be read with a byte or a short flag
     * @return Resource reference read from the stream
     */
    public ResourcePtr resource(RType rType, boolean useSingleByteFlag) {
        byte HASH = 1, GUID = 2;
        if (this.revision <= 0x18B) {
            HASH = 2;
            GUID = 1;
        }

        byte type;

        if (this.revision < 0x230) useSingleByteFlag = true;
        if (((this.revision >= 0x230 && this.revision <= 0x26e) || (this.revision >= 0x273 && this.revision <= 0x297)) && !useSingleByteFlag) 
            this.i8();

        if (useSingleByteFlag) type = this.i8();
        else if (this.isEncoded()) type = (byte) this.i16();
        else type = (byte) this.i32();

        ResourcePtr resource = new ResourcePtr();
        resource.type = rType;

        if (type == GUID) resource.GUID = this.u32();
        else if (type == HASH) resource.hash = this.bytes(0x14);
        else return null;

        return resource;
    }

    /**
     * Reads a 7-bit encoded long from the stream.
     * @return Long value read from the stream
     */
    public long varint() {
        long result = 0, i = 0;
        while (this.offset + i < this.length) {
            long b = (long) (this.i8() & 0xFFL);
            result |= (b & 0x7FL) << 7L * i;
            if ((b & 0x80L) == 0L)
                break;
            i++;
        }
        return result;
    }

    /**
     * Reads a string of specified size from the stream.
     * @param size Size of string to read
     * @return String value read from the stream
     */
    public String str(int size) {
        if (size == 0) return "";
        return new String(this.bytes(size)).replace("\0", "");
    }

    /**
     * Reads a UTF-16 character array from the stream.
     * @return UTF-16 string value read from the stream
     */
    public String str16() {
        int size = this.i32();
        if (!this.isEncoded()) size *= 2;
        byte[] data = this.bytes(size);
        return new String(data, Charset.forName("UTF-16BE"));
    }

    /**
     * Reads a character array from the stream.
     * @return UTF-8 string read from the stream
     */
    public String str8() {
        int size = this.i32();
        if (this.isEncoded()) size /= 2;
        return this.str(size);
    }

    /**
     * Overwrites bytes from current offset.
     * @param values Byte values to write
     */
    public void overwrite(byte[] values) {
        for (int i = 0; i < values.length; i++)
            this.data[this.offset + i] = values[i];
    }

    /**
     * Sets offset in stream to specified position
     * @param pos Position in stream to seek to
     */
    public void seek(int pos) { this.offset = pos; }

    /**
     * Moves forward by number of bytes
     * @param count Number of bytes to skip
     */
    public void forward(int count) { this.offset += count; }

    /**
     * Moves backward by a number of bytes
     * @param count Number of bytes to rewind
     */
    public void rewind(int count) { this.offset -= count; }
}
