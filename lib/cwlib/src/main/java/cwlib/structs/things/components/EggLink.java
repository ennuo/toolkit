package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.types.data.ResourceDescriptor;

public class EggLink implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    @GsonRevision(max = 0x15f)
    @Deprecated
    public GlobalThingDescriptor item;

    @GsonRevision(min = 0x160, max = 0x196)
    @Deprecated
    public InventoryItemDetails details;

    @GsonRevision(min = 0x160)
    public ResourceDescriptor plan;
    @GsonRevision(min = 0x23c)
    public boolean shareable = true;
    @GsonRevision(min = 0x3e1)
    public ResourceDescriptor painting;

    public EggLink() { }

    public EggLink(ResourceDescriptor item)
    {
        this.plan = item;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int version = serializer.getRevision().getVersion();

        // The game technically removes this field at 0x197,
        // but it's essentially the same as link.plan at 0x160
        if (version < 0x160)
            item = serializer.struct(item, GlobalThingDescriptor.class);
        else
            plan = serializer.resource(plan, ResourceType.PLAN, false, false, false);

        if (version > 0x15f && version < 0x197)
            details = serializer.struct(details, InventoryItemDetails.class);

        if (version > 0x207 && version < 0x22a)
            serializer.bool(false); // Unknown value

        if (version > 0x23b)
            shareable = serializer.bool(shareable);

        boolean hasPainting = false;
        if (version > 0x3e0)
        {
            if (serializer.isWriting()) hasPainting = painting != null;
            hasPainting = serializer.bool(hasPainting);
        }
        if (hasPainting)
            painting = serializer.resource(painting, ResourceType.PAINTING, true);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = EggLink.BASE_ALLOCATION_SIZE;
        if (this.details != null) size += this.details.getAllocatedSize();
        return size;
    }
}
