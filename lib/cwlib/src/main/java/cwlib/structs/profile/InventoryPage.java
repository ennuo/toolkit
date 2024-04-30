package cwlib.structs.profile;

import cwlib.enums.InventorySortMode;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class InventoryPage implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int inventoryPageTitleKey;
    public InventorySortMode desiredSortMode = InventorySortMode.INVALID;

    @Override
    public void serialize(Serializer serializer)
    {
        inventoryPageTitleKey = serializer.i32(inventoryPageTitleKey);
        desiredSortMode = serializer.enum32(desiredSortMode);
    }

    @Override
    public int getAllocatedSize()
    {
        return InventoryPage.BASE_ALLOCATION_SIZE;
    }
}
