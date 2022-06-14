package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PCostume implements Serializable {

    public PCostume serialize(Serializer serializer, Serializable structure) {
        PCostume costume = (structure == null) ? new PCostume() : (PCostume) structure;
        
        return costume;
    }
    
}
