package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.resources.enums.SlotType;

public class SlotID {
    public static int MAX_SIZE = 0x10;
    
    public SlotType type = SlotType.DEVELOPER;
    public long ID = 0;
    

    public SlotID() {}
    
    public SlotID(Data data) {
        type = SlotType.getValue(data.int32());
        ID = data.uint32();
    }
    
    public SlotID(SlotType type, int ID) {
        this.type = type;
        this.ID = ID;
    }
    
    public void serialize(Output output) {
        output.int32(type.value);
        output.uint32(ID);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SlotID)) return false;
        SlotID d = (SlotID)o;
        return (type.equals(d.type) && ID == d.ID);
    }
}
