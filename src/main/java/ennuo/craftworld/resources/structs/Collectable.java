package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class Collectable implements Serializable {
    public static int MAX_SIZE = 0x15;
    
    public ResourceDescriptor item;
    public int count;

    @Override
    public Collectable serialize(Serializer serializer, Serializable structure) {
        Collectable collectable = (structure == null) ? new Collectable() : (Collectable) structure;
        
        collectable.item = serializer.resource(collectable.item, ResourceType.PLAN, true);
        collectable.count = serializer.i32(collectable.count);
        
        return collectable;
    }
    
}
