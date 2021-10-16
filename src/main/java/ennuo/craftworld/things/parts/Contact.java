package ennuo.craftworld.things.parts;

import ennuo.craftworld.things.Serializer;

public class Contact {
    public PShape shape;
    public int flags;
    
    public Contact(Serializer serializer) {
        shape = (PShape) serializer.deserializePart("Shape");
        flags = serializer.input.i32();  
    }
}
