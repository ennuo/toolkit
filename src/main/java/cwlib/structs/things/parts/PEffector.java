package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PEffector implements Serializable {

    public PEffector serialize(Serializer serializer, Serializable structure) {
        PEffector effector = (structure == null) ? new PEffector() : (PEffector) structure;
        
        return effector;
    }
    
}
