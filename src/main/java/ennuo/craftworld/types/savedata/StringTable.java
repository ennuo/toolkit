package ennuo.craftworld.types.savedata;

import ennuo.craftworld.resources.structs.StringEntry;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class StringTable implements Serializable {
    public boolean unsorted;
    public boolean sortEnabled;
    public int[] rawIndexToSortedIndex;
    StringEntry[] strings;
    
    public StringTable serialize(Serializer serializer, Serializable structure) {
        StringTable table = (structure == null) ? new StringTable() : (StringTable) structure;
        
        table.unsorted = serializer.bool(table.unsorted);
        table.sortEnabled = serializer.bool(table.sortEnabled);
        table.rawIndexToSortedIndex = serializer.table(table.rawIndexToSortedIndex);
        table.strings = serializer.array(table.strings, StringEntry.class);
        
        return table;
    }
    
}
