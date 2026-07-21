package cwlib.types.data;

import com.google.gson.annotations.JsonAdapter;
import cwlib.enums.ResourceType;
import cwlib.io.Resource;
import cwlib.io.gson.WrappedResourceSerializer;
import cwlib.types.SerializedResource;
import cwlib.util.GsonUtils;

import java.nio.charset.StandardCharsets;

@JsonAdapter(WrappedResourceSerializer.class)
public class WrappedResource
{
    public Revision revision;
    public ResourceType type;
    public Object resource;

    public WrappedResource() { }

    public WrappedResource(SerializedResource resource)
    {
        this.revision = resource.getRevision();
        this.type = resource.getResourceType();
        this.resource = resource.loadResource(this.type.getCompressable());
    }

    public byte[] toJSON()
    {
        return GsonUtils.toJSON(this, this.revision).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] build()
    {
        return SerializedResource.compress(((Resource) this.resource).build(revision, revision.getDefaultCompressionFlags()));
    }
}