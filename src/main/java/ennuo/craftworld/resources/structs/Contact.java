package ennuo.craftworld.resources.structs;

import ennuo.craftworld.resources.things.parts.PShape;
import ennuo.craftworld.serializer.Serializer;

public class Contact {
    public PShape shape;
    public int flags;
    
    public Contact(Serializer serializer) {
        shape = (PShape) serializer.deserializePart("Shape");
        flags = serializer.input.i32();  
    }
}
