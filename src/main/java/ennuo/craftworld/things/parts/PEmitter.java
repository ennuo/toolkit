package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PEmitter implements Serializable {

    public PEmitter serialize(Serializer serializer, Serializable structure) {
        PEmitter emitter = (structure == null) ? new PEmitter() : (PEmitter) structure;
        
        return emitter;
    }
    
}
