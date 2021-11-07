package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PAudioWorld implements Serializable {

    public PAudioWorld serialize(Serializer serializer, Serializable structure) {
        PAudioWorld audioWorld = (structure == null) ? new PAudioWorld() : (PAudioWorld) structure;
        
        return audioWorld;
    }
    
}
