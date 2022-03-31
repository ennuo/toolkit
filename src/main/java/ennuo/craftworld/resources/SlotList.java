package ennuo.craftworld.resources;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class SlotList implements Serializable {
    public Slot[] slots;
    public boolean fromProductionBuild = true;

    @SuppressWarnings("unchecked")
    @Override public SlotList serialize(Serializer serializer, Serializable structure) {
        SlotList slots = (structure == null) ? new SlotList() : (SlotList) structure;

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
