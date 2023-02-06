package cwlib.types;

import cwlib.enums.Branch;
import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.streams.MemoryInputStream;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.structs.staticmesh.StaticPrimitive;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.resources.RPlan;
import cwlib.structs.staticmesh.StaticMeshInfo;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.util.Bytes;
import cwlib.util.Compressor;
import cwlib.util.Crypto;

import java.util.HashSet;

public class Resource {
    /**
     * Type of resource.
     */
    private ResourceType type = ResourceType.INVALID;

    /**
     * Method of serialization, binary, text, or texture resources.
     */
    private SerializationType method = SerializationType.UNKNOWN;

    /**
     * Texture metadata for texture resources.
     */
    private CellGcmTexture textureInfo;

    /**
     * Static mesh metadata
     */
    private StaticMeshInfo meshInfo;

    /**
     * Revision of the resource.
     */
    private Revision revision;

    /**
     * Whether or not the resource is compressed
     */
    private boolean isCompressed = true;

    /**
     * Controls which data types get compressed during serialization.
     */
    private byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;

    /**
     * Decompressed data from this resource.
     */
    private byte[] data = null;

    /**
     * Resources this resource depends on.
     */
    private HashSet<ResourceDescriptor> dependencies = new HashSet<>();

    /**
     * Constructs a new resource from path
     * @param path Path to read resource from
     */
    public Resource(String path) { this.process(new MemoryInputStream(path)); }

    /**
     * Constructs a new resource from buffer
     * @param data Data to read resource from
     */
    public Resource(byte[] data) { this.process(new MemoryInputStream(data)); }

    /**
     * Processes the resource stream.
     * @param stream Memory input stream to read from
     */
    private void process(MemoryInputStream stream) {
        if (stream == null || stream.getLength() < 0xb) return;
        this.type = ResourceType.fromMagic(stream.str(3));
        if (this.type == ResourceType.INVALID)
            throw new SerializationException("Invalid Resource type!");
        this.method = SerializationType.fromValue(stream.str(1));
        switch (this.method) {
            case BINARY:
            case ENCRYPTED_BINARY:
                int head = stream.i32();
                short branchID = 0, branchRevision = 0;
                int dependencyTableOffset = -1;
                if (head >= 0x109) {
                    dependencyTableOffset = this.processDependencies(stream);
                    if (head >= 0x189) {
                        if (this.type != ResourceType.STATIC_MESH) {
                            if (head >= 0x271) { 
                                branchID = stream.i16();
                                branchRevision = stream.i16();
                            }
                            if (head >= 0x297 || (head == Branch.LEERDAMMER.getHead() && branchID == Branch.LEERDAMMER.getID()) && branchRevision >= Revisions.LD_RESOURCES)
                                this.compressionFlags = stream.i8();
                            this.isCompressed = stream.bool();
                        } else this.meshInfo = new Serializer(stream, new Revision(head)).struct(null, StaticMeshInfo.class);
                    }
                }

                if (this.method.equals(SerializationType.ENCRYPTED_BINARY)) {
                    int size = stream.i32(), padding = 0;
                    if (size % 4 != 0)
                        padding = 4 - (size % 4);
                    stream = new MemoryInputStream(Crypto.XXTEA(stream.bytes(size + padding), true));
                    stream.seek(padding);
                }

                this.revision = new Revision(head, branchID, branchRevision);

                if (this.isCompressed) 
                    this.data = Compressor.decompressData(stream, dependencyTableOffset);
                else if (dependencyTableOffset != -1) 
                    this.data = stream.bytes(dependencyTableOffset - stream.getOffset());
                else 
                    this.data = stream.bytes(stream.getLength() - stream.getOffset());
                break;
            case TEXT:
                this.data = stream.bytes(stream.getLength() - stream.getOffset());
                break;
            case COMPRESSED_TEXTURE:
            case GTF_SWIZZLED:
            case GXT_SWIZZLED:
                if (this.type != ResourceType.TEXTURE)
                    this.textureInfo = new CellGcmTexture(stream, this.method);
                this.data = Compressor.decompressData(stream, stream.getLength());
                break;
            case UNKNOWN:
                throw new SerializationException("Invalid serialization method!");
        }
    }

    /**
     * Constructs a new serializer from this resource's data.
     * @return Data deserializer from current resource data
     */
    public Serializer getSerializer() {
        Serializer serializer = new Serializer(this.data, this.revision, this.compressionFlags);
        for (ResourceDescriptor descriptor : this.dependencies)
            serializer.addDependency(descriptor);
        return serializer;
    }

    /**
     * Constructs a new memory input stream from this resource's data.
     * @return Data stream from current resource data
     */
    public MemoryInputStream getStream() {
        return new MemoryInputStream(this.data, this.compressionFlags);
    }

    /**
     * Deserializes a resource from this instance.
     * @param <T> Resource type that implements Serializable
     * @param clazz Resource class reference that implements Serializable
     * @return Deserialized resource
     */
    public <T extends Serializable> T loadResource(Class<T> clazz) {
        Serializer serializer = this.getSerializer();
        return serializer.struct(null, clazz);
    }

