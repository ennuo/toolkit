package cwlib.structs.custom.typelibrary;

import cwlib.enums.ScriptVariableType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class FunctionVariable implements ScriptVariable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public FunctionOverload[] overloads;

    @SuppressWarnings("unchecked")
    @Override public FunctionVariable serialize(Serializer serializer, Serializable structure) {
        FunctionVariable variable = (structure == null) ? new FunctionVariable() : (FunctionVariable) structure;

        variable.overloads = serializer.array(variable.overloads, FunctionOverload.class);

        return variable;
    }
    

    @Override public int getAllocatedSize() {
        int size = FunctionVariable.BASE_ALLOCATION_SIZE;
        if (this.overloads != null) {
            for (FunctionOverload overload : this.overloads)
                size += overload.getAllocatedSize();
        }
        return size;
    }

    @Override public ScriptVariableType getVariableType() { 
        return ScriptVariableType.FUNCTION; 
    }
}
