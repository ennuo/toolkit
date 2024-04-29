package cwlib.structs.things.components;

import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class HUDDecal implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public ResourceDescriptor texture;
    public int frameNo, gpuColor;
    public Vector4f[] corners = new Vector4f[4];

    @Override
    public void serialize(Serializer serializer)
    {
        if (serializer.getRevision().getSubVersion() < 0x24)
            return;

        texture = serializer.resource(texture, ResourceType.TEXTURE);
        frameNo = serializer.i32(frameNo);
        gpuColor = serializer.i32(gpuColor);
        for (int i = 0; i < 4; ++i)
            corners[i] = serializer.v4(corners[i]);
    }

    @Override
    public int getAllocatedSize()
    {
        return HUDDecal.BASE_ALLOCATION_SIZE;
    }
}
