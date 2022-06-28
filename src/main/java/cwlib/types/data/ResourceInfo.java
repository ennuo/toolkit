package cwlib.types.data;

import cwlib.enums.Branch;
import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.resources.RStaticMesh;
import cwlib.resources.RTexture;
import cwlib.types.Resource;
import toolkit.utilities.ResourceSystem;

public class ResourceInfo {
    private Object resource;
    private Revision revision;
    private ResourceType type = ResourceType.INVALID;
    private byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
    private ResourceDescriptor[] dependencies;
    private boolean isMissingDependencies;

    public <T extends Compressable> ResourceInfo(byte[] source) {
        if (source == null || source.length < 4) return;

        ResourceType type = ResourceType.fromMagic(new String(new byte[] { source[0], source[1], source[2] }));
        SerializationType method = SerializationType.fromValue(Character.toString((char) source[3]));
        if (type == ResourceType.INVALID || type == ResourceType.FONTFACE || method == SerializationType.UNKNOWN) return;

        Resource resource = new Resource(source);
        this.type = resource.getResourceType();
        this.revision = resource.getRevision();
        this.compressionFlags = resource.getCompressionFlags();
        this.dependencies = resource.getDependencies();

        if (method == SerializationType.BINARY || method == SerializationType.ENCRYPTED_BINARY) {
            ResourceSystem.println("Resource Type: " + this.type.name());
            ResourceSystem.println(this.revision);
            short branchID = this.revision.getBranchID();
            if (branchID != 0) {
                Branch branch = Branch.fromID(branchID);
                ResourceSystem.println("Branch: " + (branch == null ? "UNRESOLVED" : branch.name()));
            }
            if (this.compressionFlags != 0)
                ResourceSystem.println(String.format("Compression Flags: %s (%d)", CompressionFlags.toString(this.compressionFlags), this.getCompressionFlags()));
            if (this.type != ResourceType.STATIC_MESH) {
                Class<? extends Serializable> clazz = this.type.getCompressable();
                if (clazz != null) {
                    try { this.resource = resource.loadResource(clazz); } 
                    catch (SerializationException ex) { 
                        ResourceSystem.println("Encountered error while deserializing resource, received message:");
                        ResourceSystem.println(ex.getMessage());
                        this.resource = null; 
                    }
                    catch (Exception ex) {
                        ResourceSystem.println("An unknown error occurred while processing resource, printing stacktrace:");
                        ex.printStackTrace();
                    }
                } else ResourceSystem.println(this.type.name() + " is unregistered!");
            }
            if (this.type == ResourceType.STATIC_MESH)
                this.resource = new RStaticMesh(resource);
        }

        if (this.type == ResourceType.GTF_TEXTURE || this.type == ResourceType.TEXTURE) {
            RTexture texture = new RTexture(resource);
            this.resource = texture;
        }
    }

    @SuppressWarnings("unchecked") public <T> T getResource() { return (T) this.resource; }
    public Revision getRevision() { return this.revision; }
    public ResourceType getType() { return this.type; }
    public byte getCompressionFlags() { return this.compressionFlags; }
    public ResourceDescriptor[] getDependencies() { return this.dependencies; }
    public boolean isMissingDependencies() { return this.isMissingDependencies; }
    public boolean isResource() { return this.type != ResourceType.INVALID; }
}
