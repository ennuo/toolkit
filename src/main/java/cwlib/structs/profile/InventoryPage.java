package cwlib.structs.profile;

import cwlib.enums.InventorySortMode;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class InventoryPage implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public int inventoryPageTitleKey;
    public InventorySortMode desiredSortMode = InventorySortMode.INVALID;

    @SuppressWarnings("unchecked")
    @Override public InventoryPage serialize(Serializer serializer, Serializable structure) {
        InventoryPage page = (structure == null) ? new InventoryPage() : (InventoryPage) structure;

        page.inventoryPageTitleKey = serializer.i32(page.inventoryPageTitleKey);
        page.desiredSortMode = serializer.enum32(page.desiredSortMode);

        return page;
    }

    @Override public int getAllocatedSize() { return InventoryPage.BASE_ALLOCATION_SIZE; }
}
