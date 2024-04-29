package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class PAnimation implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceDescriptor animation;
    public float velocity, position;

    @Override
    public void serialize(Serializer serializer)
    {
        animation = serializer.resource(animation, ResourceType.ANIMATION);
        velocity = serializer.f32(velocity);
        position = serializer.f32(position);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
