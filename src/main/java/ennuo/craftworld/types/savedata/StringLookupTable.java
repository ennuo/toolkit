package ennuo.craftworld.types.savedata;

import ennuo.craftworld.resources.structs.SortString;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import java.util.ArrayList;

public class StringLookupTable implements Serializable {
    public boolean unsorted;
    public boolean sortEnabled = true;
    public int[] rawIndexToSortedIndex;
    public ArrayList<SortString> stringList = new ArrayList<>();
    
    public StringLookupTable serialize(Serializer serializer, Serializable structure) {
        StringLookupTable table = (structure == null) ? new StringLookupTable() : (StringLookupTable) structure;
        
        if (serializer.isWriting) {
            table.rawIndexToSortedIndex = new int[table.stringList.size()];
            table.stringList.sort((l, r) -> l.string.compareTo(r.string));
            for (int i = 0; i < table.stringList.size(); ++i)
                table.rawIndexToSortedIndex[table.stringList.get(i).index] = i;
        }
        
        table.unsorted = serializer.bool(table.unsorted);
        table.sortEnabled = serializer.bool(table.sortEnabled);
        table.rawIndexToSortedIndex = serializer.table(table.rawIndexToSortedIndex);
        table.stringList = serializer.arraylist(table.stringList, SortString.class);
        
        return table;
    }
    
    public String get(int index) {
        for (SortString string : this.stringList)
            if (string.index == index)
                return string.string;
        return null;
    }
    
    public int find(long key) {
        for (int i = 0; i < this.stringList.size(); ++i) {
            SortString string = this.stringList.get(i);
            if (string.key == key)
                return string.index;
        }
        return -1;
    }
    
    public int find(String key) {
        for (int i = 0; i < this.stringList.size(); ++i) {
            SortString string = this.stringList.get(i);
            if (string.string.equals(key))
                return string.index;
        }
        return -1;
    }
    
    public int add(String string, int key) {
        int index = this.find(string);
        if (index != -1)
            return index;
        index = this.stringList.size();
        SortString sortString = new SortString();
        sortString.index = index;
        sortString.key = key;
        sortString.string = string;
        this.stringList.add(sortString);
        return index;
    }
    
    public void clear() {
        this.stringList.clear();
        this.rawIndexToSortedIndex = new int[0];
    }
}
