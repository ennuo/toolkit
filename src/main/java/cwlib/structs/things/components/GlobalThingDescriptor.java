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
        
        int version = serializer.getRevision().getVersion();

        if (version < 0x160) {
            descriptor.levelDesc = serializer.resource(descriptor.levelDesc, ResourceType.LEVEL);
            descriptor.UID = serializer.i32(descriptor.UID);
        } else descriptor.levelDesc = serializer.resource(descriptor.levelDesc, ResourceType.PLAN);
        
        return descriptor;
    }

    @Override public int getAllocatedSize() { return GlobalThingDescriptor.BASE_ALLOCATION_SIZE; }
}
