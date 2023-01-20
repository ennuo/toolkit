package cwlib.enums;

import cwlib.io.ValueEnum;

public enum ParameterSubType implements ValueEnum<Byte> {
    NONE(0x0),
    XY(0x1),
    ZW(0x2),
    Z(0x3);
    
    private final byte value;
    private ParameterSubType(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() { return this.value; }

    public static ParameterSubType fromValue(byte value) {
        for (ParameterSubType type : ParameterSubType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
