package ennuo.craftworld.resources.structs.presets;

import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.enums.Crater;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.structs.SlotID;

public class SlotPresets {
    
    public static Slot Crater(ResourcePtr level, int ID) {
        Slot slot = new Slot();

        SlotID slotID = new SlotID();
        slotID.type = SlotType.USER_CREATED_STORED_LOCAL;
        slotID.ID = ID;
        
        
        slot.slot = slotID;
        slot.group = slotID;
        slot.location = Crater.valueOf("SLOT_" + ID).value;
        
        slot.root = level;
        
        return slot;
    }
}
