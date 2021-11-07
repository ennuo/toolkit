package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PEffector implements Serializable {

    public PEffector serialize(Serializer serializer, Serializable structure) {
        PEffector effector = (structure == null) ? new PEffector() : (PEffector) structure;
        
        return effector;
    }
    
}
