package cwlib.structs.joint;

import cwlib.enums.CurveType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import org.joml.Vector4f;

public class FCurve implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public Vector4f x = new Vector4f().zero();
    public Vector4f y = new Vector4f().zero();
    public Vector4f p = new Vector4f().zero();
    public CurveType type = CurveType.CONSTANT;

    @Override
    public void serialize(Serializer serializer)
    {
        x = serializer.v4(x);
        y = serializer.v4(y);
        p = serializer.v4(p);
        type = serializer.enum32(type);
    }

    @Override
    public int getAllocatedSize()
    {
        return FCurve.BASE_ALLOCATION_SIZE;
    }
}
