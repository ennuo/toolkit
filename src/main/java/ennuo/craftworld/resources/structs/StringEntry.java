package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import java.util.Random;

public class StringEntry {
    public static int MAX_SIZE = 0x200;
    
    public long key = 0;
    public String string = "";
    public int index = 0;
    
    public StringEntry() {}
    
    public StringEntry(Data data) {
        key = data.u32();
        string = data.str16();
        index = data.i32();
    }
    
    public void serialize(Output output) {
        output.u32(key);
        output.str16(string);
        output.i32(index);
    }
}
