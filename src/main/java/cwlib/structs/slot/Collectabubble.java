package cwlib.structs.slot;

import cwlib.enums.ResourceType;
import cwlib.types.data.ResourceReference;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class Collectabubble implements Serializable {
    public static int MAX_SIZE = 0x15;
    
    public ResourceReference item;
    public int count;

    @Override
    public Collectabubble serialize(Serializer serializer, Serializable structure) {
        Collectabubble collectabubble = (structure == null) ? new Collectabubble() : (Collectabubble) structure;
        
        collectabubble.item = serializer.resource(collectabubble.item, ResourceType.PLAN, true);
        collectabubble.count = serializer.i32(collectabubble.count);
        
        return collectabubble;
    }
    
}
