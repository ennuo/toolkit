package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.v2.Serializable;
import ennuo.craftworld.serializer.v2.Serializer;

public class SoftbodyVertEquivalence implements Serializable {
    public short first, count;

    public SoftbodyVertEquivalence serialize(Serializer serializer, Serializable structure) {
        SoftbodyVertEquivalence softbody = 
                (structure == null) ? new SoftbodyVertEquivalence() : (SoftbodyVertEquivalence) structure;
        
        softbody.first = serializer.i16(softbody.first);
        softbody.count = serializer.i16(softbody.count);
        
        return softbody;
    }
}
