package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PAnimation implements Serializable {

    public PAnimation serialize(Serializer serializer, Serializable structure) {
        PAnimation animation = (structure == null) ? new PAnimation() : (PAnimation) structure;
        
        return animation;
    }
    
}
