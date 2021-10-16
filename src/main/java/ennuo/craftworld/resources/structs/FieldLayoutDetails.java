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
        
        if (partsRevision >= 0x76) modifiers = data.i16();
        else modifiers = data.i32();
        
        machineType = MachineType.getValue(data.i32());
        fishType = data.i32();
        dimensionCount = data.i8();
        arrayBaseMachineType = data.i32();
        instanceOffset = data.i32();
    }
}
