package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import java.util.Random;

public class StringEntry {
    public static int MAX_SIZE = 0x200;
    
    public long key = 0;
    public String string = "";
    public int index = 0;
    
    public StringEntry() {}
    
    public StringEntry(Data data) {
        key = data.uint32();
        string = data.str16();
        index = data.int32();
    }
    
    public void serialize(Output output) {
        output.uint32(key);
        output.str16(string);
        output.int32(index);
    }
}
