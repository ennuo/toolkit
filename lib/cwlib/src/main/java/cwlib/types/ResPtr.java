package cwlib.types;

import java.lang.reflect.ParameterizedType;

import com.google.gson.reflect.TypeToken;

import cwlib.enums.ResourceType;
import cwlib.io.Resource;
import cwlib.resources.RMesh;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;

public class ResPtr<T extends Resource>
{
    private T resource;
    private SerializedResource serializedResource;
    private final ResourceType type;
    private GUID guid;
    private final SHA1 loadedHash = SHA1.EMPTY;
    private int loadState;

    public ResPtr(ResourceDescriptor descriptor)
    {
        type = descriptor.getType();
    }

    public ResPtr()
    {
        type = ResourceType.INVALID;
    }
}
