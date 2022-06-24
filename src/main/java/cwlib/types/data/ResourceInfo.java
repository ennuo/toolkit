package cwlib.types.data;

import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.resources.RStaticMesh;
import cwlib.resources.RTexture;
import cwlib.types.Resource;

public class ResourceInfo {
    private Object resource;
    private Revision revision;
    private ResourceType type = ResourceType.INVALID;
    private byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
    private ResourceDescriptor[] dependencies;
    private boolean isMissingDependencies;

    public <T extends Compressable> ResourceInfo(byte[] source) {
        if (source == null) return;

        Resource resource = new Resource(source);
        this.type = resource.getResourceType();
        if (this.type == ResourceType.INVALID) return;
        this.revision = resource.getRevision();
        this.compressionFlags = resource.getCompressionFlags();
        this.dependencies = resource.getDependencies();

        Class<? extends Serializable> clazz = this.type.getCompressable();
        if (clazz != null) {
            try { this.resource = resource.loadResource(clazz); } 
            catch (Exception ex) { this.resource = null; }
        }

        if (this.type == ResourceType.GTF_TEXTURE || this.type == ResourceType.TEXTURE)
            this.resource = new RTexture(resource);

        if (this.type == ResourceType.STATIC_MESH)
            this.resource = new RStaticMesh(resource);
    }

    @SuppressWarnings("unchecked") public <T> T getResource() { return (T) this.resource; }
    public Revision getRevision() { return this.revision; }
    public ResourceType getType() { return this.type; }
    public byte getCompressionFlags() { return this.compressionFlags; }
    public ResourceDescriptor[] getDependencies() { return this.dependencies; }
    public boolean isMissingDependencies() { return this.isMissingDependencies; }
    public boolean isResource() { return this.type != ResourceType.INVALID; }
}
