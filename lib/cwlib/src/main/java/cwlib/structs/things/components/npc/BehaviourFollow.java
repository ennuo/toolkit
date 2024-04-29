package cwlib.structs.things.components.npc;

import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class BehaviourFollow extends BehaviourBase
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public Thing followThing;
    public int lastFollowUpdate;

    @Override
    public void serialize(Serializer serializer)
    {
        super.serialize(serializer);

        followThing = serializer.thing(followThing);
        lastFollowUpdate = serializer.i32(lastFollowUpdate);
    }

    @Override
    public int getAllocatedSize()
    {
        return BehaviourFollow.BASE_ALLOCATION_SIZE;
    }
}
