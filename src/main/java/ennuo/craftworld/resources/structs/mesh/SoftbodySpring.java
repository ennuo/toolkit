package ennuo.craftworld.resources.structs.mesh;

import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class SoftbodySpring implements Serializable {
    public short A, B;
    public float restLengthSq;

    public SoftbodySpring serialize(Serializer serializer, Serializable structure) {
        SoftbodySpring softbody = (structure == null) ? new SoftbodySpring() : (SoftbodySpring) structure;
        
        softbody.A = serializer.i16(softbody.A);
        softbody.B = serializer.i16(softbody.B);
        softbody.restLengthSq = serializer.f32(softbody.restLengthSq);
        
        return softbody;
    }
}