    /**
     * Reads the dependency table from current resource stream.
     * @param stream Memory input stream to read from
     * @return The offset of the dependency table
     */
    private int processDependencies(MemoryInputStream stream) {
        int dependencyTableOffset = stream.i32();
        int originalOffset = stream.getOffset();
        stream.seek(dependencyTableOffset, SeekMode.Begin);
        
        int count = stream.i32();
        this.dependencies = new HashSet<>(count);
        for (int i = 0; i < count; ++i) {
            ResourceDescriptor descriptor = null;
            byte flags = stream.i8();
            
            GUID guid = null;
            SHA1 sha1 = null;

            if ((flags & 2) != 0)
                guid = stream.guid();
            if ((flags & 1) != 0)
                sha1 = stream.sha1();
            
            descriptor = new ResourceDescriptor(guid, sha1, ResourceType.fromType(stream.i32()));
            if (descriptor.isValid())
                this.dependencies.add(descriptor);
        }
        
        stream.seek(originalOffset, SeekMode.Begin);

        return dependencyTableOffset;
    }

    /**
     * Wraps a resource in a container, preferring compression if possible.
     * @param resource Instance of compressible resource
     * @param revision Revision of underlying resource stream.
     * @param compressionFlags Compression flags used during resource serialization
     * @param preferCompressed Whether or not this resource should be compressed, if possible
     * @return Resource container
     */
    public static byte[] compress(Compressable resource, Revision revision, byte compressionFlags, boolean preferCompressed) {
        return Resource.compress(resource.build(revision, compressionFlags), preferCompressed);
    }

    /**
     * Wraps a resource in a container, preferring compression if possible.
     * @param resource Instance of compressible resource
     * @param revision Revision of underlying resource stream.
     * @param compressionFlags Compression flags used during resource serialization
     * @return Resource container
     */
    public static byte[] compress(Compressable resource, Revision revision, byte compressionFlags) {
        return Resource.compress(resource.build(revision, compressionFlags), true);
    }

    /**
     * Wraps a resource in a container, preferring compression if possible.
     * @param data Serialization data to wrap
     * @return Resource container
     */
    public static byte[] compress(SerializationData data) {
        return Resource.compress(data, true);
    }

    public byte[] compress(byte[] data) {
        return Resource.compress(
            new SerializationData(
                data,
                this.revision,
                this.compressionFlags,
                this.type,
                this.method,
                this.getDependencies()
            ),
            true
        );
    }

    public byte[] compress() {
        return Resource.compress(
            new SerializationData(
                this.data,
                this.revision,
                this.compressionFlags,
                this.type,
                this.method,
                this.getDependencies()
            ),
            true
        );
    }

    /**
     * Wraps a resource in a container, with optional compression.
     * @param data Serialization data to wrap
     * @param preferCompressed Whether or not this resource should be compressed, if possible
     * @return Resource container
     */
    public static byte[] compress(SerializationData data, boolean preferCompressed) {
        ResourceType type = data.getType();
        StaticMeshInfo meshInfo = data.getStaticMeshInfo();
        boolean isStaticMesh = type == ResourceType.STATIC_MESH;

        byte[] buffer = data.getBuffer();
        ResourceDescriptor[] dependencies = data.getDependencies();

        int size = buffer.length + 0x50;
        if (dependencies != null)
            size += dependencies.length * 0x1c;
        if (data.getTextureInfo() != null) size += 0x24;
        else if (meshInfo != null) size += meshInfo.getAllocatedSize();

        MemoryOutputStream stream = new MemoryOutputStream(size);

        if (data.getMethod().equals(SerializationType.TEXT)) {
            stream.str(type.getHeader() + data.getMethod().getValue() + '\n', 5);
            stream.bytes(buffer);
            stream.shrink();
            return stream.getBuffer();
        }

        if (type.equals(ResourceType.TEXTURE) || type.equals(ResourceType.GTF_TEXTURE)) {
            stream.str(type.getHeader() + data.getMethod().getValue(), 4);

            if (!type.equals(ResourceType.TEXTURE))
                data.getTextureInfo().write(stream);
            stream.bytes(Compressor.getCompressedStream(data.getBuffer(), preferCompressed));

            stream.shrink();
            return stream.getBuffer();
        }

        stream.str(type.getHeader() + data.getMethod().getValue(), 4);

        Revision revision = data.getRevision();
        int head = revision.getHead();

        boolean isCompressed = head < 0x189;
        isCompressed = preferCompressed;

        stream.i32(head);
        if (head >= 0x109 || isStaticMesh) {
            stream.i32(0); // Dummy value for dependency table offset.
            if (head >= 0x189 && !isStaticMesh) {
                if (head >= 0x271) {
                    stream.i16(revision.getBranchID());
                    stream.i16(revision.getBranchRevision());
                }

                if (head >= 0x297 || (revision.has(Branch.LEERDAMMER, Revisions.LD_RESOURCES)))
                    stream.i8(data.getCompressionFlags());

                if (preferCompressed)
                    isCompressed = true;
                stream.bool(isCompressed);
            } else if (isStaticMesh)
                new Serializer(stream, revision).struct(data.getStaticMeshInfo(), StaticMeshInfo.class);
        }

        if (isCompressed || head < 0x189)
            buffer = Compressor.getCompressedStream(buffer, isCompressed);
        
        // Tell the game there are no streams in the zlib data,
        // technically we don't have to waste memory concatenating the streams,
        // since these resources can't be encrypted anyway, but whatever 
        else if (isStaticMesh && !preferCompressed)
            buffer = Bytes.combine(new byte[] { 0x00, 0x00, 0x00, 0x00 }, buffer);
        
        if (data.getMethod().equals(SerializationType.ENCRYPTED_BINARY)) {
            stream.i32(buffer.length);
            buffer = Crypto.XXTEA(buffer, false);
        }
        stream.bytes(buffer);

        if (head >= 0x109 || isStaticMesh) {
            // Setting dependency table offset
            int dependencyTableOffset = stream.getOffset();
            stream.seek(8, SeekMode.Begin);
            stream.i32(dependencyTableOffset);
            stream.seek(dependencyTableOffset, SeekMode.Begin);

            // Writing dependencies
            stream.i32(dependencies.length);
            for (ResourceDescriptor dependency : dependencies) {
                byte flags = 0;
                
                if (dependency != null) {
                    if (dependency.isGUID()) flags |= 2;
                    if (dependency.isHash()) flags |= 1;
                }

                stream.i8(flags);
                if (flags != 0) {
                    if ((flags & 2) != 0)
                        stream.guid(dependency.getGUID());
                    if ((flags & 1) != 0)
                        stream.sha1(dependency.getSHA1());
                }

                stream.i32(dependency != null ? dependency.getType().getValue() : 0);
            }
        }

        stream.shrink();
        return stream.getBuffer();
    }
    
