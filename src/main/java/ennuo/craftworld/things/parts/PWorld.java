package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PWorld implements Serializable {

    public PWorld serialize(Serializer serializer, Serializable structure) {
        PWorld world = (structure == null) ? new PWorld() : (PWorld) structure;
        
        return world;
    }
    
}
