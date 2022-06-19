package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * Type of f-curve
 */
public enum CurveType implements ValueEnum<Integer> {
    CONSTANT(0),
    LINEAR(1),
    QUADRATIC(2),
    CUBIC(3),
    WAVE(4),
    BOX(5),
    SAW(6),
    MAX(7);

    private final int value;
    private CurveType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    /**
     * Attempts to get a f-curve type from value.
     * @param value curve type value
     * @return f-curve type
     */
    public static CurveType fromValue(int value) {
        for (CurveType type : CurveType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
