package cwlib.structs.things.components.npc;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class BehaviourBase implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public Thing npc;
    public int type;
    public int attributes;

    @Override
    public void serialize(Serializer serializer)
    {
        npc = serializer.thing(npc);
        type = serializer.s32(type);
        attributes = serializer.i32(attributes);
    }

    @Override
    public int getAllocatedSize()
    {
        return BehaviourBase.BASE_ALLOCATION_SIZE;
    }
}
