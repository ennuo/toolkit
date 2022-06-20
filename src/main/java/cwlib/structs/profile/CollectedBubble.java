package cwlib.structs.profile;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class CollectedBubble implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public ResourceDescriptor level;
    public int thingUID;
    public ResourceDescriptor plan;

    @SuppressWarnings("unchecked")
    @Override public CollectedBubble serialize(Serializer serializer, Serializable structure) {
        CollectedBubble bubble = (structure == null) ? new CollectedBubble() : (CollectedBubble) structure;

        bubble.level = serializer.resource(bubble.level, ResourceType.LEVEL, true);
        bubble.thingUID = serializer.i32(bubble.thingUID);
        bubble.plan = serializer.resource(bubble.plan, ResourceType.PLAN, true);

        return bubble;
    }

    @Override public int getAllocatedSize() { return CollectedBubble.BASE_ALLOCATION_SIZE; }
}
