package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;

public class EggLink implements Serializable {
    public ResourceDescriptor plan;
    public boolean shareable = true;
    
    public EggLink serialize(Serializer serializer, Serializable structure) {
        EggLink eggLink = (structure == null) ? new EggLink() : (EggLink) structure;
        
        eggLink.plan = serializer.resource(eggLink.plan, ResourceType.PLAN);
        eggLink.shareable = serializer.bool(eggLink.shareable);
        
        return eggLink;
    }
    
}
