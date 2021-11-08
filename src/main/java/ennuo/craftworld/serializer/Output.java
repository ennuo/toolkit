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
    /**
     * Max revision before starting to use variable length integers and matrices.
     */
    public static int ENCODED_REVISION = 0x271;
    
    public int offset = 0;
    public int revision = 0x271;
    public byte[] buffer;

    public ArrayList<ResourceDescriptor> dependencies = new ArrayList<ResourceDescriptor>();

    /**
     * Creates a memory output stream with specified size.
     * @param size Size of buffer to create
     */
    public Output(int size) { this.buffer = new byte[size]; }
    
    /**
     * Creates a memory output stream with specified size and revision.
     * @param size Size of buffer to create
     * @param revision Revision of stream
     */
    public Output(int size, int revision) {
        this.buffer = new byte[size];
        this.revision = revision;
    }
    
    /**
     * Checks if the revision of the output indicates that it should use variable length types.
     * @return True if the resource is encoded
     */
    public boolean isEncoded() {
        return this.revision > Output.ENCODED_REVISION && !(this.revision >= 0x273 && this.revision <= 0x297);
    }

    /**
     * Checks whether or not a resource is already marked as a dependency.
     * @param dependency Dependency
     * @return True if the dependency already exists
     */
    private boolean hasDependency(ResourceDescriptor dependency) {
        for (ResourceDescriptor ptr: this.dependencies)
            if (ptr.equals(dependency))
                return true;
        return false;
    }
    
    /**
     * Writes a UTF-16 character array to the stream.
     * @param value String value to write
     */
    public void str16(String value) {
        if (value == null || value.equals("")) { this.i32(0); return; }
        int size = value.length();
        if (this.isEncoded()) size *= 2;
        this.i32(size);
        this.bytes(value.getBytes(StandardCharsets.UTF_16BE));
    }

    /**
     * Writes a character array to the stream.
     * @param value String value to write
     */
    public void str8(String value) {
        if (value == null || value.equals("")) { this.i32(0); return; }
        int size = value.length();
        if (this.isEncoded()) size *= 2;
        this.i32(size);
        this.str(value);
    }
    
    /**
     * Writes a string to the stream.
     * @param value String value to write
     */
    public void str(String value) { this.bytes(value.getBytes()); }
    
    /**
     * Writes a fixed string with padded size to the stream.
     * @param value String value to write
     * @param size Fixed size of string
     */
    public void str(String value, int size) {
        this.str(value);
        this.pad(size - value.length());
    }
    
    /**
     * Writes an arbitrary number of bytes to the stream.
     * @param bytes Bytes to write
     */
    public void bytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; ++offset, ++i)
            buffer[offset] = bytes[i];
    }

    /**
     * Writes a boolean to the stream.
     * @param value Boolean to write
     */
    public void bool(boolean value) { this.u8(value == true ? 1 : 0); }

    /**
     * Writes a series of null characters to the stream.
     * @param size Number of bytes to write
     */
    public void pad(int size) {
        for (int i = 0; i < size; ++i)
            this.i8((byte) 0);
    }

    /**
     * Writes an integer as a byte to the stream.
     * @param value Byte to write
     */
    public void u8(int value) { this.i8((byte) value); }
    
    /**
     * Writes a byte to the stream.
     * @param value Byte to write
     */
    public void i8(byte value) {
        buffer[offset] = value;
        offset += 1;
    }
    
    /**
     * Writes an array of bytes to the stream.
     * @param values Bytes to write
     */
    public void i8a(byte[] values) {
        if (values == null) { this.i32(0); return; }
        this.i32(values.length);
        for (byte value : values)
            this.i8(value);
    }

    /**
     * Writes a 32-bit integer to the stream, encoded depending on the revision.
     * @param value Integer to write
     */
    public void i32(int value) {
        if (this.isEncoded())
            this.varint(value);
        else this.i32f(value);
    }
    
    /**
     * Writes an array of unsigned 32-bit integers to the stream, encoded depending on the revision.
     * @param values Integers to write
     */
    public void u32a(long[] values) {
        this.i32(values.length);
        for (long value : values)
            this.u32(value);
    }

    /**
     * Writes a long as an unsigned integer to the stream, encoded depending on the revision.
     * @param value Integer to write
     */
    public void u32(long value) {
        if (this.isEncoded())
            this.varint(value);
        else this.u32f(value);
    }
    
    /**
     * Writes a long as an unsigned integer to the stream in little endian.
     * @param value Integer to write
     */
    public void u32LE(long value) {
        if (this.isEncoded())
            this.varint(value);
        else this.u32LEf(value);
    }
    
    /**
     * Writes a short to the stream
     * @param value Short to write
     */
    public void i16(short value) { this.bytes(Bytes.toBytes(value)); }
    
    /**
     * Writes an array of shorts to the stream.
     * @param values Shorts to write
     */
    public void i16a(short[] values) {
        this.i32(values.length);
        for (short value : values)
            this.i16(value);
    }
    
    /**
     * Writes a short to the stream in little endian.
     * @param value Short to write
     */
    public void i16LE(short value) {
        byte[] bytes = Bytes.toBytes(value);
        this.i8(bytes[1]); this.i8(bytes[0]);
    }

    /**
     * Writes an integer to the stream as 32-bit regardless of revision.
     * @param value Integer to write
     */
    public void i32f(int value) { this.bytes(Bytes.toBytes(value)); }

    /**
     * Writes a long as an unsigned integer to the stream as 32-bit regardless of revision.
     * @param value Unsigned integer to write
     */
    public void u32f(long value) { this.bytes(Bytes.toBytes(value)); }
    
    /**
     * Writes a long as an unsigned integer to the stream as 32-bit in little endian regardless of revision.
     * @param value Unsigned integer to write
     */
    public void u32LEf(long value) { this.bytes(Bytes.toBytesLE(value)); }

    /**
     * Writes a 7-bit encoded integer to the stream.
     * @param value Integer to write
     */
    public void varint(int value) { this.varint((long) value); }
    
    /**
     * Writes a 7-bit encoded long to the stream.
     * @param value Long to write
     */
    public void varint(long value) {
        if (value == -1 || value == Long.MAX_VALUE) {
            this.bytes(new byte[] {  (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x0F });
            return;
        } else if (value == 0) {
            this.i8((byte) 0);
            return;
        }
        while (value > 0) {
            byte b = (byte)(value & 0x7fL);
            value >>= 7L;
            if (value > 0L) b |= 128L;
            this.i8(b);
        }
    }
    
    /**
     * Writes a Matrix4x4 to the stream, encoded depending on the revision.
     * @param value Matrix to write
     */
    public void matrix(Matrix4f value) {
        float[] identity = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        float[] values = new float[16];
        value.get(values);
        int flags = 0xFFFF;
        if (this.isEncoded()) {
            flags = 0;
            for (int i = 0; i < 16; ++i)
                if (values[i] != identity[i])
                    flags |= (1 << i);
            this.i16((short) flags);
        }

        for (int i = 0; i < 16; ++i)
            if (((flags >>> i) & 1) != 0)
                this.f32(values[i]);
    }

    /**
     * Writes a resource reference to the stream with a short flag.
     * @param value Resource reference to write
     */
    public void resource(ResourceDescriptor value) { this.resource(value, false); }
    
    /**
     * Writes a resource reference to the stream with either a byte or short flag.
     * @param value Resource reference to write
     * @param useSingleByteFlag Whether a byte or a short flag should be written
     */
    public void resource(ResourceDescriptor value, boolean useSingleByteFlag) {
        byte HASH = 1, GUID = 2;
        if (this.revision <= 0x18B) {
            HASH = 2;
            GUID = 1;
        }
        
        if (this.revision < 0x230) useSingleByteFlag = true;
        if (((this.revision >= 0x230 && this.revision <= 0x26e) || (this.revision >= 0x273 && this.revision <= 0x297)) && !useSingleByteFlag) 
            this.u8(0);
        if (useSingleByteFlag) {
            if (value == null) this.u8(0);
            else if (value.hash != null) this.i8(HASH);
            else if (value.GUID != -1) this.i8(GUID);
            else this.u8(0);
        } else if (this.isEncoded()) {
            if (value == null) this.i16((short) 0);
            else if (value.hash != null) this.i16((short) HASH);
            else if (value.GUID != -1) this.i16((short) GUID);
            else this.i16((short) 0);
        } else {
            if (value == null) this.i32(0);
            else if (value.hash != null) this.i32(HASH);
            else if (value.GUID != -1) this.i32(GUID);
            else this.i32(0);
        }
        if (value == null) return;
        if (value.hash != null) this.bytes(value.hash);
        else if (value.GUID != -1) this.u32(value.GUID);

        if (!this.hasDependency(value))
            this.dependencies.add(value);
    }

    /**
     * Writes a 32 bit floating point number to the stream.
     * @param value Float to write 
     */
    public void f32(float value) { this.bytes(Bytes.toBytes(Float.floatToIntBits(value))); }
    
    /**
     * Writes a 32 bit floating point number to the stream in little endian.
     * @param value Float to write
     */
    public void f32LE(float value) {
        int bits = Float.floatToRawIntBits(value);
        byte[] byteBuffer = ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(bits)
                .array();
        this.bytes(byteBuffer);
    }

    /**
     * Writes an array of 32 bit floating point numbers to the stream.
     * @param values Floats to write
     */
    public void f32a(float[] values) {
        if (values == null) { this.i32(0); return; }
        this.i32(values.length);
        for (float value : values)
            this.f32(value);
    }

    /**
     * Writes a 2-dimensional floating point vector to the stream.
     * @param value Vector2f to write
     */
    public void v2(Vector2f value) { this.f32(value.x); this.f32(value.y); }
    
    /**
     * Writes a 3-dimensional floating point vector to the stream.
     * @param value Vector3f to write
     */
    public void v3(Vector3f value) {
        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
    }

    /**
     * Writes a 4-dimensional floating point vector to the stream.
     * @param value Vector4f to write
     */
    public void v4(Vector4f value) {
        this.f32(value.x);
        this.f32(value.y);
        this.f32(value.z);
        this.f32(value.w);
    }

    /**
     * Shrinks the size of the buffer to the current offset.
     */
    public void shrink() { this.buffer = Arrays.copyOfRange(this.buffer, 0, this.offset); }
}
