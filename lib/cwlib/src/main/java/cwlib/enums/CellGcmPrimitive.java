package cwlib.enums;

import cwlib.io.ValueEnum;

public enum CellGcmPrimitive implements ValueEnum<Byte>
{
    POINTS(1),
    LINES(2),
    LINE_LOOP(3),
    LINE_STRIP(4),
    TRIANGLES(5),
    TRIANGLE_STRIP(6),
    TRIANGLE_FAN(7),
    QUADS(8),
    QUAD_STRIP(9),
    POLYGON(10);

    private final byte value;

    CellGcmPrimitive(int value)
    {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue()
    {
        return this.value;
    }

    public static CellGcmPrimitive fromValue(int value)
    {
        for (CellGcmPrimitive type : CellGcmPrimitive.values())
        {
            if (type.value == value)
                return type;
        }
        return null;
    }
}
