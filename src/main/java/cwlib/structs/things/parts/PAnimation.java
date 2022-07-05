package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class PAnimation implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceDescriptor animation;
    public float velocity, position;

    @SuppressWarnings("unchecked")
    @Override public PAnimation serialize(Serializer serializer, Serializable structure) {
        PAnimation animation = (structure == null) ? new PAnimation() : (PAnimation) structure;
        
        animation.animation = serializer.resource(animation.animation, ResourceType.ANIMATION);
        animation.velocity = serializer.f32(animation.velocity);
        animation.position = serializer.f32(animation.position);
        
        return animation;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
