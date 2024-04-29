package cwlib.structs.things.components.switches;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class SwitchTarget implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    public Thing thing;

    @GsonRevision(min = 0x327)
    public int port;

    public SwitchTarget() { }

    public SwitchTarget(Thing thing)
    {
        this.thing = thing;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        thing = serializer.thing(thing);
        if (serializer.getRevision().getVersion() > 0x326)
            port = serializer.i32(port);
    }

    @Override
    public int getAllocatedSize()
    {
        return SwitchTarget.BASE_ALLOCATION_SIZE;
    }
}
