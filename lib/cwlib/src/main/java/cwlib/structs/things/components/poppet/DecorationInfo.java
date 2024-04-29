package cwlib.structs.things.components.poppet;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class DecorationInfo implements Serializable
{
    public static int BASE_ALLOCATION_SIZE = 0x150;

    public float angle;
    public int lastDecoration;
    public Thing lastDecoratedThing;
    public ResourceDescriptor decoration;
    public float scale;
    public boolean reversed;

    @GsonRevision(min = 0x148)
    public boolean stamping;

    @GsonRevision(min = 0x178)
    public ResourceDescriptor plan;

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        angle = serializer.f32(angle);
        lastDecoration = serializer.i32(lastDecoration);
        lastDecoratedThing = serializer.thing(lastDecoratedThing);
        decoration = serializer.resource(decoration, ResourceType.MESH);
        scale = serializer.f32(scale);
        reversed = serializer.bool(reversed);
        if (version > 0x147)
            stamping = serializer.bool(stamping);
        if (version > 0x177)
            plan = serializer.resource(plan, ResourceType.PLAN, true);
    }


    @Override
    public int getAllocatedSize()
    {
        return DecorationInfo.BASE_ALLOCATION_SIZE;
    }


}