package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.RType;

public class Collectable {
    public static int MAX_SIZE = 0x15;
    
    
    public ResourcePtr item = new ResourcePtr();
    public int count = 0;
    
    public Collectable() {}
    public Collectable(Data data) {
        item = data.resource(RType.PLAN, true);
        count = data.i32();
    }
    
    public void serialize(Output output) {
        output.resource(item, true);
        output.i32(count);
    }
    
}
