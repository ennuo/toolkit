package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * The underlying native "machine" types used
 * by the Fish scripting environment.
 */
public enum MachineType implements ValueEnum<Integer> {
    VOID(0x0),
    BOOL(0x1),
    CHAR(0x2),
    S32(0x3),
    F32(0x4),
    V4(0x5),
    M44(0x6),

    /**
     * Strings used to be built-in types in the VM,
     * but were later replaced with a native class.
     * StringA/StringW
     */
    @Deprecated STRING(0x7),

    /**
     * Raw pointers will generally be used
     * when accessing native part data from
     * scripts.
     */
    RAW_PTR(0x8),
    REF_PTR(0x9),
    
    /**
     * Often time, scripts that inherit from
     * Thing's will be of type safeptr.
     */
    SAFE_PTR(0xa),

    /**
     * Any reference to an object in the scripting engine
     * that's not a safeptr, will generally be an object reference.
     */
    OBJECT_REF(0xb),

    S64(0xc),
    F64(0xd);

    private final int value;
    private MachineType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    public static MachineType fromValue(int value) {
        for (MachineType type : MachineType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
