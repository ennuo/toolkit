package cwlib.resources;

import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.types.data.Revision;
import cwlib.structs.slot.Slot;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class RSlotList implements Serializable {
    public Slot[] slots;
    public boolean fromProductionBuild = true;

    @SuppressWarnings("unchecked")
    @Override public RSlotList serialize(Serializer serializer, Serializable structure) {
        RSlotList slots = (structure == null) ? new RSlotList() : (RSlotList) structure;

        slots.slots = serializer.array(slots.slots, Slot.class);
        if (serializer.revision.head > 0x3b5)
            slots.fromProductionBuild = serializer.bool(slots.fromProductionBuild);

        return slots;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        int dataSize = 0x1000 * this.slots.length;
        Serializer serializer = new Serializer(dataSize, revision, compressionFlags);
        this.serialize(serializer, this);
        return Resource.compressToResource(serializer.output, ResourceType.SLOT_LIST);      
    }
}
