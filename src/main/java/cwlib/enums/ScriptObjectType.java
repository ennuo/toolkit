package cwlib.enums;

import cwlib.io.ValueEnum;

public enum ScriptObjectType implements ValueEnum<Integer> {
    NULL(0),
    ARRAY_BOOL(1),
    ARRAY_CHAR(2),
    ARRAY_S32(3),
    ARRAY_F32(4),
    ARRAY_VECTOR4(5),
    ARRAY_M44(6),
    ARRAY_STRING(7),
    ARRAY_RAW_PTR(8),
    ARRAY_REF_PTR(9),
    ARRAY_SAFE_PTR(10),
    ARRAY_OBJECT_REF(11),
    RESOURCE(12),
    INSTANCE(13),
    STRINGW(14),
    AUDIOHANDLE(15),
    STRINGA(16),
    POPPET(17),
    EXPOSED_COLLECTBUBBLE(18),
    ARRAY_S64(19),
    ARRAY_F64(20);
    
    private final int value;
    private ScriptObjectType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    public static ScriptObjectType fromValue(int value) {
        for (ScriptObjectType type : ScriptObjectType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
