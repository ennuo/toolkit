package cwlib.structs.custom.typelibrary;

import java.util.HashMap;
import java.util.Set;

import cwlib.enums.ScriptVariableType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;

public class EnumVariable implements ScriptVariable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public String name;
    public HashMap<String, Integer> members = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override public EnumVariable serialize(Serializer serializer, Serializable structure) {
        EnumVariable variable = (structure == null) ? new EnumVariable() : (EnumVariable) structure;

        variable.name = serializer.str(variable.name);
        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            Set<String> keys = variable.members.keySet();
            stream.i32(keys.size());
            for (String key : keys) {
                stream.str(key);
                stream.i32(variable.members.get(key));
            }
        } else {
            MemoryInputStream stream = serializer.getInput();
            int count = stream.i32();
            variable.members = new HashMap<>(count);
            for (int i = 0; i < count; ++i) {
                variable.members.put(
                    stream.str(),
                    stream.i32()
                );
            }
        }

        return variable;
    }

    @Override public int getAllocatedSize() {
        int size = EnumVariable.BASE_ALLOCATION_SIZE;
        if (this.name != null)
            size += this.name.length();
        if (this.members != null) {
            for (String key : this.members.keySet())
                size += 0x8 + (key.length());
        }
        return size;
    }

    @Override public ScriptVariableType getVariableType() { 
        return ScriptVariableType.ENUM; 
    }
}