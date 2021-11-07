package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PPhysicsTweak implements Serializable {

    public PPhysicsTweak serialize(Serializer serializer, Serializable structure) {
        PPhysicsTweak physicsTweak = (structure == null) ? new PPhysicsTweak() : (PPhysicsTweak) structure;
        
        return physicsTweak;
    }
    
}
