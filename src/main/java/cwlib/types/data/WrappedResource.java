package cwlib.types.data;

import com.google.gson.annotations.JsonAdapter;
import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.io.Compressable;
import cwlib.io.gson.WrappedResourceSerializer;
import cwlib.types.Resource;

@JsonAdapter(WrappedResourceSerializer.class)
public class WrappedResource {
    public Revision revision;
    public ResourceType type;
    public Object resource;

    public WrappedResource(){};
    public WrappedResource(Resource resource) {
        this.revision = resource.getRevision();
        this.type = resource.getResourceType();
        this.resource = resource.loadResource(this.type.getCompressable());
    }
    
    public byte[] build() {
        int version = this.revision.getVersion();
        byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        if (version >= 0x297 || (version == 0x272 && (this.revision.getBranchID() == 0x4c44) && ((this.revision.getBranchRevision() & 0xffff) > 1)))
            compressionFlags = CompressionFlags.USE_ALL_COMPRESSION;
        return Resource.compress(((Compressable)this.resource).build(this.revision, compressionFlags));
    }
}