package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PYellowHead implements Serializable {

    public PYellowHead serialize(Serializer serializer, Serializable structure) {
        PYellowHead yellowHead = (structure == null) ? new PYellowHead() : (PYellowHead) structure;
        
        return yellowHead;
    }
    
}
