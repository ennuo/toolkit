package cwlib.resources.custom;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cwlib.enums.Branch;
import cwlib.enums.BuiltinType;
import cwlib.enums.CompressionFlags;
import cwlib.enums.MachineType;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.ValueEnum;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;

public class RTypeLibrary implements Compressable, Serializable {

    public static class TypeReference implements Serializable {
        public static final int BASE_ALLOCATION_SIZE = 0x40;

        public String name;
        public MachineType machineType = MachineType.VOID;
        public BuiltinType fishType = BuiltinType.VOID;
        public MachineType arrayBaseMachineType = MachineType.VOID;
        public byte dimensionCount;
        public ResourceDescriptor script;

        public TypeReference() {};
        public TypeReference(MachineType machineType, BuiltinType fishType) {
            this.machineType = machineType;
            this.fishType = fishType;
        }
        public TypeReference(String name, MachineType type, ResourceDescriptor script) {
            this.name = name;
            this.machineType = type;
            this.fishType = BuiltinType.VOID;
            this.script = script;
        }

        @SuppressWarnings("unchecked")
        @Override public TypeReference serialize(Serializer serializer, Serializable structure) {
            TypeReference type = (structure == null) ? new TypeReference() : (TypeReference) structure;

            type.name = serializer.str(type.name);
            type.machineType = serializer.enum32(type.machineType);
            type.fishType = serializer.enum32(type.fishType);
            type.arrayBaseMachineType = serializer.enum32(type.arrayBaseMachineType);
            type.dimensionCount = serializer.i8(type.dimensionCount);
            type.script = serializer.resource(type.script, ResourceType.SCRIPT);

            return type;
        }

        @Override public boolean equals(Object other) {
            if (other == this) return true;
            if (!(other instanceof TypeReference)) return false;
            TypeReference type = (TypeReference) other;

            if (!type.machineType.equals(this.machineType)) return false;
            if (!type.fishType.equals(this.machineType)) return false;
            if (!type.arrayBaseMachineType.equals(this.arrayBaseMachineType)) return false;
            if (type.dimensionCount != this.dimensionCount) return false;
            
            if (this.script != null)
                return this.script.equals(type.script);
            else if (type.script != null)
                return false;

            if (this.machineType == MachineType.S32 || this.machineType == MachineType.RAW_PTR) {
                if (this.name == null && type.name == null) return true;
                if (this.name != null)
                    return this.name.equals(type.name);
                else if (type.name != null)
                    return false;
            }

            return true;
        }

        @Override public int hashCode() {
            if (this.script != null) return this.script.hashCode();
            if ((this.machineType == MachineType.S32 || this.machineType == MachineType.RAW_PTR) && this.name != null)
                return this.name.hashCode();
            int hash = 7;
            hash = 31 * hash + this.machineType.getValue();
            hash = 31 * hash + this.fishType.getValue();
            hash = 31 * hash + this.arrayBaseMachineType.getValue();
            return 31 * hash + (this.dimensionCount & 0xff);
        }

        @Override public int getAllocatedSize() {
            int size = TypeReference.BASE_ALLOCATION_SIZE;
            if (this.name != null)
                size += this.name.length();
            return size;
        }
    }

    public static enum VariableType implements ValueEnum<Byte> {
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

        private VariableType(int value, Class<? extends Serializable> serializable) {
            this.value = (byte) value;
            this.serializable = serializable;
        }
    
        public Class<? extends Serializable> getSerializable() { return this.serializable; }
        public Byte getValue() { return this.value; }
        public static VariableType fromValue(byte value) {
            for (VariableType type : VariableType.values()) {
                if (type.value == value) 
                    return type;
            }
            return null;
        }
    }

    public static enum FunctionType implements ValueEnum<Byte> {
        NORMAL(0),
        GETTER(1),
        SETTER(2);

        private final byte value;
        private FunctionType(int value) {
            this.value = (byte) value;
        }
    
        public Byte getValue() { return this.value; }
        public static FunctionType fromValue(int value) {
            for (FunctionType type : FunctionType.values()) {
                if (type.value == value) 
                    return type;
            }
            return null;
        }
    }

