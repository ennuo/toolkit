package cwlib.structs.profile;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class InventoryCollection implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int inventoryCollectionIndex;
    public InventoryPage[] inventoryPageInstances;
    public int currentPageNumber;
    public boolean poppetPowerupSelection;

    @SuppressWarnings("unchecked")
    @Override public InventoryCollection serialize(Serializer serializer, Serializable structure) {
        InventoryCollection collection = (structure == null) ? new InventoryCollection() : (InventoryCollection) structure;

        collection.inventoryCollectionIndex = serializer.i32(collection.inventoryCollectionIndex);
        collection.inventoryPageInstances = serializer.array(collection.inventoryPageInstances, InventoryPage.class, true);
        collection.currentPageNumber = serializer.i32(collection.currentPageNumber);

        if (serializer.getRevision().isAfterLBP3Revision(0x8))
            collection.poppetPowerupSelection = serializer.bool(collection.poppetPowerupSelection);

        return collection;
    }

    @Override public int getAllocatedSize() {
        int size = InventoryCollection.BASE_ALLOCATION_SIZE;
        if (this.inventoryPageInstances != null)
            size += (this.inventoryPageInstances.length * InventoryPage.BASE_ALLOCATION_SIZE);
        return size;
    }
}
