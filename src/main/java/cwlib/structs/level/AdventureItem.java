package cwlib.structs.level;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class AdventureItem implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;
    
    public int PUID;
    public ResourceDescriptor descriptor;
    public int flags;
    public int iconPUID;

    @SuppressWarnings("unchecked")
    @Override public AdventureItem serialize(Serializer serializer, Serializable structure) {
        AdventureItem item = (structure == null) ? new AdventureItem() : (AdventureItem) structure;

        int subVersion = serializer.getRevision().getSubVersion();

        item.PUID = serializer.i32(item.PUID);
        item.descriptor = serializer.resource(descriptor, ResourceType.PLAN, true);

        if (subVersion < 0xae) {
            serializer.u16(0);
            serializer.u8(0);
            serializer.f32(0);
            serializer.i32(0);
        }

        if (subVersion > 0xbe)
            item.flags = serializer.i32(item.flags);
        if (subVersion > 0xe0)
            item.iconPUID = serializer.i32(item.iconPUID);

        return item;
    }

    @Override public int getAllocatedSize() {
        return AdventureItem.BASE_ALLOCATION_SIZE;
    }
}
