package cwlib.structs.profile;

import java.util.ArrayList;
import java.util.Iterator;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class StringLookupTable implements Serializable, Iterable<SortString> {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    private boolean unsorted;
    private boolean sortEnabled = true;
    private int[] rawIndexToSortedIndex;
    private ArrayList<SortString> stringList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public StringLookupTable serialize(Serializer serializer, Serializable structure) {
        StringLookupTable table = 
            (structure == null) ? new StringLookupTable() : (StringLookupTable) structure;
        
        // Let's make sure the indices are sorted.
        if (serializer.isWriting()) {
            table.rawIndexToSortedIndex = new int[table.stringList.size()];
            table.stringList.sort((l, r) -> l.string.compareTo(r.string));
            for (int i = 0; i < table.stringList.size(); ++i)
                table.rawIndexToSortedIndex[table.stringList.get(i).index] = i;
        }

        table.unsorted = serializer.bool(table.unsorted);
        table.sortEnabled = serializer.bool(table.sortEnabled);
        table.rawIndexToSortedIndex = serializer.intvector(table.rawIndexToSortedIndex);
        table.stringList = serializer.arraylist(table.stringList, SortString.class);
        
        return table;
    }

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        if (this.stringList != null)
            for (int i = 0; i < stringList.size(); ++i)
                size += stringList.get(i).getAllocatedSize();
        if (this.rawIndexToSortedIndex != null)
            size += (this.rawIndexToSortedIndex.length * 0x4);
        return size;
    }

    @Override public Iterator<SortString> iterator() { return this.stringList.iterator(); }


    /**
     * Gets a string by its index.
     * @param index Index of string
     * @return Translated string
     */
    public String get(int index) {
        for (SortString string : this.stringList) {
            if (string.index == index)
                return string.string;
        }
        return null;
    }

    /**
     * Finds index of key in string list.
     * @param key Key to search for
     * @return Index of key, -1 if not found
     */
    public int find(String key) {
        for (int i = 0; i < this.stringList.size(); ++i) {
            SortString string = this.stringList.get(i);
            if (string.string.equals(key))
                return string.index;
        }
        return -1;
    }

    /**
     * Finds index of key in string list.
     * @param key Key to search for
     * @return Index of key, -1 if not found
     */
    public int find(int key) {
        for (int i = 0; i < this.stringList.size(); ++i) {
            SortString string = this.stringList.get(i);
            if (string.lamsKeyID == key)
                return string.index;
        }
        return -1;
    }

    /**
     * Adds a string to the table.
     * @param string String to add
     * @param key LAMS Key ID of string
     * @return Index of added string
     */
    public int add(String string, int key) {
        int index = this.find(key);
        if (index != -1)
            return index;
        index = this.find(string);
        if (index != -1)
            return index;
        
        index = this.stringList.size();
        this.stringList.add(new SortString(key, string, index));
        return index;
    }

    /**
     * Clears the string table.
     */
    public void clear() {
        this.stringList.clear();
        this.rawIndexToSortedIndex = new int[0];
    }
}
