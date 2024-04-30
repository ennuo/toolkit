package cwlib.structs.level;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class AdventureItem implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public int PUID;
    public ResourceDescriptor descriptor;
    public int flags;
    public int iconPUID;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        PUID = serializer.i32(PUID);
        descriptor = serializer.resource(descriptor, ResourceType.PLAN, true);

        if (subVersion < 0xae)
        {
            serializer.u16(0);
            serializer.u8(0);
            serializer.f32(0);
            serializer.i32(0);
        }

        if (subVersion > 0xbe)
            flags = serializer.i32(flags);
        if (subVersion > 0xe0)
            iconPUID = serializer.i32(iconPUID);
    }

    @Override
    public int getAllocatedSize()
    {
        return AdventureItem.BASE_ALLOCATION_SIZE;
    }
}
