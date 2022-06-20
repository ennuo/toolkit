package cwlib.structs.profile;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;

public class CollectableData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceReference plan;
    public int source;

    @SuppressWarnings("unchecked")
    @Override public CollectableData serialize(Serializer serializer, Serializable structure) {
        CollectableData data = (structure == null) ? new CollectableData() : (CollectableData) structure;

        data.plan = serializer.resource(data.plan, ResourceType.PLAN, true);
        data.source = serializer.i32(data.source);

        return data;
    }

    @Override public int getAllocatedSize() { return CollectableData.BASE_ALLOCATION_SIZE; }
}
