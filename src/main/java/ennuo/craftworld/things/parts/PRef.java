package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PRef implements Serializable {

    public PRef serialize(Serializer serializer, Serializable structure) {
        PRef ref = (structure == null) ? new PRef() : (PRef) structure;
        
        return ref;
    }
    
}
