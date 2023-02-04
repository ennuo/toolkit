package cwlib.io.serializer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.ValueEnum;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.Thing;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.util.Bytes;

/**
 * Reversible serializer for assets, also handles
 * references, revision, and dependencies.
 */
public class Serializer {
    private final boolean isWriting;

    private final MemoryInputStream input;
    private final MemoryOutputStream output;

    private final Revision revision;
    private final byte compressionFlags;

    private HashMap<Integer, Object> referenceIDs = new HashMap<>();
    private HashMap<Object, Integer> referenceObjects = new HashMap<>();
    private HashSet<ResourceDescriptor> dependencies = new HashSet<>();

    private int nextReference = 1;

    /**
     * Constructs a deserializer with stream and revision.
     * @param stream Input stream to use for serializer
     * @param revision Revision of resource contained by stream
     */
    public Serializer(MemoryInputStream stream, Revision revision) {
        this.input = stream;
        this.output = null;
        this.revision = revision;
        this.compressionFlags = stream.getCompressionFlags();
        this.isWriting = false;
    }

    /**
     * Constructs a serializer with stream and revision.
     * @param stream Output stream to use for serializer
     * @param revision Revision of resource to be serialized
     */
    public Serializer(MemoryOutputStream stream, Revision revision) {
        this.output = stream;
        this.input = null;
        this.revision = revision;
        this.compressionFlags = stream.getCompressionFlags();
        this.isWriting = true;
    }

    /**
     * Constructs a new serializer.
     * @param size Size of output stream
     * @param revision Revision of resource to be serialized
     * @param compressionFlags Compression flags to use during serialization
     */
    public Serializer(int size, Revision revision, byte compressionFlags) {
        this.output = new MemoryOutputStream(size, compressionFlags);
        this.input = null;
        this.revision = revision;
        this.compressionFlags = compressionFlags;
        this.isWriting = true;
    }

    /**
     * Constructs a new deserializer.
     * @param data Buffer to use in deserializer
     * @param revision Revision of resource contained by buffer
     * @param compressionFlags Compression flags to use during deserialization
     */
    public Serializer(byte[] data, Revision revision, byte compressionFlags) {
        this.input = new MemoryInputStream(data, compressionFlags);
        this.output = null;
        this.revision = revision;
        this.compressionFlags = compressionFlags;
        this.isWriting = false;
    }

    /**
     * Pads a selected number of bytes in the stream.
     * @param size Number of bytes to pad
     */
    public final void pad(int size) {
        if (this.isWriting) this.output.pad(size);
        else this.input.bytes(size);
    }

    /**
     * (De)serailizes a byte array to/from the stream.
     * @param value Bytes to write
     * @return Bytes serialized
     */
    public final byte[] bytearray(byte[] value) {
        if (this.isWriting) {
            this.output.bytearray(value);
            return value;
        }
        return this.input.bytearray();
    }

    /**
     * (De)serializes bytes to/from the stream.
     * @param value Bytes to write
     * @param size Number of bytes to read
     * @return Bytes serialized
     */
    public final byte[] bytes(byte[] value, int size) {
        if (this.isWriting) {
            this.output.bytes(value);
            return value;
        }
        return this.input.bytes(size);
    }

    /**
     * (De)serializes a boolean to/from the stream.
     * @param value Boolean to write
     * @return Boolean (de)serialized
     */
    public final boolean bool(boolean value) {
        if (this.isWriting) {
            this.output.bool(value);
            return value;
        }
        return this.input.bool();
    }
    
    /**
     * (De)serializes a boolean array to/from the stream.
     * @param values Boolean array to write
     * @return Boolean array (de)serialized
     */
    public final boolean[] boolarray(boolean[] values) {
        if (this.isWriting) {
            this.output.boolarray(values);
            return values;
        }
        return this.input.boolarray();
    }


    /**
     * (De)serializes a padded boolean to/from the stream.
     * @param value Boolean to write
     * @return Boolean (de)serialized
     */
    public final boolean intbool(boolean value) {
        if (this.isWriting) {
            this.output.i32(value ? 1 : 0);
            return value;
        }
        return this.input.i32() != 0;
    }

