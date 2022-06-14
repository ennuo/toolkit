package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PCameraTweak implements Serializable {

    public PCameraTweak serialize(Serializer serializer, Serializable structure) {
        PCameraTweak cameraTweak = (structure == null) ? new PCameraTweak() : (PCameraTweak) structure;
        
        return cameraTweak;
    }
    
}
