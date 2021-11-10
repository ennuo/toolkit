package ennuo.craftworld.serializer;

import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.utilities.Bytes;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Output {
    public byte[] buffer;
    public int offset;
    public int length;
    
    public int revision = 0x271;
    public int branchDescription;
    public byte compressionFlags = 0;

    public ArrayList<ResourceDescriptor> dependencies = new ArrayList<ResourceDescriptor>();

    /**
     * Creates a memory output stream with specified size.
     * @param size Size of buffer to create
     */
    public Output(int size) { 
        this.buffer = new byte[size]; 
        this.length = size;
    }
    
    /**
     * Creates a memory output stream with specified size and revision.
     * @param size Size of buffer to create
     * @param revision Revision of stream
     */
    public Output(int size, int revision) {
        this.buffer = new byte[size];
        this.revision = revision;
        this.length = size;
        
        // NOTE(Abz): For legacy reasons.
        if (this.revision == 0x272 || this.revision > 0x297)
            this.compressionFlags = 0x7;
    }
    
    /**
     * Creates a memory output stream with specified size and revision.
     * @param size Size of buffer to create
     * @param revision Revision of stream
     * @param branch Branch descriptor of stream
     */
    public Output(int size, int revision, int branch) {
        this.buffer = new byte[size];
        this.revision = revision;
        this.length = size;
        this.branchDescription = branch;
        
        // NOTE(Abz): For legacy reasons.
        if ((this.revision == 0x272 && this.branchDescription != 0) || this.revision > 0x297)
            this.compressionFlags = 0x7;
    }

    /**
     * Checks whether or not a resource is already marked as a dependency.
     * @param dependency Dependency
     * @return True if the dependency already exists
     */
    private boolean hasDependency(ResourceDescriptor dependency) {
        for (ResourceDescriptor descriptor: this.dependencies)
            if (descriptor.equals(dependency))
                return true;
        return false;
    }
    
    /**
     * Writes a UTF-16 character array to the stream.
     * @param value String value to write
     * @return This output stream
     */
    public Output str16(String value) {
        if (value == null || value.equals("")) { this.i32(0); return this; }
        int size = value.length();
        if ((this.compressionFlags & Data.USE_COMPRESSED_INTEGERS) != 0) size *= 2;
        this.i32(size);
        this.bytes(value.getBytes(StandardCharsets.UTF_16BE));
        return this;
    }

    /**
     * Writes a character array to the stream.
     * @param value String value to write
     * @return This output stream
     */
    public Output str8(String value) {
        if (value == null || value.equals("")) { this.i32(0); return this; }
        int size = value.length();
        if ((this.compressionFlags & Data.USE_COMPRESSED_INTEGERS) != 0) size *= 2;
        this.i32(size);
        this.str(value);
        return this;
    }
    
    /**
     * Writes a string to the stream.
     * @param value String value to write
     * @return This output stream
     */
    public Output str(String value) { return this.bytes(value.getBytes()); }
    
    /**
     * Writes a fixed string with padded size to the stream.
     * @param value String value to write
     * @param size Fixed size of string
     * @return This output stream
     */
    public Output str(String value, int size) {
        this.str(value);
        this.pad(size - value.length());
        return this;
    }
    
    /**
     * Writes an arbitrary number of bytes to the stream.
     * @param bytes Bytes to write
     * @return This output stream
     */
    public Output bytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++offset, ++i)
            buffer[offset] = bytes[i];
        return this;
    }

    /**
     * Writes a boolean to the stream.
     * @param value Boolean to write
     * @return This output stream
     */
    public Output bool(boolean value) { return this.u8(value == true ? 1 : 0); }

    /**
     * Writes a series of null characters to the stream.
     * @param size Number of bytes to write
     * @return This output stream
     */
    public Output pad(int size) {
        for (int i = 0; i < size; ++i)
            this.i8((byte) 0);
        return this;
    }

    /**
     * Writes an integer as a byte to the stream.
     * @param value Byte to write
     * @return This output stream
     */
    public Output u8(int value) { return this.i8((byte) value); }
    
    /**
     * Writes a byte to the stream.
     * @param value Byte to write
     * @return This output stream
     */
    public Output i8(byte value) {
        buffer[offset] = value;
        offset += 1;
        return this;
    }
    
    /**
     * Writes an array of bytes to the stream.
     * @param values Bytes to write
     * @return This output stream
     */
    public Output i8a(byte[] values) {
        if (values == null) { this.i32(0); return this; }
        this.i32(values.length);
        for (byte value : values)
            this.i8(value);
        return this;
    }

    /**
     * Writes a 32-bit integer to the stream, encoded depending on the revision.
     * @param value Integer to write
     * @return This output stream
     */
    public Output i32(int value) {
        if ((this.compressionFlags & Data.USE_COMPRESSED_INTEGERS) != 0)
            return this.varint(value);
        else return this.i32f(value);
    }
    
    /**
     * Writes an array of 32-bit integers to the stream, encoded depending on the revision.
     * @param values Integers to write
     * @return This output stream
     */
    public Output i32a(int[] values) {
        this.i32(values.length);
        for (int value : values)
            this.i32(value);
        return this;
    }
    
    /**
     * Writes an array of unsigned 32-bit integers to the stream, encoded depending on the revision.
     * @param values Integers to write
     * @return This output stream
     */
    public Output u32a(long[] values) {
        this.i32(values.length);
        for (long value : values)
            this.u32(value);
        return this;
    }

    /**
     * Writes a long as an unsigned integer to the stream, encoded depending on the revision.
     * @param value Integer to write
     * @return This output stream
     */
    public Output u32(long value) {
        if ((this.compressionFlags & Data.USE_COMPRESSED_INTEGERS) != 0)
            return this.varint(value);
        else return this.u32f(value);
    }
    
    /**
     * Writes a long as an unsigned integer to the stream in little endian.
     * @param value Integer to write
     * @return This output stream
     */
    public Output u32LE(long value) {
        if ((this.compressionFlags & Data.USE_COMPRESSED_INTEGERS) != 0)
            return this.varint(value);
        else return this.u32LEf(value);
    }
    
    /**
     * Writes a short to the stream
     * @param value Short to write
     * @return This output stream
     */
    public Output i16(short value) { return this.bytes(Bytes.toBytes(value)); }
    
    /**
     * Writes an array of shorts to the stream.
     * @param values Shorts to write
     * @return This output stream
     */
    public Output i16a(short[] values) {
        this.i32(values.length);
        for (short value : values)
            this.i16(value);
        return this;
    }
    
    /**
     * Writes a short to the stream in little endian.
     * @param value Short to write
     * @return This output stream
     */
    public Output i16LE(short value) {
        byte[] bytes = Bytes.toBytes(value);
        this.i8(bytes[1]); this.i8(bytes[0]);
        return this;
    }

    /**
     * Writes an integer to the stream as 32-bit regardless of revision.
     * @param value Integer to write
     * @return This output stream
     */
    public Output i32f(int value) { return this.bytes(Bytes.toBytes(value)); }

    /**
     * Writes a long as an unsigned integer to the stream as 32-bit regardless of revision.
     * @param value Unsigned integer to write
     * @return This output stream
     */
    public Output u32f(long value) { return this.bytes(Bytes.toBytes(value)); }
    
    /**
     * Writes a long as an unsigned integer to the stream as 32-bit in little endian regardless of revision.
     * @param value Unsigned integer to write
     * @return This output stream
     */
    public Output u32LEf(long value) { return this.bytes(Bytes.toBytesLE(value)); }

    /**
     * Writes a 7-bit encoded integer to the stream.
     * @param value Integer to write
     * @return This output stream
     */
    public Output varint(int value) { return this.varint((long) value); }
    
    /**
     * Writes a 7-bit encoded long to the stream.
     * @param value Long to write
     * @return This output stream
     */
    public Output varint(long value) {
        if (value == -1 || value == Long.MAX_VALUE) {
            this.bytes(new byte[] {  (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x0F });
            return this;
        } else if (value == 0) {
            this.i8((byte) 0);
            return this;
        }
        while (value > 0) {
            byte b = (byte)(value & 0x7fL);
            value >>= 7L;
            if (value > 0L) b |= 128L;
            this.i8(b);
        }
        return this;
    }
    
    /**
     * Writes a Matrix4x4 to the stream, encoded depending on the revision.
     * @param value Matrix to write
     * @return This output stream
     */
    public Output matrix(Matrix4f value) {
        float[] identity = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        float[] values = new float[16];
        value.get(values);
        int flags = 0xFFFF;
        if ((this.compressionFlags & Data.USE_COMPRESSED_MATRICES) != 0) {
            flags = 0;
            for (int i = 0; i < 16; ++i)
                if (values[i] != identity[i])
                    flags |= (1 << i);
            this.i16((short) flags);
        }

        for (int i = 0; i < 16; ++i)
            if (((flags >>> i) & 1) != 0)
                this.f32(values[i]);
        
        return this;
    }

    /**
     * Writes a resource reference to the stream with a short flag.
     * @param value Resource reference to write
     * @return This output stream
     */
    public Output resource(ResourceDescriptor value) { return this.resource(value, false); }
    
    /**
     * Writes a resource reference to the stream with either a byte or short flag.
     * @param value Resource reference to write
     * @param skipFlags Whether resource flags should be written
     * @return This output stream
     */
    public Output resource(ResourceDescriptor value, boolean skipFlags) {
        byte HASH = 1, GUID = 2;
        if (this.revision <= 0x18B) {
            HASH = 2;
            GUID = 1;
        }
        
        if (this.revision > 0x22e && !skipFlags)
            this.i32(value.flags);
        
        if (value.hash != null) {
            this.i8(HASH);
            this.bytes(value.hash);
        } else if (value.GUID != -1) {
            this.i8(GUID);
            this.u32(value.GUID);
        } else this.i8((byte) 0);
        
        if (!this.hasDependency(value))
            this.dependencies.add(value);
        
        return this;
    }

    /**
     * Writes a 32 bit floating point number to the stream.
     * @param value Float to write 
     * @return This output stream
     */
    public Output f32(float value) { 
        return this.bytes(Bytes.toBytes(Float.floatToIntBits(value))); 
    }
    
    /**
     * Writes a 32 bit floating point number to the stream in little endian.
     * @param value Float to write
     * @return This output stream
     */
    public Output f32LE(float value) {
        int bits = Float.floatToRawIntBits(value);
        byte[] byteBuffer = ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(bits)
                .array();
        this.bytes(byteBuffer);
        return this;
    }

    /**
     * Writes an array of 32 bit floating point numbers to the stream.
     * @param values Floats to write
     * @return This output stream
     */
    public Output f32a(float[] values) {
        if (values == null) { this.i32(0); return this; }
        this.i32(values.length);
        for (float value : values)
            this.f32(value);
        return this;
    }

    /**
     * Writes a 2-dimensional floating point vector to the stream.
     * @param value Vector2f to write
     * @return This output stream
     */
    public Output v2(Vector2f value) { 
        this.f32(value.x); 
        this.f32(value.y); 
        return this;
    }
    
    /**
     * Writes a 3-dimensional floating point vector to the stream.
     * @param value Vector3f to write
     * @return This output stream
     */
    public Output v3(Vector3f value) {
        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
        return this;
    }

    /**
     * Writes a 4-dimensional floating point vector to the stream.
     * @param value Vector4f to write
     * @return This output stream
     */
    public Output v4(Vector4f value) {
        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
        this.f32(value.w);
        return this;
    }

    /**
     * Shrinks the size of the buffer to the current offset.
     * @return This output stream
     */
    public Output shrink() { 
        this.buffer = Arrays.copyOfRange(this.buffer, 0, this.offset); 
        return this;
    }
}
