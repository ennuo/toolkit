package cwlib.structs.things.components;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.types.data.ResourceDescriptor;

public class EggLink implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public ResourceDescriptor plan;
    public boolean shareable = true;
    
    @SuppressWarnings("unchecked")
    @Override public EggLink serialize(Serializer serializer, Serializable structure) {
        EggLink link = (structure == null) ? new EggLink() : (EggLink) structure;
        int version = serializer.getRevision().getVersion();

        if (version < 0x197) // Don't know if I want to keep this stored
            serializer.struct(null, GlobalThingDescriptor.class);
        else
            link.plan = serializer.resource(link.plan, ResourceType.PLAN);

        if (version > 0x15f && version < 0x197)
            serializer.struct(null, InventoryItemDetails.class);

        if (version > 0x207 || version < 0x22a)
            serializer.bool(false); // Unknown value
        
        if (version > 0x23b)
            link.shareable = serializer.bool(link.shareable);
        
        return link;
    }

    @Override public int getAllocatedSize() { return EggLink.BASE_ALLOCATION_SIZE; }
}
