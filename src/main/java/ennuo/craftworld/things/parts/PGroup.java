package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PGroup implements Serializable {

    public PGroup serialize(Serializer serializer, Serializable structure) {
        PGroup group = (structure == null) ? new PGroup() : (PGroup) structure;
        
        return group;
    }
    
}
