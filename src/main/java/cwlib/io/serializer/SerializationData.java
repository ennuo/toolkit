package cwlib.io.serializer;

import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.structs.staticmesh.StaticMeshInfo;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

/**
 * Structure for holding data related to
 * serialization necessary for compressing the resource.
 */
public final class SerializationData {
    private final byte[] buffer;
    private final Revision revision;
    private final byte compressionFlags;
    private final ResourceType type;
    private final SerializationType method;
    private final ResourceDescriptor[] dependencies;
    private final CellGcmTexture textureInfo;
    private final StaticMeshInfo staticMeshInfo;


    /**
     * Creates a serialization data structure.
     * @param buffer Buffer to be compressed
     * @param revision Revision of the serialized resource
     * @param compressionFlags Compression flags used during resource serialization
     * @param type Type of resource
     * @param method Method of serialization
     * @param dependencies Resources this resource depends on
     */
    public SerializationData(
        byte[] buffer, 
        Revision revision, 
        byte compressionFlags,
        ResourceType type,
        SerializationType method, 
        ResourceDescriptor[] dependencies) {
        this.buffer = buffer;
        this.revision = revision;
        this.compressionFlags = compressionFlags;
        this.type = type;
        this.method = method;
        this.dependencies =	dependencies;
        this.textureInfo = null;
        this.staticMeshInfo = null;
    }

    /**
     * Creates a serialization data structure for texture data.
     * @param buffer Decompressed texture data
     */
    public SerializationData(byte[] buffer) {
        this.buffer = buffer;
        this.revision = null;
        this.compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        this.type = ResourceType.TEXTURE;
        this.method = SerializationType.COMPRESSED_TEXTURE;
        this.dependencies = null;
        this.textureInfo = null;
        this.staticMeshInfo = null;
    }

    /**
     * Creates a serialization data structure for GTF texture data.
     * @param buffer Decompressed texture data
     * @param info Texture metadata
     */
    public SerializationData(byte[] buffer, CellGcmTexture info) {
        this.buffer = buffer;
        this.revision = null;
        this.compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        this.type = ResourceType.GTF_TEXTURE;
        this.method = info.getMethod();
        this.dependencies = null;
        this.textureInfo = info;
        this.staticMeshInfo = null;
    }

    /**
     * Creates a serialization data structure for static mesh data
     * @param buffer Decompressed static mesh
     * @param revision Revision of the serialized resource
     * @param info Static mesh metadata
     */
    public SerializationData(byte[] buffer, Revision revision, StaticMeshInfo info) {
        this.buffer = buffer;
        this.revision = revision;
        this.compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        this.type = ResourceType.STATIC_MESH;
        this.method = SerializationType.BINARY;

        // Maybe I need a method for gathering dependencies without actually
        // serializing the data, can probably use reflection for it,
        // as to avoid serializing twice.

        Serializer serializer = new Serializer(info.getAllocatedSize(), revision, CompressionFlags.USE_NO_COMPRESSION);
        serializer.struct(info, StaticMeshInfo.class);
        this.dependencies = serializer.getDependencies();
        
        this.textureInfo = null;
        this.staticMeshInfo = info;
    }

    public byte[] getBuffer() { return this.buffer; }
    public Revision getRevision() { return this.revision; }
    public byte getCompressionFlags() { return this.compressionFlags; }
    public ResourceType getType() { return this.type; }
    public SerializationType getMethod() { return this.method; }
    public ResourceDescriptor[] getDependencies() { return this.dependencies; }
    public CellGcmTexture getTextureInfo() { return this.textureInfo; }
    public StaticMeshInfo getStaticMeshInfo() { return this.staticMeshInfo; }
}
