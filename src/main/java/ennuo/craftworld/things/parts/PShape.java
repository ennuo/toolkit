package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PShape implements Serializable {

    public PShape serialize(Serializer serializer, Serializable structure) {
        PShape shape = (structure == null) ? new PShape() : (PShape) structure;
        
        return shape;
    }
    
}
