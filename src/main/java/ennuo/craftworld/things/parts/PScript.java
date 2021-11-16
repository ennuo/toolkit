package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;

public class PScript implements Serializable {
    public ResourceDescriptor script;
    
    
    public PScript serialize(Serializer serializer, Serializable structure) {
        PScript script = (structure == null) ? new PScript() : (PScript) structure;
        
        script.script = serializer.resource(script.script, ResourceType.SCRIPT);
        
        return script;
    }
    
}
