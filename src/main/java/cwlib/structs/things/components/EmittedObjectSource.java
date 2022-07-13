package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;

public class EmittedObjectSource implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public Thing[] things;
    public ResourceDescriptor plan;

    @SuppressWarnings("unchecked")
    @Override public EmittedObjectSource serialize(Serializer serializer, Serializable structure) {
        EmittedObjectSource source = (structure == null) ? new EmittedObjectSource() : (EmittedObjectSource) structure;

        source.things = serializer.thingarray(source.things);
        source.plan = serializer.resource(source.plan, ResourceType.PLAN);

        return source;
    }


    @Override public int getAllocatedSize() {
        int size = EmittedObjectSource.BASE_ALLOCATION_SIZE;
        if (this.things != null) size += (this.things.length * 0x4);
        return size;
    }


}
