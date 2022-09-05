package cwlib.structs.things.components.poppet;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class DecorationInfo implements Serializable {
    public static int BASE_ALLOCATION_SIZE = 0x150;

    public float angle;
    public int lastDecoration;
    public Thing lastDecoratedThing;
    public ResourceDescriptor decoration;
    public float scale;
    public boolean reversed;
    
    @GsonRevision(min=0x148)
    public boolean stamping;

    @GsonRevision(min=0x178)
    public ResourceDescriptor plan;

    @SuppressWarnings("unchecked")
    @Override public DecorationInfo serialize(Serializer serializer, Serializable structure) {
        DecorationInfo info = (structure == null) ? new DecorationInfo() : (DecorationInfo) structure;

        int version = serializer.getRevision().getVersion();

        info.angle = serializer.f32(info.angle);
        info.lastDecoration = serializer.i32(info.lastDecoration);
        info.lastDecoratedThing = serializer.thing(info.lastDecoratedThing);
        info.decoration = serializer.resource(info.decoration, ResourceType.MESH);
        info.scale = serializer.f32(info.scale);
        info.reversed = serializer.bool(info.reversed);
        if (version > 0x147)
            info.stamping = serializer.bool(info.stamping);
        if (version > 0x177)
            info.plan = serializer.resource(info.plan, ResourceType.PLAN, true);
        
        return info;
    }


    @Override public int getAllocatedSize() { return DecorationInfo.BASE_ALLOCATION_SIZE; }
    

}