package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class SortString implements Serializable {
    public static int MAX_SIZE = 0x200;
    
    public long key;
    public String string;
    public int index;
    
    public SortString serialize(Serializer serializer, Serializable structure) {
        SortString entry = (structure == null) ? new SortString() : (SortString) structure;
        
        entry.key = serializer.u32(entry.key);
        entry.string = serializer.str16(entry.string);
        entry.index = serializer.i32(entry.index);
        
        return entry;
    }
    
    public SortString() {}
    
    public SortString(Data data) {
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
