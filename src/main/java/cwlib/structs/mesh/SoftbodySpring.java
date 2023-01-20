package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class SoftbodySpring implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public short A, B;
    public float restLengthSq;

    public SoftbodySpring() {};
    public SoftbodySpring(int a, int b, float restLengthSq) {
        this.A = (short) a;
        this.B = (short) b;
        this.restLengthSq = restLengthSq;
    }

    @SuppressWarnings("unchecked")
    @Override public SoftbodySpring serialize(Serializer serializer, Serializable structure) {
        SoftbodySpring spring = 
            (structure == null) ? new SoftbodySpring() : (SoftbodySpring) structure;

        spring.A = serializer.i16(spring.A);
        spring.B = serializer.i16(spring.B);
        spring.restLengthSq = serializer.f32(spring.restLengthSq);

        return spring;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}
