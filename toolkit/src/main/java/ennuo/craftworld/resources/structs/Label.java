package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;

public class Label {
    public static int MAX_SIZE = 0x6;
    
    
    public long key;
    public int category;
    public String translated;
    
    public Label(Data data) {
        key = data.uint32();
        category = data.int8();
    }
    
    public void serialize(Output output) {
        output.int32((int) key);
        output.int8(category);
    }
}
