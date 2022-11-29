package cwlib.structs.custom.typelibrary;

import cwlib.enums.ScriptVariableType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class FieldVariable implements ScriptVariable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public short modifiers;
    public TypeReference type;

    @SuppressWarnings("unchecked")
    @Override public FieldVariable serialize(Serializer serializer, Serializable structure) {
        FieldVariable variable = (structure == null) ? new FieldVariable() : (FieldVariable) structure;

        variable.modifiers = serializer.i16(variable.modifiers);
        variable.type = serializer.reference(variable.type, TypeReference.class);

        return variable;
    }

    @Override public int getAllocatedSize() {
        int size = FieldVariable.BASE_ALLOCATION_SIZE;
        size += this.type.getAllocatedSize();
        return size;
    }

    @Override public ScriptVariableType getVariableType() { 
        return ScriptVariableType.FIELD; 
    }
}