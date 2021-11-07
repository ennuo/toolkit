package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PTrigger implements Serializable {

    public PTrigger serialize(Serializer serializer, Serializable structure) {
        PTrigger trigger = (structure == null) ? new PTrigger() : (PTrigger) structure;
        
        return trigger;
    }
    
}
