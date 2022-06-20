package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;

public class SlotLink implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public SlotID from = new SlotID();
    public SlotID to = new SlotID();

    @SuppressWarnings("unchecked")
    @Override public SlotLink serialize(Serializer serializer, Serializable structure) {
        SlotLink link = (structure == null) ? new SlotLink() : (SlotLink) structure;

        link.from = serializer.struct(link.from, SlotID.class);
        link.to = serializer.struct(link.to, SlotID.class);

        return link;
    }

    @Override public int getAllocatedSize() { return SlotLink.BASE_ALLOCATION_SIZE; }
}
