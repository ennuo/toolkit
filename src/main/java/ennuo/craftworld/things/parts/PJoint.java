package ennuo.craftworld.things.parts;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class PJoint implements Serializable {

    public PJoint serialize(Serializer serializer, Serializable structure) {
        PJoint joint = (structure == null) ? new PJoint() : (PJoint) structure;
        
        return joint;
    }
    
}