    /**
     * (De)serializes a byte to/from the stream.
     * @param value Byte to write
     * @return Byte (de)serialized
     */
    public final byte i8(byte value) {
        if (this.isWriting) {
            this.output.i8(value);
            return value;
        }
        return this.input.i8();
    }

    /**
     * (De)serializes an integer as a byte to/from the stream.
     * @param value Byte to write
     * @return Byte (de)serialized
     */
    public final int u8(int value) {
        if (this.isWriting) {
            this.output.u8(value);
            return value;
        }
        return this.input.u8();
    }

    /**
     * (De)serializes a short to/from the stream.
     * @param value Short to write
     * @return Short (de)serialized
     */
    public final short i16(short value) {
        if (this.isWriting) {
            this.output.i16(value);
            return value;
        }
        return this.input.i16();
    }

    /**
     * (De)serializes an integer to/from the stream as a short.
     * @param value Short to write
     * @return Short (de)serialized
     */
    public final int u16(int value) {
        if (this.isWriting) {
            this.output.u16(value);
            return value;
        }
        return this.input.u16();
    }

    /**
     * (De)serializes a 24-bit unsigned integer to/from the stream.
     * @param value Integer to write
     * @return Integer (de)serialized
     */
    public final int u24(int value) {
        if (this.isWriting) {
            this.output.u24(value);
            return value;
        }
        return this.input.u24();
    }

    /**
     * (De)serializes a 32-bit integer to/from the stream, compressed depending on the flags.
     * @param value Integer to write
     * @param force32 Whether or not to always write 32 bits regardless of compression flags.
     * @return Integer (de)serialized
     */
    public final int i32(int value, boolean force32) {
        if (this.isWriting) {
            this.output.i32(value, force32);
            return value;
        }
        return this.input.i32(force32);
    }

    /**
     * (De)serializes a signed 32-bit integer to/from the stream, compressed depending on the flags.
     * This function modifies the value written to the stream to fit an unsigned value, prefer i32
     * for normal values that are technically unsigned, but read as signed integers.
     * @param value Integer to write
     * @return Integer (de)serialized
     */
    public final int s32(int value) {
        if (this.isWriting) {
            this.output.s32(value);
            return value;
        }
        return this.input.s32();
    }

    /**
     * (De)serializes an unsigned 32-bit integer to/from the stream as a long, compressed depending on the flags.
     * @param value Integer to write
     * @param force32 Whether or not to always write 32 bits regardless of compression flags.
     * @return Integer (de)serialized
     */
    public final long u32(long value, boolean force32) {
        if (this.isWriting) {
            this.output.u32(value, force32);
            return value;
        }
        return this.input.u32(force32);
    }


    /**
     * (De)serializes a long to/from the stream, compressed depending on the flags.
     * @param value Long to write
     * @param force64 Whether or not to always write 64 bits regardless of compression flags.
     * @return Long (de)serialized
     */
    public final long i64(long value, boolean force64) {
        if (this.isWriting) {
            this.output.i64(value, force64);
            return value;
        }
        return this.input.i64(force64);
    }

    /**
     * (De)serializes a 32-bit integer to/from the stream.
     * @param value Integer to write
     * @return Integer (de)serialized
     */
    public final int i32(int value) { return this.i32(value, false); }

    /**
     * (De)serializes an unsigned 32-bit integer to/from the stream.
     * @param value Integer to write
     * @return Integer (de)serialized
     */
    public final long u32(long value) { return this.u32(value, false); }

    /**
     * (De)serializes a long to/from the stream.
     * @param value Long to write
     * @return Long (de)serialized
     */
    public final long i64(long value) { return this.i64(value, false); }

    /**
     * (De)serializes a variable length quantity to/from the stream.
     * @param value Long to write
     * @return Long (de)serialized
     */
    public final long uleb128(long value) {
        if (this.isWriting) {
            this.output.uleb128(value);
            return value;
        }
        return this.input.uleb128();
    }

    /**
     * (De)serializes a 16-bit integer array to/from the stream.
     * @param values Short array to write
     * @return Short array (de)serialized
     */
    public final short[] shortarray(short[] values) {
        if (this.isWriting) {
            this.output.shortarray(values);
            return values;
        }
        return this.input.shortarray();
    }

