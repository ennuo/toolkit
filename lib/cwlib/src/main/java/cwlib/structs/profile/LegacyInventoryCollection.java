package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class LegacyInventoryCollection implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public InventoryView[] inventoryViews;
    public int currentPageNumber;
    public int collectionID;
    public int actionOnItemSelect;

    @Override
    public void serialize(Serializer serializer)
    {
        inventoryViews =
            serializer.array(inventoryViews, InventoryView.class, true);

        currentPageNumber = serializer.i32(currentPageNumber);
        collectionID = serializer.i32(collectionID);
        actionOnItemSelect = serializer.i32(actionOnItemSelect);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = LegacyInventoryCollection.BASE_ALLOCATION_SIZE;
        if (this.inventoryViews != null)
            for (InventoryView view : this.inventoryViews)
                size += view.getAllocatedSize();
        return size;
    }
}
