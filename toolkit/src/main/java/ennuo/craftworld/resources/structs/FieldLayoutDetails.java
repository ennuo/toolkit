package ennuo.craftworld.resources.structs;

import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.enums.MachineType;

public class FieldLayoutDetails {
    public String fieldName;
    public int modifiers;
    public MachineType machineType;
    public int fishType;
    public int dimensionCount;
    public int arrayBaseMachineType;
    public int instanceOffset;
    
    public FieldLayoutDetails(Data data, int partsRevision) {
        fieldName = data.str8();
        
        if (partsRevision >= 0x76) modifiers = data.int16();
        else modifiers = data.int32();
        
        machineType = MachineType.getValue(data.int32());
        fishType = data.int32();
        dimensionCount = data.int8();
        arrayBaseMachineType = data.int32();
        instanceOffset = data.int32();
    }
}
