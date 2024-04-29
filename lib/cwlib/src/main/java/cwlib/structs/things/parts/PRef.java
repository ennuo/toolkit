package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.GlobalThingDescriptor;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.ResourceDescriptor;

public class PRef implements Serializable
{
    @GsonRevision(max = 0x15f)
    @Deprecated
    public GlobalThingDescriptor thing;

    @GsonRevision(min = 0x160)
    public ResourceDescriptor plan;

    public int oldLifetime;

    @GsonRevision(min = 0x1c9)
    public int oldAliveFrames;

    @GsonRevision(max = 0x320)
    public boolean childrenSelectable;

    @GsonRevision(min = 0x13d, max = 0x320)
    public boolean stripChildren;

    public PRef() { }

    public PRef(ResourceDescriptor descriptor)
    {
        this.plan = descriptor;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        if (version < 0x160)
            thing = serializer.struct(thing, GlobalThingDescriptor.class);
        else
            plan = serializer.resource(plan, ResourceType.PLAN, true, false, false);

        oldLifetime = serializer.i32(oldLifetime);
        if (version >= 0x1c9)
            oldAliveFrames = serializer.i32(oldAliveFrames);
        if (version < 0x321)
            childrenSelectable = serializer.bool(childrenSelectable);

        if (version < 0x19e)
            serializer.array(null, Thing.class, true);

        if (version >= 0x13d && version < 0x321)
            stripChildren = serializer.bool(stripChildren);

        if (version > 0x171 && version < 0x180) serializer.u8(0);
        if (version > 0x17f && version < 0x19e)
        {
            serializer.u8(0);
            serializer.struct(null, NetworkPlayerID.class);
        }
    }

    // TODO: Actually implement
    @Override
    public int getAllocatedSize()
    {
        return 0;
    }
}
