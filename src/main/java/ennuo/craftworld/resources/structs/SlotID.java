package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class SlotID implements Serializable {
    public static int MAX_SIZE = 0x10;
    
    public SlotType type = SlotType.DEVELOPER;
    public long ID = 0;
    
    public SlotID() {}
    
    public SlotID(SlotType type, long ID) {
        this.type = type;
        this.ID = ID;
    }
    
    public SlotID(Data data) {
        this.type = SlotType.getValue(data.i32());
        this.ID = data.u32();
    }
    
    public SlotID serialize(Serializer serializer, Serializable structure) {
        SlotID slot = (structure == null) ? new SlotID() : (SlotID) structure;
        
        slot.type = SlotType.getValue(serializer.i32(slot.type.value));
        slot.ID = serializer.u32(slot.ID);
        
        return slot;
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
    
    @Override
    public int hashCode() {
        int result = (int) (this.ID ^ (this.ID >>> 32));
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        return result;
    }
}
