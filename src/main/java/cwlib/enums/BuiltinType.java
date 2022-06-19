package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * Virtual types used in the Fish scripting environments.
 */
public enum BuiltinType implements ValueEnum<Integer> {
    /**
     * If a type doesn't exist, or if it's associated
     * machine type is a reference, this type will be used.
     */
    VOID(0x0),
    
    BOOL(0x1),
    CHAR(0x2),
    S32(0x3),
    F32(0x4),

    /* All the vector types are still vector4's natively */

    V2(0x5),
    V3(0x6),
    V4(0x7),

    M44(0x8),

    /**
     * Functionally the same as a s32.
     */
    GUID(0x9),

    S64(0xa),
    F64(0xb);

    private final int value;
    private BuiltinType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    public static BuiltinType fromValue(int value) {
        for (BuiltinType type : BuiltinType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
