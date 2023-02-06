package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.GlobalThingDescriptor;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.ResourceDescriptor;

public class PRef implements Serializable {
    @GsonRevision(max=0x15f)
    @Deprecated public GlobalThingDescriptor thing;
    
    @GsonRevision(min=0x160)
    public ResourceDescriptor plan;

    public int oldLifetime;
    
    @GsonRevision(min=0x1c9)
    public int oldAliveFrames;

    public boolean childrenSelectable = true;

    @GsonRevision(min=0x13d)
    public boolean stripChildren;

    public PRef() {};
    public PRef(ResourceDescriptor descriptor) {
        this.plan = descriptor;
    }

    @SuppressWarnings("unchecked")
    @Override public PRef serialize(Serializer serializer, Serializable structure) {
        PRef ref = (structure == null) ? new PRef() : (PRef) structure;

        int version = serializer.getRevision().getVersion();

        if (version < 0x160)
            ref.thing = serializer.struct(ref.thing, GlobalThingDescriptor.class);
        else
            ref.plan = serializer.resource(ref.plan, ResourceType.PLAN, true, false, false);
        
        ref.oldLifetime = serializer.i32(ref.oldLifetime);
        if (version >= 0x1c9)
            ref.oldAliveFrames = serializer.i32(ref.oldAliveFrames);
        ref.childrenSelectable = serializer.bool(ref.childrenSelectable);
        
        if (version < 0x19e)
            serializer.array(null, Thing.class, true);
        
        if (version >= 0x13d)
            ref.stripChildren = serializer.bool(ref.stripChildren);

        if (version > 0x171 && version < 0x180) serializer.u8(0);
        if (version > 0x17f && version < 0x19e) {
            serializer.u8(0);
            serializer.struct(null, NetworkPlayerID.class);
        }
        
        return ref;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
}
