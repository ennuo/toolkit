package cwlib.structs.profile;

import cwlib.enums.InventorySortMode;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class InventoryView implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int type;
    public int subType;
    public String title;
    public boolean heartedOnly;
    public int customID;
    public InventorySortMode currentSortMode = InventorySortMode.INVALID;
    public InventorySortMode desiredSortMode = InventorySortMode.INVALID;

    @SuppressWarnings("unchecked")
    @Override public InventoryView serialize(Serializer serializer, Serializable structure) {
        InventoryView view = (structure == null) ? new InventoryView() : (InventoryView) structure;

        view.type = serializer.i32(view.type);
        view.subType = serializer.i32(view.subType);
        view.title = serializer.wstr(view.title);
        view.heartedOnly = serializer.bool(view.heartedOnly);
        view.customID = serializer.i32(view.customID);
        view.currentSortMode = serializer.enum32(view.currentSortMode);
        view.desiredSortMode = serializer.enum32(view.desiredSortMode);

        return view;
    }

    @Override public int getAllocatedSize() { 
        int size = InventoryView.BASE_ALLOCATION_SIZE;
        if (this.title != null)
            size += (this.title.length() * 2);
        return size;
    }
}
