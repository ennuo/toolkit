package ennuo.craftworld.types.savedata;

import ennuo.craftworld.resources.structs.StringEntry;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class StringTable implements Serializable {
    public boolean unsorted;
    public boolean sortEnabled;
    public int[] rawIndexToSortedIndex;
    StringEntry[] strings;
    
    public StringTable serialize(Serializer serializer, Serializable structure) {
        StringTable table = (structure == null) ? new StringTable() : (StringTable) structure;
        
        if (serializer.isWriting && table.strings != null && table.strings.length != 0) {
            ArrayList<StringEntry> strings = new ArrayList<>(Arrays.asList(table.strings));
            strings.sort((e1, e2) -> e1.string.compareTo(e2.string));
            table.rawIndexToSortedIndex = new int[table.strings.length];
            for (int i = 0; i < table.strings.length; ++i)
                table.rawIndexToSortedIndex[table.strings[i].index] = i;
            table.strings = strings.toArray(StringEntry[]::new);
        }
        
        table.unsorted = serializer.bool(table.unsorted);
        table.sortEnabled = serializer.bool(table.sortEnabled);
        table.rawIndexToSortedIndex = serializer.table(table.rawIndexToSortedIndex);
        table.strings = serializer.array(table.strings, StringEntry.class);
        
        return table;
    }
    
}
