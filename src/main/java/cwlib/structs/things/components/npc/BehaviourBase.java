package cwlib.structs.things.components.npc;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class BehaviourBase implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public Thing npc;
    public int type;
    public int attributes;

    @SuppressWarnings("unchecked")
    @Override public BehaviourBase serialize(Serializer serializer, Serializable structure) {
        BehaviourBase base = (structure == null) ? new BehaviourBase() : (BehaviourBase) structure;

        base.npc = serializer.thing(base.npc);
        base.type = serializer.s32(base.type);
        base.attributes = serializer.i32(base.attributes);
        
        return base;
    }
    
    @Override public int getAllocatedSize() { return BehaviourBase.BASE_ALLOCATION_SIZE; }
}
