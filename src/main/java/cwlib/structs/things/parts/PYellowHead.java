package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PYellowHead implements Serializable {

    public PYellowHead serialize(Serializer serializer, Serializable structure) {
        PYellowHead yellowHead = (structure == null) ? new PYellowHead() : (PYellowHead) structure;
        
        return yellowHead;
    }
    
}
