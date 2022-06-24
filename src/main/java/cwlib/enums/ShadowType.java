package cwlib.enums;

import cwlib.io.ValueEnum;

public enum ShadowType implements ValueEnum<Byte> {
    ALWAYS(0),
    NEVER(1),
    IF_ON_SCREEN(2);

    private final byte value;
    private ShadowType(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }
    public static ShadowType fromValue(byte value) {
        for (ShadowType mode : ShadowType.values()) {
            if (mode.value == value) 
                return mode;
        }
        return null;
    }
}
