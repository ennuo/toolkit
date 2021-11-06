package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;

public class Label {
    public static int MAX_SIZE = 0x6;
    
    
    public long key;
    public int category;
    public String translated;
    
    public Label(Data data) {
        key = data.u32();
        category = data.i8();
    }
    
    public void serialize(Output output) {
        output.i32((int) key);
        output.u8(category);
    }
}