    public void replaceDependency(ResourceDescriptor oldDescriptor, ResourceDescriptor newDescriptor) {
        if (oldDescriptor.equals(newDescriptor)) return;
        if (!this.dependencies.contains(oldDescriptor)) return;
        
        if (this.type != ResourceType.STATIC_MESH) {
            ResourceType type = oldDescriptor.getType();
            boolean isFSB = type.equals(ResourceType.FILENAME);
            byte[] oldDescBuffer, newDescBuffer;

            // Music dependencies are actually the GUID dependencies of a script,
            // so they don't have the same structure for referencing.
            if (type.equals(ResourceType.MUSIC_SETTINGS) || type.equals(ResourceType.FILE_OF_BYTES) || type.equals(ResourceType.SAMPLE) || isFSB) {
                oldDescBuffer = Bytes.getIntegerBuffer(oldDescriptor.getGUID().getValue(), this.compressionFlags);
                newDescBuffer = Bytes.getIntegerBuffer(newDescriptor.getGUID().getValue(), this.compressionFlags);
            } else {
                oldDescBuffer = Bytes.getResourceReference(oldDescriptor, this.revision, this.compressionFlags);
                newDescBuffer = Bytes.getResourceReference(newDescriptor, this.revision, this.compressionFlags);
            }


            if (this.type == ResourceType.PLAN) {
                RPlan plan = this.loadResource(RPlan.class);
                plan.thingData = Bytes.replace(plan.thingData, oldDescBuffer, newDescBuffer);
                if (isFSB && plan.inventoryData != null) {
                    if (oldDescriptor.getGUID().equals(plan.inventoryData.highlightSound))
                        plan.inventoryData.highlightSound = newDescriptor.getGUID();
                }
                this.data = plan.build(this.revision, this.compressionFlags).getBuffer();
            }

            this.data = Bytes.replace(this.data, oldDescBuffer, newDescBuffer);
        } else {
            if (this.meshInfo.fallmap.equals(oldDescriptor))
                this.meshInfo.fallmap = newDescriptor;
            if (this.meshInfo.lightmap.equals(oldDescriptor))
                this.meshInfo.lightmap = newDescriptor;
            if (this.meshInfo.risemap.equals(oldDescriptor))
                this.meshInfo.risemap = newDescriptor;
            for (StaticPrimitive primitive : this.meshInfo.primitives)
                if (primitive.gmat.equals(oldDescriptor))
                    primitive.gmat = newDescriptor;
        }
        
        this.dependencies.remove(oldDescriptor);
        if (newDescriptor != null)
            this.dependencies.add(newDescriptor);
    }

    public ResourceDescriptor[] getDependencies() { return this.dependencies.toArray(ResourceDescriptor[]::new); }
    public byte getCompressionFlags() { return this.compressionFlags; }
    public Revision getRevision() { return this.revision; }
    public ResourceType getResourceType() { return this.type; }
    public SerializationType getSerializationType() { return this.method; }
    public CellGcmTexture getTextureInfo() { return this.textureInfo; }
    public StaticMeshInfo getMeshInfo() { return this.meshInfo; }
}