    public static interface Variable extends Serializable {
        VariableType getVariableType();
    }

    public static class Function implements Serializable {
        public static final int BASE_ALLOCATION_SIZE = 0x10;

        public String name;
        public short modifiers;
        public TypeReference[] argumentTypes;
        public TypeReference returnType;

        @SuppressWarnings("unchecked")
        @Override public Function serialize(Serializer serializer, Serializable structure) {
            Function function = (structure == null) ? new Function() : (Function) structure;

            function.name = serializer.str(function.name);
            function.modifiers = serializer.i16(function.modifiers);
            function.argumentTypes = serializer.array(function.argumentTypes, TypeReference.class, true);
            function.returnType = serializer.reference(function.returnType, TypeReference.class);

            return function;
        }

        @Override public int getAllocatedSize() {
            int size = Function.BASE_ALLOCATION_SIZE;
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

    public static class FunctionVariable implements Variable {
        public static final int BASE_ALLOCATION_SIZE = 0x10;

        public Function[] overloads;

        @SuppressWarnings("unchecked")
        @Override public FunctionVariable serialize(Serializer serializer, Serializable structure) {
            FunctionVariable variable = (structure == null) ? new FunctionVariable() : (FunctionVariable) structure;

            variable.overloads = serializer.array(variable.overloads, Function.class);

            return variable;
        }
        

        @Override public int getAllocatedSize() {
            int size = FunctionVariable.BASE_ALLOCATION_SIZE;
            if (this.overloads != null) {
                for (Function overload : this.overloads)
                    size += overload.getAllocatedSize();
            }
            return size;
        }

        @Override public VariableType getVariableType() { 
            return VariableType.FUNCTION; 
        }
    }
    public static class ClassVariable implements Variable {
        public static final int BASE_ALLOCATION_SIZE = 
            (TypeReference.BASE_ALLOCATION_SIZE * 2) + 0x10;
        
        public TypeReference classType;
        public short modifiers;
        public TypeReference superClassType;
        public HashMap<String, Variable> members = new HashMap<>();

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
                    Variable member = variable.members.get(key);
                    stream.enum8(member.getVariableType());
                    serializer.struct(member, (Class<Serializable>) member.getVariableType().getSerializable());
                }
            } else {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                variable.members = new HashMap<>(count);
                for (int i = 0; i < count; ++i) {
                    String key = stream.str();
                    VariableType type = VariableType.fromValue(stream.i8());
                    variable.members.put(
                        key,
                        (Variable) serializer.struct(null, type.getSerializable())
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

        @Override public VariableType getVariableType() { 
            return VariableType.CLASS; 
        }
    }

    public static class EnumVariable implements Variable {
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



        @Override public VariableType getVariableType() { 
            return VariableType.ENUM; 
        }
    }

    public static class FieldVariable implements Variable {
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

        @Override public VariableType getVariableType() { 
            return VariableType.FIELD; 
        }
    }

    public static class PropertyVariable implements Variable {
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

        @Override public VariableType getVariableType() { 
            return VariableType.PROPERTY; 
        }
    }

    public String name;
    public HashMap<String, HashMap<String, Variable>> namespaces = new HashMap<>();

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
                HashMap<String, Variable> namespace = library.namespaces.get(key);
                stream.i32(namespace.size());
                for (String member : namespace.keySet()) {
                    stream.str(member);
                    Variable variable = namespace.get(member);
                    serializer.enum8(variable.getVariableType());
                    serializer.struct(variable, (Class<Serializable>) variable.getClass());
                }
            }
        } else {
            MemoryInputStream stream = serializer.getInput();
            int count = stream.i32();
            library.namespaces = new HashMap<>(count);

            for (int i = 0; i < count; ++i) {
                String key = stream.str();

                HashMap<String, Variable> namespace = new HashMap<>();



                library.namespaces.put(key, namespace);
            }
        }

        return library;
    }


    @Override public int getAllocatedSize() {
        int size = 0xFFFFF;
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