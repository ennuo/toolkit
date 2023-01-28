package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class GlobalThingDescriptor implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public ResourceDescriptor levelDesc;
    public int UID;

    @SuppressWarnings("unchecked")
    @Override public GlobalThingDescriptor serialize(Serializer serializer, Serializable structure) {
        GlobalThingDescriptor descriptor = 
            (structure == null) ? new GlobalThingDescriptor() : (GlobalThingDescriptor) structure;
        
        descriptor.levelDesc = serializer.resource(descriptor.levelDesc, ResourceType.LEVEL, true, false, false);
        descriptor.UID = serializer.i32(descriptor.UID);

        return descriptor;
    }

    @Override public int getAllocatedSize() { return GlobalThingDescriptor.BASE_ALLOCATION_SIZE; }
}
