package cwlib.structs.profile;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class CollectableData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceDescriptor plan;
    public int source;

    // -1 =invalid
    // 0 = egg
    // 1 = award_complete
    // 2 = award_collect
    // 3 = award_ace

    @SuppressWarnings("unchecked")
    @Override public CollectableData serialize(Serializer serializer, Serializable structure) {
        CollectableData data = (structure == null) ? new CollectableData() : (CollectableData) structure;

        if (serializer.getRevision().getVersion() >= 0x1c2) {
            data.plan = serializer.resource(data.plan, ResourceType.PLAN, true);
            data.source = serializer.s32(data.source);
        }

        return data;
    }

    @Override public int getAllocatedSize() { return CollectableData.BASE_ALLOCATION_SIZE; }
}
