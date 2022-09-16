package cwlib.structs.slot;

import com.google.gson.annotations.JsonAdapter;

import cwlib.enums.SlotType;
import cwlib.io.Serializable;
import cwlib.io.gson.SlotIDSerializer;
import cwlib.io.serializer.Serializer;

/**
 * This structure represents a reference to a slot.
 */
@JsonAdapter(SlotIDSerializer.class)
public class SlotID implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public SlotType slotType = SlotType.DEVELOPER;
    public long slotNumber;

    /**
     * Constructs an empty Slot ID.
     */
    public SlotID(){};

    /**
     * Constructs a slot reference from a type and ID.
     * @param type Type of slot
     * @param ID ID of slot
     */
    public SlotID(SlotType type, long ID) {
        if (type == null)
            throw new NullPointerException("SlotType cannot be null!");
        this.slotType = type;
        this.slotNumber = ID;
    }

    @SuppressWarnings("unchecked")
    @Override public SlotID serialize(Serializer serializer, Serializable structure) {
        SlotID slot = (structure == null) ? new SlotID() : (SlotID) structure;
        
        slot.slotType = serializer.enum32(slot.slotType);
        slot.slotNumber = serializer.u32(slot.slotNumber);
        
        return slot;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }

    @Override public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SlotID)) return false;
        SlotID d = (SlotID)o;
        return (slotType.equals(d.slotType) && slotNumber == d.slotNumber);
    }

    @Override public int hashCode() {
        int result = (int) (this.slotNumber ^ (this.slotNumber >>> 32));
        result = 31 * result + (this.slotType != null ? this.slotType.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return String.format("SlotID{%s, %d}", this.slotType, this.slotNumber);
    }
}
