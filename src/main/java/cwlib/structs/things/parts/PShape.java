package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PShape implements Serializable {

    public PShape serialize(Serializer serializer, Serializable structure) {
        PShape shape = (structure == null) ? new PShape() : (PShape) structure;
        
        return shape;
    }
    
}
