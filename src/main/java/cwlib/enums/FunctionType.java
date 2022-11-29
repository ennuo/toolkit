package cwlib.enums;

import cwlib.io.ValueEnum;

public enum FunctionType implements ValueEnum<Byte> {
    NORMAL(0),
    GETTER(1),
    SETTER(2);

    private final byte value;
    private FunctionType(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() { return this.value; }
    public static FunctionType fromValue(int value) {
        for (FunctionType type : FunctionType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}