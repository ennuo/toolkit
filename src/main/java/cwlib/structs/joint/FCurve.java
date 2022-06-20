package cwlib.structs.joint;

import org.joml.Vector4f;

import cwlib.enums.CurveType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class FCurve implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public Vector4f x = new Vector4f().zero();
    public Vector4f y = new Vector4f().zero();
    public Vector4f p = new Vector4f().zero();
    public CurveType type = CurveType.CONSTANT;

    @SuppressWarnings("unchecked")
    @Override public FCurve serialize(Serializer serializer, Serializable structure) {
        FCurve curve = (structure == null) ? new FCurve() : (FCurve) structure;

        curve.x = serializer.v4(curve.x);
        curve.y = serializer.v4(curve.y);
        curve.p = serializer.v4(curve.p);
        curve.type = serializer.enum32(curve.type);

        return curve;
    }

    @Override public int getAllocatedSize() { return FCurve.BASE_ALLOCATION_SIZE; }
}
