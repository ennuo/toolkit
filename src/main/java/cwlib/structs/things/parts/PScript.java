package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;

public class PScript implements Serializable {
    public ResourceReference script;
    
    
    public PScript serialize(Serializer serializer, Serializable structure) {
        PScript script = (structure == null) ? new PScript() : (PScript) structure;
        
        script.script = serializer.resource(script.script, ResourceType.SCRIPT);
        
        return script;
    }
    
}
