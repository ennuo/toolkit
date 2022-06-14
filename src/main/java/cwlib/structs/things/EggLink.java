package cwlib.structs.things;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;

public class EggLink implements Serializable {
    public ResourceReference plan;
    public boolean shareable = true;
    
    public EggLink serialize(Serializer serializer, Serializable structure) {
        EggLink eggLink = (structure == null) ? new EggLink() : (EggLink) structure;
        
        eggLink.plan = serializer.resource(eggLink.plan, ResourceType.PLAN);
        eggLink.shareable = serializer.bool(eggLink.shareable);
        
        return eggLink;
    }
    
}
