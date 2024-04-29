package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class EmittedObjectSource implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public Thing[] things;
    public ResourceDescriptor plan;

    // I'll check what these are at some point
    @GsonRevision(lbp3 = true, min = 0xcd)
    public float f0, f1, f2, f3, f4, f5;
    @GsonRevision(lbp3 = true, min = 0xcd)
    public byte b0;

    @Override
    public void serialize(Serializer serializer)
    {
        things = serializer.thingarray(things);
        plan = serializer.resource(plan, ResourceType.PLAN);

        if (serializer.getRevision().getSubVersion() > 0xcc)
        {
            f0 = serializer.f32(f0);
            f1 = serializer.f32(f1);
            f2 = serializer.f32(f2);
            f3 = serializer.f32(f3);
            f4 = serializer.f32(f4);
            f5 = serializer.f32(f5);
            b0 = serializer.i8(b0);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = EmittedObjectSource.BASE_ALLOCATION_SIZE;
        if (this.things != null) size += (this.things.length * 0x4);
        return size;
    }


}
