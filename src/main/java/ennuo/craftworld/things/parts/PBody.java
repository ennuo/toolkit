package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PBody implements Serializable {

    public PBody serialize(Serializer serializer, Serializable structure) {
        PBody body = (structure == null) ? new PBody() : (PBody) structure;
        
        return body;
    }
    
}
