package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class StringEntry implements Serializable {
    public static int MAX_SIZE = 0x200;
    
    public long key;
    public String string;
    public int index;
    
    public StringEntry serialize(Serializer serializer, Serializable structure) {
        StringEntry entry = (structure == null) ? new StringEntry() : (StringEntry) structure;
        
        entry.key = serializer.u32(entry.key);
        entry.string = serializer.str16(entry.string);
        entry.index = serializer.i32(entry.index);
        
        return entry;
    }
    
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
