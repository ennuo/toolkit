package ennuo.craftworld.serializer.v2;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.io.FileIO;
import java.lang.reflect.Array;
import java.util.HashMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Serializer {
    public boolean isWriting = false;
    public Data input;
    public Output output;
    public int revision;
    private HashMap<Integer, Object> referenceIDs = new HashMap<>();
    private HashMap<Object, Integer> referenceObjects = new HashMap<>();
    private int nextReference = 1;
    
    public Serializer(Data data) {
        this.input = data;
        this.revision = data.revision;
        this.isWriting = false;
    }
    
    public Serializer(int size, int revision) {
        this.isWriting = true;
        this.output = new Output(size, revision);
        this.revision = revision;
    }
    
    public Serializer(byte[] data, int revision) {
        this.isWriting = false;
        this.input = new Data(data, revision);
        this.revision = revision;
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
    
    public long u32d(long value) {
        // NOTE(Abz): I'm not actually sure what this type is
        // but for some reason in the "encoded" revisions, it's
        // doubled for some reason? Well, whatever
        
        if (this.isWriting) {
            this.output.u32(value * ((this.revision > 0x271) ? 2 : 1));
            return value;
        }
        return this.input.u32() / ((this.revision > 0x271) ? 2 : 1);
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
    
    public ResourcePtr resource(ResourcePtr value, RType type) {
        if (this.isWriting) {
            this.output.resource(value);
            return value;
        }
        return this.input.resource(type);
    }
    
    public ResourcePtr resource(ResourcePtr value, RType type, boolean useSingleByteFlag) {
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
