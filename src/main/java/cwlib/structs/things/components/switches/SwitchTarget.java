package cwlib.structs.things.components.switches;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class SwitchTarget implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public Thing thing;
    public int port;

    public SwitchTarget() {};
    public SwitchTarget(Thing thing) { this.thing = thing; }
    
    @SuppressWarnings("unchecked")
    @Override public SwitchTarget serialize(Serializer serializer, Serializable structure) {
        SwitchTarget target = (structure == null) ? new SwitchTarget() : (SwitchTarget) structure;

        target.thing = serializer.thing(target.thing);
        target.port = serializer.i32(target.port);
        
        return target;
    }

    @Override public int getAllocatedSize() { return SwitchTarget.BASE_ALLOCATION_SIZE; }
}
