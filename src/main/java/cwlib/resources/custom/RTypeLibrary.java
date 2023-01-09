package cwlib.resources.custom;

import java.util.HashMap;
import java.util.Set;

import cwlib.enums.ResourceType;
import cwlib.enums.ScriptVariableType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.custom.typelibrary.ClassVariable;
import cwlib.structs.custom.typelibrary.ScriptVariable;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;

public class RTypeLibrary implements Compressable, Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public String name;
    public HashMap<String, HashMap<String, ScriptVariable>> namespaces = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override public RTypeLibrary serialize(Serializer serializer, Serializable structure) {
        RTypeLibrary library = (structure == null) ? new RTypeLibrary() : (RTypeLibrary) structure;

        library.name = serializer.str(library.name);
        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            Set<String> keys = library.namespaces.keySet();
            stream.i32(keys.size());
            for (String key : keys) {
                stream.str(key);
                HashMap<String, ScriptVariable> namespace = library.namespaces.get(key);
                stream.i32(namespace.size());
                for (String member : namespace.keySet()) {
                    stream.str(member);
                    ScriptVariable variable = namespace.get(member);
                    serializer.enum8(variable.getVariableType());
                    variable.serialize(serializer, variable);
                }
            }
        } else {
            MemoryInputStream stream = serializer.getInput();

            int namespaceCount = stream.i32();
            library.namespaces = new HashMap<>(namespaceCount);

            for (int i = 0; i < namespaceCount; ++i) {
                String key = stream.str();

                HashMap<String, ScriptVariable> namespace = new HashMap<>();
                int variableCount = stream.i32();
                for (int j = 0; j < variableCount; ++j) {
                    String variableName = stream.str();
                    ScriptVariableType variableType = stream.enum8(ScriptVariableType.class);
                    namespace.put(variableName, serializer.struct(null, (Class<ScriptVariable>) variableType.getSerializable()));
                }

                library.namespaces.put(key, namespace);
            }
        }

        return library;
    }

    public String getNameByDescriptor(ResourceDescriptor descriptor) {
        HashMap<String, ScriptVariable> std = this.namespaces.get("std");
        for (ScriptVariable member : std.values()) {
            if (member.getVariableType() == ScriptVariableType.CLASS) {
                ClassVariable variable = (ClassVariable) member;
                if (variable.classType.script != null && variable.classType.script.equals(descriptor))
                    return variable.classType.name;
            }
        }
        return null;
    }

    public static final RTypeLibrary LBP1 = new Resource(FileIO.getResourceFile("/libraries/lbp1.lib")).loadResource(RTypeLibrary.class);
    public static final RTypeLibrary LBP3 = new Resource(FileIO.getResourceFile("/libraries/lbp3.lib")).loadResource(RTypeLibrary.class);

    @Override public int getAllocatedSize() {
        int size = RTypeLibrary.BASE_ALLOCATION_SIZE;
        if (this.namespaces != null) {
            for (String key : this.namespaces.keySet()) {
                size += (0x8 + key.length());
                HashMap<String, ScriptVariable> namespace = this.namespaces.get(key);
                for (String subKey : namespace.keySet())
                    size += (0x8 + subKey.length()) + namespace.get(subKey).getAllocatedSize();
            }
        }
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RTypeLibrary.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags, 
            ResourceType.TYPE_LIBRARY,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }
}
