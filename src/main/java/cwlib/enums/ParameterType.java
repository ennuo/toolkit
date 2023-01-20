package cwlib.enums;

import cwlib.io.ValueEnum;

public enum ParameterType implements ValueEnum<Byte> {
    INVALID(-1),
    TEXTURE0(0x0),
    TEXTURE1(0x1),
    TEXTURE2(0x2),
    TEXTURE3(0x3),
    TEXTURE4(0x4),
    TEXTURE5(0x5),
    TEXTURE6(0x6),
    TEXTURE7(0x7),
    ALPHA_TEST_LEVEL(0x8),
    BUMP_LEVEL(0x9),
    COSINE_POWER(0xa),
    SPECULAR_COLOR(0xb);
    
    private final byte value;
    private ParameterType(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() { return this.value; }

    public static ParameterType fromValue(byte value) {
        for (ParameterType type : ParameterType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
