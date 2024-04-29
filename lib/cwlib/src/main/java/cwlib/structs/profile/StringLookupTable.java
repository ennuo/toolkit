package cwlib.structs.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class StringLookupTable implements Serializable, Iterable<SortString>
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    private boolean unsorted;
    private boolean sortEnabled = true;
    private int[] rawIndexToSortedIndex;
    private ArrayList<SortString> stringList = new ArrayList<>();

    @Override
    public void serialize(Serializer serializer)
    {
        // Let's make sure the indices are sorted.
        if (serializer.isWriting())
        {
            sortEnabled = true;
            unsorted = false;
            rawIndexToSortedIndex = new int[stringList.size()];

            Collections.sort(stringList, (a, z) -> a.string.compareTo(z.string));
            for (int i = 0; i < stringList.size(); ++i)
                rawIndexToSortedIndex[stringList.get(i).index] = i;
        }

        unsorted = serializer.bool(unsorted);
        sortEnabled = serializer.bool(sortEnabled);
        rawIndexToSortedIndex = serializer.intvector(rawIndexToSortedIndex);
        stringList = serializer.arraylist(stringList, SortString.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.stringList != null)
            for (int i = 0; i < stringList.size(); ++i)
                size += stringList.get(i).getAllocatedSize();
        if (this.rawIndexToSortedIndex != null)
            size += (this.rawIndexToSortedIndex.length * 0x4);
        return size;
    }

    @Override
    public Iterator<SortString> iterator()
    {
        return this.stringList.iterator();
    }

    /**
     * Gets a string by its index.
     *
     * @param index Index of string
     * @return Translated string
     */
    public String get(int index)
    {
        for (SortString string : this.stringList)
        {
            if (string.index == index)
                return string.string;
        }
        return null;
    }

    /**
     * Finds index of key in string list.
     *
     * @param key Key to search for
     * @return Index of key, -1 if not found
     */
    public int find(String key)
    {
        for (int i = 0; i < this.stringList.size(); ++i)
        {
            SortString string = this.stringList.get(i);
            if (string.string.equals(key))
                return string.index;
        }
        return -1;
    }

    /**
     * Finds index of key in string list.
     *
     * @param key Key to search for
     * @return Index of key, -1 if not found
     */
    public int find(int key)
    {
        // LAMS keys shouldn't be 0, treat this as non-existing.
        if (key == 0) return -1;

        for (int i = 0; i < this.stringList.size(); ++i)
        {
            SortString string = this.stringList.get(i);
            if (string.lamsKeyID == key)
                return string.index;
        }
        return -1;
    }

    /**
     * Adds a string to the table.
     *
     * @param string String to add
     * @param key    LAMS Key ID of string
     * @return Index of added string
     */
    public int add(String string, int key)
    {
        int index = key != 0 ? this.find(key) : this.find(string);
        if (index != -1) return index;

        index = this.stringList.size();
        this.stringList.add(new SortString(key, string, index));
        return index;
    }

    /**
     * Clears the string table.
     */
    public void clear()
    {
        this.stringList.clear();
        this.rawIndexToSortedIndex = new int[0];
    }
}
