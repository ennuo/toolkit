package ennuo.craftworld.serializer;

import ennuo.craftworld.resources.enums.CompressionFlags;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.SHA1;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Data {
    public String path;

    public byte[] data;
    public int offset;
    public int length;
    
    public Revision revision = new Revision(0x271);
    public byte compressionFlags = 0;

    /**
     * Creates a memory input stream from byte array
     * @param data Byte array to use as source
     */
    public Data(byte[] data) { 
        this.setData(data);
    }
    
    /**
     * Creates a memory input stream from byte array with specified revision.
     * @param data Byte array to use as source
     * @param revision Revision of the stream
     */
    public Data(byte[] data, int revision) {
        this.setData(data);
        this.revision = new Revision(revision);
        
        // NOTE(Aidan): For legacy reasons.
        if (this.revision.head == 0x272 || this.revision.head > 0x297)
            this.compressionFlags = 0x7;
    }

    /**
     * Creates a memory input stream from byte array with specified revision.
     * @param data Byte array to use as source
     * @param revision Revision of the stream
     * @param branch Branch descriptor of the stream.
     */
    public Data(byte[] data, int revision, int branch) {
        this.setData(data);
        this.revision = new Revision(revision, branch >> 0x10, branch & 0xFFFF);
        
        // NOTE(Aidan): For legacy reasons.
        if ((this.revision.head == 0x272 && this.revision.branchID != 0) || this.revision.head > 0x297)
            this.compressionFlags = 0x7;
    }
    
    /**
     * Creates a memory input stream from byte array with specified revision.
     * @param data Byte array to use as source
     * @param revision Revision of the stream
     * @param branchID ID of branch of this stream.
     * @param branchRevision Revision of the branch.
     */
    public Data(byte[] data, int revision, int branchID, int branchRevision) {
        this.setData(data);
        this.revision = new Revision(revision, branchID, branchRevision);
        
        // NOTE(Aidan): For legacy reasons.
        if ((this.revision.head == 0x272 && this.revision.branchID != 0) || this.revision.head > 0x297)
            this.compressionFlags = 0x7;
    }
    
    /**
     * Creates a memory input stream from byte array with specified revision.
     * @param data Byte array to use as source
     * @param revision Revision of the stream
     */
    public Data(byte[] data, Revision revision) {
        this.setData(data);
        this.revision = revision;
        
        // NOTE(Aidan): For legacy reasons.
        if ((this.revision.head == 0x272 && this.revision.branchID != 0) || this.revision.head > 0x297)
            this.compressionFlags = 0x7;
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
        this.revision = new Revision(revision);
        byte[] data = FileIO.read(path);
        this.setData(data);
        
        // NOTE(Aidan): For legacy reasons.
        if (this.revision.head == 0x272 || this.revision.head > 0x297)
            this.compressionFlags = 0x7;
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
     * Reads an array of shorts from the stream.
     * @return Array of shorts read from the stream
     */
    public short[] i16a() {
        short[] values = new short[this.i32()];
        for (int i = 0; i < values.length; ++i)
            values[i] = this.i16();
        return values;
    }

    /**
     * Reads an unsigned short from the stream.
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
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) == 0)
            return this.i32f();
        return (int) (this.varint() & 0xFFFFFFFF);
    }
    
    /**
     * Reads a 64-bit long from the stream, encoded depending on the revision.
     * @return Long read from the stream
     */
    public long i64() {
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) == 0)
            return this.i64f();
        return this.varint();
    }
    
    /**
     * Reads a long from the stream.
     * @return Long read from the stream
     */
    public long i64f() {
        byte[] buffer = this.bytes(8);
        return (buffer[0] & 0xFFL) << 56L |
               (buffer[1] & 0xFFL) << 48L |
               (buffer[2] & 0xFFL) << 40L |
               (buffer[3] & 0xFFL) << 32L |
               (buffer[4] & 0xFFL) << 24L |
               (buffer[5] & 0xFFL) << 16L |
               (buffer[6] & 0xFFL) << 8L |
               (buffer[7] & 0xFFL) << 0L;
    }
    
    /**
     * Reads an array of 32-bit integers from the stream, encoded depending on the revision.
     * @return Array of integers read from the stream
     */
    public int[] i32a() {
        int[] values = new int[this.i32()];
        for (int i = 0; i < values.length; ++i)
            values[i] = this.i32();
        return values;
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
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) == 0)
            return this.u32f();
        return this.varint();
    }

    /**
     * Reads an integer from the stream.
     * @return Integer read from the stream
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
    public Matrix4f matrix() {
        float[] matrix = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        int flags = 0xFFFF;
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_MATRICES) != 0)
            flags = this.i16();
        for (int i = 0; i < 16; ++i)
            if (((flags >>> i) & 1) != 0)
                matrix[i] = this.f32();
        
        Matrix4f mat = new Matrix4f();
        mat.set(matrix);
        return mat;
    }
    
    /**
     * Reads a SHA1 hash from the stream.
     * @return SHA1 hash read from the stream
     */
    public SHA1 sha1() { return new SHA1(this.bytes(0x14)); }

    /**
     * Reads a resource reference from the stream with a short flag.
     * @param type Type of resource reference
     * @return Resource reference read from the stream
     */
    public ResourceDescriptor resource(ResourceType type) { return this.resource(type, false); }

    /**
     * Reads a resource reference from the stream
     * @param resourceType Type of resource reference
     * @param skipFlags Whether or not to skip resource flags in parsing
     * @return Resource reference read from the stream
     */
    public ResourceDescriptor resource(ResourceType resourceType, boolean skipFlags) {
        byte HASH = 1, GUID = 2;
        if (this.revision.head <= 0x18B) {
            HASH = 2;
            GUID = 1;
        }

        byte type = 0; int flags = 0;
        
        if (this.revision.head > 0x22e && !skipFlags) flags = this.i32();
        type = this.i8();
        
        ResourceDescriptor resource = new ResourceDescriptor();
        resource.type = resourceType;
        resource.flags = flags;

        if (type == GUID) resource.GUID = this.u32();
        else if (type == HASH) resource.hash = this.sha1();
        else return null;

        return resource;
    }

    /**
     * Reads a 7-bit encoded long from the stream.
     * @return Long value read from the stream
     */
    public long varint() {
        long result = 0, i = 0;
        while (true) {
            long b = (long) (this.i8() & 0xFFL);
            result |= (b & 0x7FL) << 7L * i;
            if ((b & 0x80L) == 0L)
                break;
            i++;
        }
        return result >>> 0;
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
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) == 0) size *= 2;
        byte[] data = this.bytes(size);
        return new String(data, Charset.forName("UTF-16BE"));
    }

    /**
     * Reads a character array from the stream.
     * @return UTF-8 string read from the stream
     */
    public String str8() {
        int size = this.i32();
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) != 0) size /= 2;
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
