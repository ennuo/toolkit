package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PScript implements Serializable {

    public PScript serialize(Serializer serializer, Serializable structure) {
        PScript script = (structure == null) ? new PScript() : (PScript) structure;
        
        return script;
    }
    
}