    /**
     * (De)serializes a 32-bit integer array to/from the stream.
     * @param values Integer array to write
     * @return Integer array (de)serialized
     */
    public final int[] intarray(int[] values) {
        if (this.isWriting) {
            this.output.intarray(values);
            return values;
        }
        return this.input.intarray();
    }

    /**
     * (De)serializes a 64-bit integer array to/from the stream.
     * @param values Long array to write
     * @return Long array (de)serialized
     */
    public final long[] longarray(long[] values) {
        if (this.isWriting) {
            this.output.longarray(values);
            return values;
        }
        return this.input.longarray();
    }

    /**
     * (De)serializes a 16-bit floating point number to/from the stream.
     * @param value Float to write
     * @return Float (de)serialized
     */
    public final float f16(float value) {
        if (this.isWriting) {
            this.output.f16(value);
            return value;
        }
        return this.input.f16();
    }

    /**
     * (De)serializes a 32-bit floating point number to/from the stream.
     * @param value Float to write
     * @return Float (de)serialized
     */
    public final float f32(float value) {
        if (this.isWriting) {
            this.output.f32(value);
            return value;
        }
        return this.input.f32();
    }

    /**
     * (De)serializes a 32-bit floating point number array to/from the stream.
     * @param values Float array to write
     * @return Float array (de)serialized
     */
    public final float[] floatarray(float[] values) {
        if (this.isWriting) {
            this.output.floatarray(values);
            return values;
        }
        return this.input.floatarray();
    }

    /**
     * (De)serializes a 2-dimensional floating point vector to/from the stream.
     * @param value Vector2f to write
     * @return Vector2f (de)serialized
     */
    public final Vector2f v2(Vector2f value) {
        if (this.isWriting) {
            this.output.v2(value);
            return value;
        }
        return this.input.v2();
    }

    /**
     * (De)serializes a 3-dimensional floating point vector to/from the stream.
     * @param value Vector3f to write
     * @return Vector3f (de)serialized
     */
    public final Vector3f v3(Vector3f value) {
        if (this.isWriting) {
            this.output.v3(value);
            return value;
        }
        return this.input.v3();
    }

    /**
     * (De)serializes a 4-dimensional floating point vector to/from the stream.
     * @param value Vector4f to write
     * @return Vector4f (de)serialized
     */
    public final Vector4f v4(Vector4f value) {
        if (this.isWriting) {
            this.output.v4(value);
            return value;
        }
        return this.input.v4();
    }

    /**
     * (De)serializes an array of 4-dimensional floating point vectors to/from the stream.
     * @param values Vector array to write
     * @return Vector array (de)serialized
     */
    public final Vector4f[] vectorarray(Vector4f[] values) {
        if (this.isWriting) {
            this.output.vectorarray(values);
            return values;
        }
        return this.input.vectorarray();
    }

    /**
     * (De)serializes a Matrix4x4 to/from the stream.
     * @param value Matrix4x4 to write
     * @return Matrix4x4 (de)serialized
     */
    public final Matrix4f m44(Matrix4f value) {
        if (this.isWriting) {
            this.output.m44(value);
            return value;
        }
        return this.input.m44();
    }

    /**
     * (De)serializes a fixed length string to/from the stream.
     * @param value String to write
     * @param size Fixed length of string to write
     * @return String (de)serialized
     */
    public final String str(String value, int size) {
        if (this.isWriting) {
            this.output.str(value, size);
            return value;
        }
        return this.input.str(size);
    }
    /**
     * (De)serializes a string to/from the stream.
     * @param value String to write
     * @return String (de)serialized
     */
    public final String str(String value) {
        if (this.isWriting) {
            this.output.str(value);
            return value;
        }
        return this.input.str();
    }

    /**
     * (De)serializes a wide string to/from the stream.
     * @param value String to write
     * @return String (de)serialized
     */
    public final String wstr(String value) {
        if (this.isWriting) {
            this.output.wstr(value);
            return value;
        }
        return this.input.wstr();
    }

    /**
     * (De)serializes a SHA1 hash to/from the stream.
     * @param value SHA1 hash to write
     * @return SHA1 hash (de)serialized
     */
    public final SHA1 sha1(SHA1 value) {
        if (this.isWriting) {
            this.output.sha1(value);
            return value;
        }
        return this.input.sha1();
    }

