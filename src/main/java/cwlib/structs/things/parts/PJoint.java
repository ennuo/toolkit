package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PJoint implements Serializable {

    public PJoint serialize(Serializer serializer, Serializable structure) {
        PJoint joint = (structure == null) ? new PJoint() : (PJoint) structure;
        
        return joint;
    }
    
}
