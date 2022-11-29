package cwlib.structs.things.components.npc;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class BehaviourFollow extends BehaviourBase {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public Thing followThing;
    public int lastFollowUpdate;

    @SuppressWarnings("unchecked")
    @Override public BehaviourFollow serialize(Serializer serializer, Serializable structure) {
        BehaviourFollow follow = (structure == null) ? new BehaviourFollow() : (BehaviourFollow) structure;

        super.serialize(serializer, follow);

        follow.followThing = serializer.thing(follow.followThing);
        follow.lastFollowUpdate = serializer.i32(follow.lastFollowUpdate);

        return follow;
    }
    
    @Override public int getAllocatedSize() { return BehaviourFollow.BASE_ALLOCATION_SIZE; }
}
