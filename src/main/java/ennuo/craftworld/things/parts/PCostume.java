package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PCostume implements Serializable {

    public PCostume serialize(Serializer serializer, Serializable structure) {
        PCostume costume = (structure == null) ? new PCostume() : (PCostume) structure;
        
        return costume;
    }
    
}
