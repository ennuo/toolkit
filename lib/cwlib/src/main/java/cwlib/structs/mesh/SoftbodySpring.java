package cwlib.structs.mesh;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class SoftbodySpring implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public short A, B;
    public float restLengthSq;

    public SoftbodySpring() { }

    public SoftbodySpring(int a, int b, float restLengthSq)
    {
        this.A = (short) a;
        this.B = (short) b;
        this.restLengthSq = restLengthSq;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        A = serializer.i16(A);
        B = serializer.i16(B);
        restLengthSq = serializer.f32(restLengthSq);
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}
