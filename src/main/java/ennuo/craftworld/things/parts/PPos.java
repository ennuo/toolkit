package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PPos implements Serializable {

    public PPos serialize(Serializer serializer, Serializable structure) {
        PPos pos = (structure == null) ? new PPos() : (PPos) structure;
        
        return pos;
    }
    
}
