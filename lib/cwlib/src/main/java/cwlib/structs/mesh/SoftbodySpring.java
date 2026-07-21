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
        if (a < b)
        {
            this.A = (short)a;
            this.B = (short)b;
        }
        else
        {
            this.A = (short)b;
            this.B = (short)a;
        }

        this.restLengthSq = restLengthSq;
    }

    @Override public int hashCode()
    {
        int result = (int) (this.A ^ (this.A >>> 32));
        result = 31 * result + B;
        return result;
    }

    @Override public boolean equals(Object other)
    {
        if (!(other instanceof SoftbodySpring spring)) return false;
        if (other == this) return true;
        return spring.A == A && spring.B == B;
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
