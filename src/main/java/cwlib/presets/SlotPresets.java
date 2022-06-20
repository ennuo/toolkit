package cwlib.presets;

import cwlib.types.data.ResourceDescriptor;
import cwlib.enums.Crater;
import cwlib.enums.SlotType;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;

public class SlotPresets {
    
    public static Slot Crater(ResourceDescriptor level, int ID) {
        Slot slot = new Slot();

        SlotID slotID = new SlotID(SlotType.USER_CREATED_STORED_LOCAL, ID);

        slot.id = slotID;
        slot.location = Crater.valueOf("SLOT_" + ID).getValue();
        
        slot.root = level;
        
        return slot;
    }
}
