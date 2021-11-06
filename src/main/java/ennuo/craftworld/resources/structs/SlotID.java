package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.resources.enums.SlotType;

public class SlotID {
    public static int MAX_SIZE = 0x10;
    
    public SlotType type = SlotType.DEVELOPER;
    public long ID = 0;
    

    public SlotID() {}
    
    public SlotID(Data data) {
        type = SlotType.getValue(data.i32());
        ID = data.u32();
    }
    
    public SlotID(SlotType type, int ID) {
        this.type = type;
        this.ID = ID;
    }
    
    public void serialize(Output output) {
        output.i32(type.value);
        output.u32(ID);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SlotID)) return false;
        SlotID d = (SlotID)o;
        return (type.equals(d.type) && ID == d.ID);
    }
}