    /**
     * (De)serializes a GUID to/from the stream.
     * @param value GUID to write
     * @param force32 Whether or not to force 32 bit, regardless of compression flags.
     * @return GUID (de)serialized
     */
    public final GUID guid(GUID value, boolean force32) {
        if (this.isWriting) {
            this.output.guid(value, force32);
            return value;
        }
        return this.input.guid(force32);
    }

    /**
     * (De)serializes a GUID to/from the stream.
     * @param value GUID to write
     * @return GUID (de)serialized
     */
    public final GUID guid(GUID value) { return this.guid(value, false); }

    /**
     * (De)serializes a Thing reference to/from the stream.
     * @param thing Thing to write
     * @return Thing (de)serialized
     */
    public final Thing thing(Thing thing) {
        return this.reference(thing, Thing.class);
    }

    /**
     * (De)serializes an array of Thing references to/from the stream
     * @param things Things to write
     * @return Things (de)serialized
     */
    public final Thing[] thingarray(Thing[] things) {
        return this.array(things, Thing.class, true);
    }

    /**
     * (De)serializes a resource to/from the stream.
     * @param value Resource to write
     * @param type Type of resource
     * @return Resource (de)serialized
     */
    public final ResourceDescriptor resource(ResourceDescriptor value, ResourceType type) {
        return this.resource(value, type, false, true, false);
    }

    /**
     * (De)serializes a resource to/from the stream.
     * @param value Resource to write
     * @param type Type of resource
     * @param isDescriptor Whether or not to skip resource flags
     * @return Resource (de)serialized
     */
    public final ResourceDescriptor resource(ResourceDescriptor value, ResourceType type, boolean isDescriptor) {
        return this.resource(value, type, isDescriptor, true, false);
    }

    /**
     * (De)serializes a resource to/from the stream.
     * @param value Resource to write
     * @param type Type of resource
     * @param isDescriptor Whether or not to skip resource flags
     * @param cp Flag toggle
     * @param t Serialize resource type
     * @return Resource (de)serialized
     */
    public final ResourceDescriptor resource(ResourceDescriptor value, ResourceType type, boolean isDescriptor, boolean cp, boolean t) {
        byte NONE = 0, HASH = 1, GUID = 2;
        // is it 0x191 or 0x18c
        if (this.revision.getVersion() < 0x191 && cp) {
            HASH = 2;
            GUID = 1;
        }

        if (!this.isWriting) {
            int flags = 0;
            if (this.revision.getVersion() > 0x22e && !isDescriptor)
                flags = this.input.i32();
            
            byte guidHashFlag = this.input.i8();
            ResourceDescriptor descriptor = null;
            
            if (guidHashFlag == NONE) return null;

            GUID guid = null;
            SHA1 sha1 = null;

            if ((guidHashFlag & GUID) != 0)
                guid = this.input.guid();
            if ((guidHashFlag & HASH) != 0)
                sha1 = this.input.sha1();

            if (t) type = ResourceType.fromType(this.input.i32());

            descriptor = new ResourceDescriptor(guid, sha1, type);
            if (!descriptor.isValid()) return null;
            descriptor.setFlags(flags);

            if (descriptor.isHash() || (!(isDescriptor && type == ResourceType.PLAN)))
                this.dependencies.add(descriptor);
            
            return descriptor;
        }

        if (this.revision.getVersion() > 0x22e && !isDescriptor)
            this.output.u32(value != null ? value.getFlags() : 0);

        if (value != null && value.isValid()) {
            byte flags = 0;
            
            if (value.isHash()) flags |= HASH;
            if (value.isGUID()) flags |= GUID;

            this.output.i8(flags);

            if ((flags & GUID) != 0)
                this.output.guid(value.getGUID());
            if ((flags & HASH) != 0)
                this.output.sha1(value.getSHA1());
            
            if (flags != 0 && !(isDescriptor && type == ResourceType.PLAN))
                this.dependencies.add(value);
        } else this.i8(NONE);

        if (t)
            this.output.i32(value != null ? value.getType().getValue() : 0);

        return value;
    }

