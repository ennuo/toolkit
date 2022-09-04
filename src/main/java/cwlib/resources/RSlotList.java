package cwlib.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.types.data.Revision;
import cwlib.structs.slot.Slot;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;

public class RSlotList implements Serializable, Compressable, Iterable<Slot> {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    private ArrayList<Slot> slots = new ArrayList<>();
    @GsonRevision(min=950)
    public boolean fromProductionBuild = true;

    public RSlotList() {}
    public RSlotList(ArrayList<Slot> slots) { this.slots = slots; }
    public RSlotList(Slot[] slots) {
        this.slots = new ArrayList<Slot>(Arrays.asList(slots));
    }

    @SuppressWarnings("unchecked")
    @Override public RSlotList serialize(Serializer serializer, Serializable structure) {
        RSlotList slots = (structure == null) ? new RSlotList() : (RSlotList) structure;

        slots.slots = serializer.arraylist(slots.slots, Slot.class);
        if (serializer.getRevision().getVersion() >= Revisions.PRODUCTION_BUILD)
            slots.fromProductionBuild = serializer.bool(slots.fromProductionBuild);

        return slots;
    }
    
    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        if (this.slots != null) {
            for (Slot slot : slots)
                size += slot.getAllocatedSize();
        }
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RSlotList.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.SLOT_LIST,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    @Override public Iterator<Slot> iterator() { return this.slots.iterator(); }

    public ArrayList<Slot> getSlots() { return this.slots; }
}
