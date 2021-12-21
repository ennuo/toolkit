package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PCreature implements Serializable {

    public PCreature serialize(Serializer serializer, Serializable structure) {
        PCreature creature = (structure == null) ? new PCreature() : (PCreature) structure;
        
        return creature;
    }
    
}
