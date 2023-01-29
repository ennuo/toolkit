package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class EmittedObjectSource implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x60;

    public Thing[] things;
    public ResourceDescriptor plan;

    // I'll check what these are at some point
    @GsonRevision(lbp3=true, min=0xcd)
    public float f0, f1, f2, f3, f4, f5;
    @GsonRevision(lbp3=true, min=0xcd)
    public byte b0;

    @SuppressWarnings("unchecked")
    @Override public EmittedObjectSource serialize(Serializer serializer, Serializable structure) {
        EmittedObjectSource source = (structure == null) ? new EmittedObjectSource() : (EmittedObjectSource) structure;

        source.things = serializer.thingarray(source.things);
        source.plan = serializer.resource(source.plan, ResourceType.PLAN);

        if (serializer.getRevision().getSubVersion() > 0xcc) {
            source.f0 = serializer.f32(source.f0);
            source.f1 = serializer.f32(source.f1);
            source.f2 = serializer.f32(source.f2);
            source.f3 = serializer.f32(source.f3);
            source.f4 = serializer.f32(source.f4);
            source.f5 = serializer.f32(source.f5);
            source.b0 = serializer.i8(source.b0);
        }

        return source;
    }


    @Override public int getAllocatedSize() {
        int size = EmittedObjectSource.BASE_ALLOCATION_SIZE;
        if (this.things != null) size += (this.things.length * 0x4);
        return size;
    }


}
