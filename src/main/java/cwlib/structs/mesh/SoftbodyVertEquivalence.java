package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

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
