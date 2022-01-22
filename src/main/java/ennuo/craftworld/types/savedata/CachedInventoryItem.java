package ennuo.craftworld.types.savedata;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;

public class CachedInventoryItem implements Serializable {
    public ResourceDescriptor plan;
    public long tempGUID;
    public InventoryDetails details;
    public byte flags;
    
    public CachedInventoryItem serialize(Serializer serializer, Serializable structure) {
        CachedInventoryItem item = (structure == null) ?
                new CachedInventoryItem() : (CachedInventoryItem) structure;
        
        item.plan = serializer.resource(item.plan, ResourceType.PLAN, true);
        if (serializer.revision.head > 0x010503ef)
            item.tempGUID = serializer.u32f(item.tempGUID);
        item.details = serializer.struct(item.details, InventoryDetails.class);
        
        // NOTE(Aidan): A lot of this data is mostly useless, so we'll
        // skip it!
        
        if (serializer.revision.isVita())
            serializer.pad(8);
        else serializer.pad(7);
        
        item.flags = serializer.i8(item.flags);
        
        if (serializer.revision.head > 0x33a)
            serializer.pad(4);
        else {
            serializer.pad(7);
            item.flags = serializer.i8(item.flags);
        }
        
        return item;
    }
    
    
}
