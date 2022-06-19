package cwlib.enums;

import cwlib.io.ValueEnum;

public enum LethalType implements ValueEnum<Integer> {
    NOT(0),
    FIRE(1),
    ELECTRIC(2),
    @Deprecated ICE(3),
    CRUSH(4),
    SPIKE(5),
    GAS(6),
    GAS2(7),
    GAS3(8),
    GAS4(9),
    GAS5(10),
    GAS6(11),
    NO_STAND(12),
    BULLET(13),
    DROWNED(14);

    private final int value;
    private LethalType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    public static LethalType fromValue(int value) {
        for (LethalType type : LethalType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
