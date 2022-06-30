package cwlib.structs.things.components.decals;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

/**
 * Squished paint positional data.
 */
public class PaintControlPoint implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;
    
    public byte x, y, startRadius, endRadius;

    @SuppressWarnings("unchecked")
    @Override public PaintControlPoint serialize(Serializer serializer, Serializable structure) {
        PaintControlPoint point = 
            (structure == null) ? new PaintControlPoint() : (PaintControlPoint) structure;

        point.x = serializer.i8(point.x);
        point.y = serializer.i8(point.y);
        point.startRadius = serializer.i8(point.startRadius);
        point.endRadius = serializer.i8(point.endRadius);

        return point;
    }

    @Override public int getAllocatedSize() { return PaintControlPoint.BASE_ALLOCATION_SIZE; }
}