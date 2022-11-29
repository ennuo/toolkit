package cwlib.structs.custom.typelibrary;

import cwlib.enums.ScriptVariableType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class PropertyVariable implements ScriptVariable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public short modifiers;
    public String getter;
    public String setter;

    @SuppressWarnings("unchecked")
    @Override public PropertyVariable serialize(Serializer serializer, Serializable structure) {
        PropertyVariable variable = (structure == null) ? new PropertyVariable() : (PropertyVariable) structure;

        variable.modifiers = serializer.i16(modifiers);
        variable.getter = serializer.str(variable.getter);
        variable.setter = serializer.str(variable.setter);

        return variable;
    }

    @Override public int getAllocatedSize() {
        int size = PropertyVariable.BASE_ALLOCATION_SIZE;
        if (this.getter != null) size += this.getter.length();
        if (this.setter != null) size += this.setter.length();
        return size;
    }

    @Override public ScriptVariableType getVariableType() { 
        return ScriptVariableType.PROPERTY; 
    }
}
