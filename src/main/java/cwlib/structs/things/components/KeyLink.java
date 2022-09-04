package cwlib.structs.things.components;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class KeyLink implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public SlotID slotID;

    @GsonRevision(max=0x16e)
    @Deprecated public int unknown;

    @SuppressWarnings("unchecked")
    @Override public KeyLink serialize(Serializer serializer, Serializable structure) {
        KeyLink link = (structure == null) ? new KeyLink() : (KeyLink) structure;

        link.slotID = serializer.struct(link.slotID, SlotID.class);
        if (serializer.getRevision().getVersion() < 0x16f)
            link.unknown = serializer.i32(link.unknown);
        
        return link;
    }

    @Override public int getAllocatedSize() { return KeyLink.BASE_ALLOCATION_SIZE; }
}
