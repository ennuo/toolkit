package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PWorld implements Serializable {

    public PWorld serialize(Serializer serializer, Serializable structure) {
        PWorld world = (structure == null) ? new PWorld() : (PWorld) structure;
        
        return world;
    }
    
}
