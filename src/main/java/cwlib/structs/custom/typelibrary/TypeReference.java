package cwlib.structs.custom.typelibrary;

import cwlib.enums.BuiltinType;
import cwlib.enums.MachineType;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class TypeReference implements Serializable {
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