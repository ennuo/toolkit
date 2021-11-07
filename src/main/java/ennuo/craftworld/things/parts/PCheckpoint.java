package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PCheckpoint implements Serializable {

    public PCheckpoint serialize(Serializer serializer, Serializable structure) {
        PCheckpoint checkpoint = (structure == null) ? new PCheckpoint() : (PCheckpoint) structure;
        
        return checkpoint;
    }
    
}
