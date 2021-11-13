package ennuo.craftworld.serializer;

import ennuo.craftworld.resources.enums.CompressionFlags;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.structs.Revision;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Serializer {
    public boolean isWriting = false;
    
    public Data input;
    public Output output;
    
    public Revision revision;
    public int compressionFlags;
    
    private HashMap<Integer, Object> referenceIDs = new HashMap<>();
    private HashMap<Object, Integer> referenceObjects = new HashMap<>();
    
    private int nextReference = 1;
    
    public Serializer(Data data) {
        this.input = data;
        this.revision = data.revision;
        this.compressionFlags = data.compressionFlags;
        this.isWriting = false;
    }
    
    public Serializer(Output data) {
        this.output = data;
        this.revision = data.revision;
        this.compressionFlags = data.compressionFlags;
        this.isWriting = true;
    }
    
    public Serializer(int size, Revision revision, byte compressionFlags) {
        this.isWriting = true;
        this.output = new Output(size, revision);
        this.revision = revision;
        this.output.compressionFlags = compressionFlags;
        this.compressionFlags = compressionFlags;
    }
    
    public Serializer(byte[] data, Revision revision, byte compressionFlags) {
        this.isWriting = false;
        this.input = new Data(data, revision);
        this.input.compressionFlags = compressionFlags;
        this.revision = revision;
        this.compressionFlags = compressionFlags;
    }
    
    public void pad(int size) {
        if (this.isWriting) this.output.pad(size);
        else this.input.bytes(size);
    }
    
    public byte[] bytes(byte[] value, int size) {
        if (this.isWriting) {
            this.output.bytes(value);
            return value;
        }
        return this.input.bytes(size);
    }
    
    public boolean bool(boolean value) {
        if (this.isWriting) {
            this.output.bool(value);
            return value;
        }
        return this.input.bool();
    }
    
    public byte i8(byte value) {
        if (this.isWriting) {
            this.output.i8(value);
            return value;
        }
        return this.input.i8();
    }
    
    public byte[] i8a(byte[] values) {
        if (this.isWriting) {
            this.output.i8a(values);
            return values;
        }
        return this.input.i8a();
    }
    
    public short i16(short value) {
        if (this.isWriting) {
            this.output.i16(value);
            return value;
        }
        return this.input.i16();
    }
    
    public short[] i16a(short[] values) {
        if (this.isWriting) {
            this.output.i16a(values);
            return values;
        }
        return this.input.i16a();
    }
    
    public int i32(int value) {
        if (this.isWriting) {
            this.output.i32(value);
            return value;
        }
        return this.input.i32();
    }
    
    public long i64(long value) {
        if (this.isWriting) {
            this.output.i64(value);
            return value;
        }
        return this.input.i64();
    }
    
    public int[] i32a(int[] values) {
        if (this.isWriting) {
            this.output.i32a(values);
            return values;
        }
        return this.input.i32a();
    }
    
    public long u32d(long value) {
        // NOTE(Abz): I'm not actually sure what this type is
        // but for some reason in the "encoded" revisions, it's
        // doubled for some reason? Well, whatever
        
        int multiplier = 
                ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_INTEGERS) != 0) ? 2 : 1;
        
        if (this.isWriting) {
            this.output.u32(value * multiplier);
            return value;
        }
        return this.input.u32() / multiplier;
    }
    
    public long u32(long value) {
        if (this.isWriting) {
            this.output.u32(value);
            return value;
        }
        return this.input.u32();
    }
    
    public long[] u32a(long[] values) {
        if (this.isWriting) {
            this.output.u32a(values);
            return values;
        }
        return this.input.u32a();
    }
    
    public int i32f(int value) {
        if (this.isWriting) {
            this.output.i32f(value);
            return value;
        }
        return this.input.i32f();
    }
    
    public long u32f(long value) {
        if (this.isWriting) {
            this.output.u32f(value);
            return value;
        }
        return this.input.u32f();
    }
    
    public float f32(float value) {
        if (this.isWriting) {
            this.output.f32(value);
            return value;
        }
        return this.input.f32();
    }
    
    public float[] f32a(float[] values) {
        if (this.isWriting) {
            this.output.f32a(values);
            return values;
        }
        return this.input.f32a();
    }
    
    public String str(String value, int size) {
        if (this.isWriting) {
            this.output.str(value, size);
            return value;
        }
        return this.input.str(size);
    }
    
    public String str8(String value) {
        if (this.isWriting) {
            this.output.str8(value);
            return value;
        }
        return this.input.str8();
    }
    
    public String str16(String value) {
        if (this.isWriting) {
            this.output.str16(value);
            return value;
        }
        return this.input.str16();
    }
    
    public Vector2f v2(Vector2f value) {
        if (this.isWriting) {
            this.output.v2(value);
            return value;
        }
        return this.input.v2();
    }
    
    public Vector3f v3(Vector3f value) {
        if (this.isWriting) {
            this.output.v3(value);
            return value;
        }
        return this.input.v3();
    }
    
    public Vector4f v4(Vector4f value) {
        if (this.isWriting) {
            this.output.v4(value);
            return value;
        }
        return this.input.v4();
    }
    
    public Matrix4f matrix(Matrix4f value) {
        if (this.isWriting) {
            this.output.matrix(value);
            return value;
        }
        return this.input.matrix();
    }
    
    public int[] table(int[] values) {
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_VECTORS) == 0) 
            return this.i32a(values);
        if (this.isWriting) {
            if (values == null || values.length == 0) {
                this.output.i32(0);
                return values;
            }
            
            boolean overflow = 
                    Arrays.stream(values).anyMatch(x -> x > 0xFF);
            
            this.output.i32(values.length);
            if (values.length == 1 && values[0] == 0) {
                this.output.i32(0);
                return values;
            }
            
            if (overflow) this.output.i32(2);
            else this.output.i32(1);
            
            int[] indices = new int[values.length];
            int[] overflowIndices = new int[values.length];
            int loop = 0;
            
            for (int i = 0; i < values.length; ++i) {
                int value = values[i];
                if (value - (loop * 0x100) >= 0x100) loop++;
                indices[i] = value - (loop * 0x100);
                if (overflow)
                    overflowIndices[i] = loop;
            }
            
            for (int index : indices)
                this.output.i8((byte) index);
            if (overflow)
                for (int index : overflowIndices)
                    this.output.i8((byte) index);
            
            return values;
        }
        
        int indexCount = this.input.i32();
        if (indexCount == 0) return null;
        int tableCount = this.input.i32();
        if (tableCount == 0) return null;
        
        int[] indices = new int[indexCount];
        for (int i = 0; i < indexCount; ++i)
            indices[i] = this.input.i8() & 0xFF;
        
        for (int i = 1; i < tableCount; ++i)
            for (int j = 0; j < indexCount; ++j)
                indices[j] += (this.input.i8() & 0xFF) * 0x100;
        
        return indices;
    }
    
    public ResourceDescriptor resource(ResourceDescriptor value, ResourceType type) {
        if (this.isWriting) {
            this.output.resource(value);
            return value;
        }
        return this.input.resource(type);
    }
    
    public ResourceDescriptor resource(ResourceDescriptor value, ResourceType type, boolean useSingleByteFlag) {
        if (this.isWriting) {
            this.output.resource(value, useSingleByteFlag);
            return value;
        }
        return this.input.resource(type, useSingleByteFlag);
    }
    
    public <T extends Serializable> T reference(T value, Class<T> clazz) {
        if (this.isWriting) {
            if (value == null) {
                this.output.i32(0);
                return value;
            }
            int reference = this.referenceObjects.getOrDefault(value, -1);
            if (reference == -1) {
                int next = this.nextReference++;
                this.output.i32(next);
                T.serialize(this, value, clazz);
                this.referenceIDs.put(next, value);
                this.referenceObjects.put(value, next);
                return value;
            } else this.output.i32(reference);
            return value;
        }
        int reference = this.input.i32();
        if (reference == 0) return null;
        if (this.referenceIDs.containsKey(reference))
            return (T) this.referenceIDs.get(reference);
        T struct = T.serialize(this, null, clazz);
        this.referenceIDs.put(reference, struct);
        this.referenceObjects.put(struct, reference);
        return struct;
    }
    
    public <T extends Serializable> T struct(T value, Class<T> clazz) {
        if (this.isWriting) {
            T.serialize(this, value, clazz);
            return value;
        }
        return clazz.cast(T.serialize(this, null, clazz));
    }
    
    public <T extends Serializable> T[] array(T[] values, Class<T> clazz) {
        return this.array(values, clazz, false);
    }
    
    public <T extends Serializable> T[] array(T[] values, Class<T> clazz, boolean isReference) {
        if (this.isWriting) {
            if (values == null) {
                this.output.i32(0);
                return values;
            }
            this.output.i32(values.length);
            for (T serializable : values) {
                if (isReference) this.reference(serializable, clazz);
                else T.serialize(this, serializable, clazz);
            }
            return values;
        }
        int count = this.input.i32();
        T[] output = (T[]) Array.newInstance(clazz, count);
        for (int i = 0; i < count; ++i) {
            if (isReference)
                output[i] = clazz.cast(this.reference(null, clazz));
            else
                output[i] = clazz.cast(T.serialize(this, null, clazz));
        }
        return output;
    }
    
    public byte[] getBuffer() {
        if (!this.isWriting) return null;
        this.output.shrink();
        return this.output.buffer;
    }
    
    public void write(String path) {
        if (!this.isWriting) return;
        FileIO.write(this.getBuffer(), path);
    }
}
