package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PCameraTweak implements Serializable {

    public PCameraTweak serialize(Serializer serializer, Serializable structure) {
        PCameraTweak cameraTweak = (structure == null) ? new PCameraTweak() : (PCameraTweak) structure;
        
        return cameraTweak;
    }
    
}
