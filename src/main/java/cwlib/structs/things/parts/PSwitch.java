package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PSwitch implements Serializable {

    public PSwitch serialize(Serializer serializer, Serializable structure) {
        PSwitch switchBase = (structure == null) ? new PSwitch() : (PSwitch) structure;
        
        return switchBase;
    }
    
}
