package ennuo.craftworld.memory;

import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.enums.RType;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Data {
    public static int ENCODED_REVISION = 0x271;

    public String path;

    public byte[] data;
    public int offset;
    public int length;
    
    public int revision = 0x271;
    
    public static int[] toPrimitive(Integer[] input) {
        return Arrays.stream(input).mapToInt(Integer::intValue).toArray();
    }
    
    public static short[] toPrimitive(Short[] input) {
        short[] output = new short[input.length];
        for (int i = 0; i < output.length; ++i)
            output[i] = input[i].shortValue();
        return output;
    }
    
    public static float[] toPrimitive(Float[] input) {
        float[] output = new float[input.length];
        for (int i = 0; i < output.length; ++i)
            output[i] = input[i].floatValue();
        return output;
    }
    
    public boolean isEncoded() {
        return this.revision > ENCODED_REVISION && !(this.revision >= 0x273 && this.revision <= 0x297);
    }

    public Data(byte[] data) {
        setData(data);
    }
    public Data(byte[] data, int revision) {
        setData(data);
        this.revision = revision;
    }

    public Data(String path) {
        this.path = path;
        byte[] data = FileIO.read(path);
        if (data != null)
            setData(data);
        else setData(null);
    }

    public Data(String path, int revision) {
        this.path = path;
        this.revision = revision;
        byte[] data = FileIO.read(path);
        if (data != null)
            setData(data);
        else setData(null);
    }

    public void setData(byte[] buffer) {
        this.data = buffer;
        if (this.data != null)
            this.length = buffer.length;
        this.offset = 0;
    }

    public byte[] bytes(int size) {
        this.offset += size;
        if (this.offset == (data.length + 1) || size == 0) return new byte[] {};
        return Arrays.copyOfRange(this.data, this.offset - size, this.offset);
    }

    public void flip32() {
        byte[] bytes = this.bytes(4);
        data[offset - 4] = bytes[3];
        data[offset - 3] = bytes[2];
        data[offset - 2] = bytes[1];
        data[offset - 1] = bytes[0];
    }

    public boolean bool() {
        return int8() != 0;
    }

    public byte int8() {
        this.offset++;
        return this.data[this.offset - 1];
    }
    
    public <T> T[] array(Supplier<T> func) {
        int size = this.int32();
        if (size < 0) return null;
        T index = func.get();
        T[] elements = (T[]) Array.newInstance(index.getClass(), size);
        elements[0] = index;
        for (int i = 1; i < size; ++i)
            elements[i] = (T) func.get();
        return elements;
    }

    public int peek() {
        int offset = this.offset;
        int value = int32();
        seek(offset);
        return value;
    }
    
    public float f16() {
        short half = int16();
        return Float.intBitsToFloat((( half & 0x8000 )<<16 ) | ((( half & 0x7c00 ) + 0x1C000 )<<13 ) | (( half & 0x03FF )<<13 ));
    }

    public short int16() {
        byte[] buffer = bytes(2);
        return (short)((buffer[0] << 8) | buffer[1] & 0xFF);
    }

    public int int16LE() {
        byte[] buffer = bytes(2);
        return buffer[0] << 8 & 0xFF00 | buffer[1] & 0xFF;
    }
    
    public int int24() {
        byte[] buffer = bytes(3);
        return (buffer[0] & 0xFF) << 16 | (buffer[1] & 0xFF) << 8 | buffer[2] & 0xFF;
    }

    public int int32() {
        if (this.isEncoded()) return (int) varint() & 0xFFFFFFFF;
        return int32f();
    }
    
    public long[] u32a() {
        int count = int32();
        long[] output = new long[count];
        for (int i = 0; i < count; ++i)
            output[i] = uint32();
        return output;
    }

    public long uint32() {
        if (this.isEncoded()) return varint();
        return uint32f();
    }

    public int int32f() {
        byte[] buffer = bytes(4);
        return buffer[0] << 24 | (buffer[1] & 0xFF) << 16 | (buffer[2] & 0xFF) << 8 | buffer[3] & 0xFF;
    }

    public long uint32f() {
        byte[] bytes = bytes(4);
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt() & 0xFFFFFFFFL;
    }

    public float float32() {
        return Float.intBitsToFloat(int32f());
    }

    public float[] float32arr() {
        float[] floats = new float[int32()];
        for (int i = 0; i < floats.length; ++i)
            floats[i] = float32();
        return floats;
    }

    public Vector2f v2() {
        return new Vector2f(float32(), float32());
    }

    public Vector3f v3() {
        return new Vector3f(float32(), float32(), float32());
    }

    public Vector4f v4() {
        return new Vector4f(float32(), float32(), float32(), float32());
    }

    public float[] matrix() {
        float[] matrix = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        int flags = 0xFFFF;
        if (isEncoded()) flags = int16();
        for (int i = 0; i < 16; ++i)
            if (((flags >>> i) & 1) != 0)
                matrix[i] = float32();
        return matrix;
    }

    public ResourcePtr resource(RType type) {
        return resource(type, false);
    }
    public ResourcePtr resource(RType rType, boolean bit) {
        byte HASH = 1, GUID = 2;
        if (revision <= 0x18B) {
            HASH = 2;
            GUID = 1;
        }

        byte type;

        if (revision < 0x230) bit = true;
        if (((revision >= 0x230 && revision <= 0x26e) || (this.revision >= 0x273 && this.revision <= 0x297)) && !bit) this.int8();

        if (bit) type = int8();
        else if (isEncoded()) type = (byte) int16();
        else type = (byte) int32();

        ResourcePtr resource = new ResourcePtr();
        resource.type = rType;

        if (type == GUID) resource.GUID = uint32();
        else if (type == HASH) resource.hash = bytes(0x14);
        else return null;

        return resource;
    }

    public long varint() {
        long result = 0, i = 0;
        while (this.offset + i < this.length) {
            long b = (long) int8();
            result |= (b & 0x7FL) << 7L * i;
            if ((b & 0x80L) == 0L)
                break;
            i++;
        }
        return result;
    }

    public String str(int size) {
        if (size == 0) return "";
        return new String(bytes(size)).replace("\0", "");
    }

    public String str16() {
        int size = int32();
        if (!isEncoded()) size *= 2;
        byte[] data = bytes(size);
        return new String(data, Charset.forName("UTF-16BE"));
    }

    public String str8() {
        int size = int32();
        if (isEncoded()) size /= 2;
        return str(size);
    }

    public void overwrite(byte[] data) {
        for (int i = 0; i < data.length; i++)
            this.data[this.offset + i] = data[i];
    }

    public void seek(int pos) {
        this.offset = pos;
    }

    public void forward(int pos) {
        this.offset += pos;
    }

    public void rewind(int pos) {
        this.offset -= pos;
    }
}
