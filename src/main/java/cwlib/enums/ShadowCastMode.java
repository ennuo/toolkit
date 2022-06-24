package cwlib.enums;

import cwlib.io.ValueEnum;

public enum ShadowCastMode implements ValueEnum<Byte> {
    OFF(0x0),
    ON(0x1),
    ALPHA(0x2);

    private final byte value;
    private ShadowCastMode(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }
    public static ShadowCastMode fromValue(byte value) {
        for (ShadowCastMode mode : ShadowCastMode.values()) {
            if (mode.value == value) 
                return mode;
        }
        return null;
    }
}
