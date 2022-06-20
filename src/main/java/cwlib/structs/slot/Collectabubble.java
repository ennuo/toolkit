package cwlib.structs.slot;

import cwlib.enums.ResourceType;
import cwlib.types.data.ResourceDescriptor;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Collectabubble implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;
    
    public ResourceDescriptor plan;
    public int count;

    @SuppressWarnings("unchecked")
    @Override public Collectabubble serialize(Serializer serializer, Serializable structure) {
        Collectabubble collectabubble = (structure == null) ? new Collectabubble() : (Collectabubble) structure;
        
        collectabubble.plan = serializer.resource(collectabubble.plan, ResourceType.PLAN, true);
        collectabubble.count = serializer.i32(collectabubble.count);
        
        return collectabubble;
    }
    
    @Override public int getAllocatedSize() { return Collectabubble.BASE_ALLOCATION_SIZE; }
}