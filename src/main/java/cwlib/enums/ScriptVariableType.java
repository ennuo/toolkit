package cwlib.enums;

import cwlib.io.Serializable;
import cwlib.io.ValueEnum;
import cwlib.structs.custom.typelibrary.*;

public enum ScriptVariableType implements ValueEnum<Byte> {
    CLASS(0, ClassVariable.class),
    FUNCTION(1, FunctionVariable.class),
    ENUM(2, EnumVariable.class),
    FIELD(3, FieldVariable.class),
    LOCAL(4, null),
    MACRO(5, null),
    NULL(6, null),
    PROPERTY(7, PropertyVariable.class);

    private final byte value;
    private final Class<? extends Serializable> serializable;

    private ScriptVariableType(int value, Class<? extends Serializable> serializable) {
        this.value = (byte) value;
        this.serializable = serializable;
    }

    public Class<? extends Serializable> getSerializable() { return this.serializable; }
    public Byte getValue() { return this.value; }
    public static ScriptVariableType fromValue(byte value) {
        for (ScriptVariableType type : ScriptVariableType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
