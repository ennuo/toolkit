package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PPocketItem implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x50;

    public short flags;
    public byte powerUpType;
    public float aimModifier;
    public int lifetime;

    @SuppressWarnings("unchecked")
    @Override public PPocketItem serialize(Serializer serializer, Serializable structure) {
        PPocketItem item = (structure == null) ? new PPocketItem() : (PPocketItem) structure;
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion < 0x15) {
            serializer.str(null);
            serializer.u16(0);
        }

        if (subVersion >= 0x15 && subVersion < 0x14d)
            serializer.u16(0);

        item.flags = serializer.i16(item.flags);

        if (subVersion >= 0x14 && subVersion < 0x60)
            serializer.resource(null, ResourceType.PLAN, true);

        if (subVersion >= 0x1e && subVersion < 0x78)
            serializer.u8(0);
        if (subVersion >= 0x2e && subVersion < 0x14d)
            serializer.u8(0);
        if (subVersion >= 0x2e && subVersion < 0x78)
            serializer.u8(0);

        if (subVersion > 0x3d)
            item.powerUpType = serializer.i8(item.powerUpType);

        if (subVersion > 0x3f)
            item.aimModifier = serializer.f32(item.aimModifier);

        if (subVersion > 0x55)
            item.lifetime = serializer.i32(item.lifetime);

        return item;
    }
    
    @Override public int getAllocatedSize() { return PPocketItem.BASE_ALLOCATION_SIZE; }
}
