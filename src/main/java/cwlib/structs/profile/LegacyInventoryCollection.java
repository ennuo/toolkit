package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class LegacyInventoryCollection implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public InventoryView[] inventoryViews;
    public int currentPageNumber;
    public int collectionID;
    public int actionOnItemSelect;

    @SuppressWarnings("unchecked")
    @Override public LegacyInventoryCollection serialize(Serializer serializer, Serializable structure) {
        LegacyInventoryCollection collection = (structure == null) ? new LegacyInventoryCollection() : (LegacyInventoryCollection) structure;

        collection.inventoryViews = 
            serializer.array(collection.inventoryViews, InventoryView.class, true);

        collection.currentPageNumber = serializer.i32(collection.currentPageNumber);
        collection.collectionID = serializer.i32(collection.collectionID);
        collection.actionOnItemSelect = serializer.i32(collection.actionOnItemSelect);

        return collection;
    }

    @Override public int getAllocatedSize() {
        int size = LegacyInventoryCollection.BASE_ALLOCATION_SIZE;
        if (this.inventoryViews != null)
            for (InventoryView view : this.inventoryViews)
                size += view.getAllocatedSize();
        return size;
    }
}
