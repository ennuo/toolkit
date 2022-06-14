package cwlib.presets;

import cwlib.types.data.ResourceReference;
import cwlib.enums.Crater;
import cwlib.enums.SlotType;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;

public class SlotPresets {
    
    public static Slot Crater(ResourceReference level, int ID) {
        Slot slot = new Slot();

        SlotID slotID = new SlotID();
        slotID.type = SlotType.USER_CREATED_STORED_LOCAL;
        slotID.ID = ID;
        
        
        slot.id = slotID;
        slot.location = Crater.valueOf("SLOT_" + ID).value;
        
        slot.root = level;
        
        return slot;
    }
}