    /**
     * (De)serializes a vector (uint32_t array) to/from the stream, compressed depending on the flags.
     * @param value Vector to (de)serialize
     * @return (De)serialized vector
     */
    public final long[] longvector(long[] value) {
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_VECTORS) == 0)
            return this.longarray(value);
        
        if (this.isWriting) {
            if (value == null || value.length == 0) {
                this.output.i32(0);
                return value;
            }

            long bytes = Arrays.stream(value).max().orElse(0);
            long min = Arrays.stream(value).min().orElse(0);
            if (bytes == 0) {
                this.output.i32(0);
                return value;
            }

            // signed sucks
            if (min < 0) bytes = 8;
            else {
                if (bytes < 0xFFFFFFFFFFFFFFl) {
                    if (bytes <= 0xFFFFFFFFFFFFl) {
                        if (bytes <= 0xFFFFFFFFFFl) {
                            if (bytes <= 0xFFFFFFFFl) {
                                if (bytes <= 0xFFFFFFl) {
                                    if (bytes <= 0xFFFFl) {
                                        if (bytes <= 0xFF) bytes = 1;
                                        else bytes = 2;
                                    } else bytes = 3;
                                } else bytes = 4;
                            } else bytes = 5;
                        } else bytes = 6;
                    } else bytes = 7;
                } else bytes = 8;
            }

            this.output.i32(value.length);
            this.output.u8((int) (bytes & 0xFF));
            for (int i = 0; i < bytes; ++i)
                for (int j = 0; j < value.length; ++j)
                    this.output.u8((int) (((value[j] >>> (i * 8)) & 0xFFl)));
                
            return value;
        }

        int count = this.input.i32();
        if (count == 0) return null;
        int bytes = this.input.u8();
        long[] vector = new long[count];
        for (int i = 0; i < bytes; ++i)
            for (int j = 0; j < count; ++j)
                vector[j] |= ((long) this.input.u8()) << (i * 8);
        
        return vector;
    }

    /**
     * (De)serializes a vector (uint32_t array) to/from the stream, compressed depending on the flags.
     * @param value Vector to (de)serialize
     * @return (De)serialized vector
     */
    public final int[] intvector(int[] value) {
        if ((this.compressionFlags & CompressionFlags.USE_COMPRESSED_VECTORS) == 0)
            return this.intarray(value);
        
        if (this.isWriting) {
            if (value == null || value.length == 0) {
                this.output.i32(0);
                return value;
            }

            long bytes = Arrays.stream(value).mapToLong(x -> x & 0xFFFFFFFFl).max().orElse(0);
            if (bytes == 0) {
                this.output.i32(value.length);
                this.output.i32(0);
                return value;
            }

            // Get number of bytes max number requires
            if (bytes < 0xFFFFFFl) {
                if (bytes <= 0xFFFFl) {
                    if (bytes <= 0xFF) bytes = 1;
                    else bytes = 2;
                } else bytes = 3;
            } else bytes = 4;


            this.output.i32(value.length);
            this.output.u8((int) (bytes & 0xFF));
            for (int i = 0; i < bytes; ++i)
                for (int j = 0; j < value.length; ++j)
                    this.output.u8((value[j] >>> (i * 8)) & 0xFF);
            
            return value;
        }

        int count = this.input.i32();
        if (count == 0) return null;
        int bytes = this.input.u8();
        int[] vector = new int[count];
        for (int i = 0; i < bytes; ++i)
            for (int j = 0; j < count; ++j)
                vector[j] |= (this.input.u8() << (i * 8));
        
        return vector;
    }

    /**
     * (De)serializes a 8-bit enum value to/from the stream.
     * @param <T> Enum class
     * @param value Enum value
     * @return (De)serialized enum value
     */
    @SuppressWarnings("unchecked")
    public final <T extends Enum<T> & ValueEnum<Byte>> T enum8(T value) {
        if (this.isWriting) {
            this.output.enum8(value);
            return value;
        }
        return this.input.enum8((Class<T>) value.getClass());
    }

    /**
     * (De)serializes a 32-bit enum value to/from the stream.
     * @param <T> Enum class
     * @param value Enum value
     * @return (De)serialized enum value
     */
    @SuppressWarnings("unchecked")
    public final <T extends Enum<T> & ValueEnum<Integer>> T enum32(T value) {
        if (this.isWriting) {
            this.output.enum32(value);
            return value;
        }
        return this.input.enum32((Class<T>) value.getClass());
    }

    /**
     * (De)serializes a 32-bit enum value to/from the stream.
     * @param <T> Enum class
     * @param value Enum value
     * @param signed Whether or not to (de)serialize s32
     * @return (De)serialized enum value
     */
    @SuppressWarnings("unchecked")
    public final <T extends Enum<T> & ValueEnum<Integer>> T enum32(T value, boolean signed) {
        if (this.isWriting) {
            this.output.enum32(value, signed);
            return value;
        }
        return this.input.enum32((Class<T>) value.getClass(), signed);
    }

    /**
     * (De)serializes a 8-bit enum value to/from the stream.
     * @param <T> Enum class
     * @param value Enum value
     * @param 
     * @return (De)serialized enum value
     */
    public final <T extends Enum<T> & ValueEnum<Byte>> T[] enumarray(T[] values, Class<T> enumeration) {
        if (this.isWriting) {
            this.output.enumarray(values);
            return values;
        }
        return this.input.enumarray(enumeration);
    }

    /**
     * (De)serializes a structure to/from the stream as a reference.
     * @param <T> Generic serializable structure
     * @param value Structure to serialize
     * @param clazz Serializable class type
     * @return (De)serialized structure
     */
    @SuppressWarnings("unchecked")
    public final <T extends Serializable> T reference(T value, Class<T> clazz) {
        if (this.isWriting) {
            if (value == null) {
                this.output.i32(0);
                return value;
            }
            int reference = this.referenceObjects.getOrDefault(value, -1);
            if (reference == -1) {
                int next = this.nextReference++;
                this.output.i32(next);
                this.referenceIDs.put(next, value);
                this.referenceObjects.put(value, next);
                Serializable.serialize(this, value, clazz);
                return value;
            } else this.output.i32(reference);
            return value;
        }
        int reference = this.input.i32();
        if (reference == 0) return null;
        if (this.referenceIDs.containsKey(reference))
            return (T) this.referenceIDs.get(reference);
        T struct = null;
        try { struct = clazz.getDeclaredConstructor().newInstance(); } 
        catch (Exception ex) {
            throw new SerializationException("Failed to create class instance in serializer!");
        }
        this.referenceIDs.put(reference, struct);
        this.referenceObjects.put(struct, reference);

        struct = Serializable.serialize(this, struct, clazz);

        return struct;
    }

    /**
     * (De)serializes a structure to/from the stream.
     * @param <T> Generic serializable structure
     * @param value Structure to serialize
     * @param clazz Serializable class type
     * @return (De)serialized structure
     */
    public final <T extends Serializable> T struct(T value, Class<T> clazz) {
        if (this.isWriting) {
            Serializable.serialize(this, value, clazz);
            return value;
        }
        return clazz.cast(Serializable.serialize(this, null, clazz));
    }

    /**
     * (De)serializes an array to/from the stream.
     * @param <T> Generic serializable structure
     * @param values Array to serialize
     * @param clazz Array base serializable type
     * @return (De)serialized array
     */
    public final <T extends Serializable> T[] array(T[] values, Class<T> clazz) {
        return this.array(values, clazz, false);
    }

    /**
     * (De)serializes an array to/from the stream.
     * @param <T> Generic serializable structure
     * @param values Array to serialize
     * @param clazz Array base serializable type
     * @return (De)serialized array
     */
    public final <T extends Serializable> ArrayList<T> arraylist(ArrayList<T> values, Class<T> clazz) {
        return this.arraylist(values, clazz, false);
    }

    /**
     * (De)serializes an arraylist to/from the stream.
     * @param <T> Generic serializable structure
     * @param values Array to serialize
     * @param clazz Array base serializable type
     * @param isReference Whether the array base structure is a reference type
     * @return (De)serialized array
     */
    public final <T extends Serializable> ArrayList<T> arraylist(ArrayList<T> values, Class<T> clazz, boolean isReference) {
        if (this.isWriting) {
            if (values == null) {
                this.output.i32(0);
                return values;
            }
            this.output.i32(values.size());
            for (T serializable : values) {
                if (isReference) this.reference(serializable, clazz);
                else Serializable.serialize(this, serializable, clazz);
            }
            return values;
        }
        int count = this.input.i32();
        ArrayList<T> output = new ArrayList<T>(count);
        for (int i = 0; i < count; ++i) {
            if (isReference)
                output.add(clazz.cast(this.reference(null, clazz)));
            else
                output.add(clazz.cast(Serializable.serialize(this, null, clazz)));
        }
        return output;
    }
    
    /**
     * (De)serializes an array to/from the stream.
     * @param <T> Generic serializable structure
     * @param values Array to serialize
     * @param clazz Array base serializable type
     * @param isReference Whether the array base structure is a reference type
     * @return (De)serialized array
     */
    @SuppressWarnings("unchecked")
    public final <T extends Serializable> T[] array(T[] values, Class<T> clazz, boolean isReference) {
        if (this.isWriting) {
            if (values == null) {
                this.output.i32(0);
                return values;
            }
            this.output.i32(values.length);
            for (T serializable : values) {
                if (isReference) this.reference(serializable, clazz);
                else Serializable.serialize(this, serializable, clazz);
            }
            return values;
        }
        int count = this.input.i32();
        T[] output = (T[]) Array.newInstance(clazz, count);
        try {
            for (int i = 0; i < count; ++i) {
                if (isReference)
                    output[i] = clazz.cast(this.reference(null, clazz));
                else
                    output[i] = clazz.cast(Serializable.serialize(this, null, clazz));
            }
        } catch (Exception ex) {
            throw new SerializationException("There was an error (de)serializing an array!");
        }
        return output;
    }

    /**
     * Shrinks the buffer to current offset and returns the buffer.
     * @return The shrinked buffer
     */
    public final byte[] getBuffer() {
        if (!this.isWriting) return null;
        return this.output.shrink().getBuffer();
    }

    public final MemoryInputStream getInput() { return this.input; }
    public final MemoryOutputStream getOutput() { return this.output; }
    public final int getOffset() {
        if (this.isWriting) return this.output.getOffset();
        return this.input.getOffset();
    }
    public final int getLength() {
        if (this.isWriting) return this.output.getLength();
        return this.input.getLength();
    }

    public void log(String message) {
        this.log(message, 0);
    }

    public void log(String message, int level) {
        if (ResourceSystem.DISABLE_LOGS) return;
        if (level < ResourceSystem.LOG_LEVEL) return;
        if (this.isWriting) {
            System.out.println("[WRITING] @ 0x" + Bytes.toHex(Bytes.toBytesBE(this.getOffset())) + " -> " + message);
        } else
        System.out.println("[READING] @ 0x" + Bytes.toHex(Bytes.toBytesBE(this.getOffset())) + " -> " + message);

    }

    /**
     * Forcibly adds a dependency to this serializer's collection,
     * used for RPlan's because we can't serialize thing data yet.
     * @param dependency Dependency to add
     */
    public final void addDependency(ResourceDescriptor dependency) {
        this.dependencies.add(dependency);
    }

    /**
     * Remove all dependencies in collection.
     */
    public final void clearDependencies() {
        this.dependencies.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getPointer(int index) { return (T) this.referenceIDs.get(index); }

    public void setPointer(int index, Object value) { 
        this.referenceIDs.put(index, value);
        this.referenceObjects.put(value, index);
    }

    public int getNextReference() { return this.nextReference++; }
    
    public Thing[] getThings() {
        ArrayList<Thing> things = new ArrayList<>();
        for (Object reference : this.referenceObjects.keySet()) {
            if (reference instanceof Thing)
                things.add((Thing) reference);
        }
        return things.toArray(Thing[]::new);
    }
    
    public final boolean isWriting() { return this.isWriting; }
    public final Revision getRevision() { return this.revision; }
    public final byte getCompressionFlags() { return this.compressionFlags; }
    public final ResourceDescriptor[] getDependencies() {
        ResourceDescriptor[] descriptors = 
            new ResourceDescriptor[this.dependencies.size()];
        descriptors = this.dependencies.toArray(descriptors);
        return descriptors;
    }
}
