package cwlib.structs.profile;

import cwlib.enums.InventorySortMode;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class InventoryView implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int type;
    public int subType;
    public String title;
    public boolean heartedOnly;
    public int customID;
    public InventorySortMode currentSortMode = InventorySortMode.INVALID;
    public InventorySortMode desiredSortMode = InventorySortMode.INVALID;

    @Override
    public void serialize(Serializer serializer)
    {
        type = serializer.i32(type);
        subType = serializer.i32(subType);
        title = serializer.wstr(title);
        heartedOnly = serializer.bool(heartedOnly);
        customID = serializer.i32(customID);
        currentSortMode = serializer.enum32(currentSortMode);
        desiredSortMode = serializer.enum32(desiredSortMode);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = InventoryView.BASE_ALLOCATION_SIZE;
        if (this.title != null)
            size += (this.title.length() * 2);
        return size;
    }
}
