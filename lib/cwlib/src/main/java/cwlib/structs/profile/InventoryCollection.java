package cwlib.structs.profile;

import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

public class InventoryCollection implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int inventoryCollectionIndex;
    public InventoryPage[] inventoryPageInstances;
    public int currentPageNumber;

    @GsonRevision(lbp3 = true, min = Revisions.COLLECTION_POPPET_POWERUP)
    public boolean poppetPowerupSelection;

    @Override
    public void serialize(Serializer serializer)
    {
        inventoryCollectionIndex = serializer.i32(inventoryCollectionIndex);
        inventoryPageInstances = serializer.array(inventoryPageInstances,
            InventoryPage.class, true);
        currentPageNumber = serializer.i32(currentPageNumber);

        if (serializer.getRevision().getSubVersion() >= Revisions.COLLECTION_POPPET_POWERUP)
            poppetPowerupSelection = serializer.bool(poppetPowerupSelection);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = InventoryCollection.BASE_ALLOCATION_SIZE;
        if (this.inventoryPageInstances != null)
            size += (this.inventoryPageInstances.length * InventoryPage.BASE_ALLOCATION_SIZE);
        return size;
    }
}
