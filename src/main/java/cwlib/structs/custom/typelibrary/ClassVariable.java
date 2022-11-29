package cwlib.structs.custom.typelibrary;

import java.util.HashMap;
import java.util.Set;

import cwlib.enums.ScriptVariableType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

public class ClassVariable implements ScriptVariable {
    public static final int BASE_ALLOCATION_SIZE = 
        (TypeReference.BASE_ALLOCATION_SIZE * 2) + 0x10;
    
    public TypeReference classType;
    public short modifiers;
    public TypeReference superClassType;
    public HashMap<String, ScriptVariable> members = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override public ClassVariable serialize(Serializer serializer, Serializable structure) {
        ClassVariable variable = (structure == null) ? new ClassVariable() : (ClassVariable) structure;

        variable.modifiers = serializer.i16(variable.modifiers);
        variable.classType = serializer.reference(variable.classType, TypeReference.class);
        variable.superClassType = serializer.reference(variable.superClassType, TypeReference.class);
        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            Set<String> keys = variable.members.keySet();
            stream.i32(keys.size());
            for (String key : keys) {
                stream.str(key);
                ScriptVariable member = variable.members.get(key);
                stream.enum8(member.getVariableType());
                serializer.struct(member, (Class<Serializable>) member.getVariableType().getSerializable());
            }
        } else {
            MemoryInputStream stream = serializer.getInput();
            int count = stream.i32();
            variable.members = new HashMap<>(count);
            for (int i = 0; i < count; ++i) {
                String key = stream.str();
                ScriptVariableType type = ScriptVariableType.fromValue(stream.i8());
                variable.members.put(
                    key,
                    (ScriptVariable) serializer.struct(null, type.getSerializable())
                );
            }
        }

        return variable;
    }

    @Override public int getAllocatedSize() {
        int size = ClassVariable.BASE_ALLOCATION_SIZE;
        if (this.classType != null) size += this.classType.getAllocatedSize();
        if (this.superClassType != null) size += this.superClassType.getAllocatedSize();
        if (this.members != null) {
            for (String key : this.members.keySet())
                size += this.members.get(key).getAllocatedSize() + (key.length());
        }
        return size;
    }

    @Override public ScriptVariableType getVariableType() { 
        return ScriptVariableType.CLASS; 
    }
}