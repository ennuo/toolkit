package cwlib.structs.custom.typelibrary;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class FunctionOverload implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public String name;
    public short modifiers;
    public TypeReference[] argumentTypes;
    public TypeReference returnType;

    @SuppressWarnings("unchecked")
    @Override public FunctionOverload serialize(Serializer serializer, Serializable structure) {
        FunctionOverload function = (structure == null) ? new FunctionOverload() : (FunctionOverload) structure;

        function.name = serializer.str(function.name);
        function.modifiers = serializer.i16(function.modifiers);
        function.argumentTypes = serializer.array(function.argumentTypes, TypeReference.class, true);
        function.returnType = serializer.reference(function.returnType, TypeReference.class);

        return function;
    }

    @Override public int getAllocatedSize() {
        int size = FunctionOverload.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += this.name.length();
        if (this.argumentTypes != null) {
            for (TypeReference reference : this.argumentTypes)
                size += reference.getAllocatedSize();
        }
        if (this.returnType != null)
            size += this.returnType.getAllocatedSize();
        return size;
    }
}