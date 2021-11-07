package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PSwitch implements Serializable {

    public PSwitch serialize(Serializer serializer, Serializable structure) {
        PSwitch switchBase = (structure == null) ? new PSwitch() : (PSwitch) structure;
        
        return switchBase;
    }
    
}
