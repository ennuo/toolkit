package ennuo.craftworld.resources.enums;

public enum MachineType {
    VOID(0),
    BOOL(1),
    CHAR(2),
    INT(3),
    FLOAT(4),
    VECTOR4(5),
    MATRIX(6),
    DEPRECATED(7),
    RAW_PTR(8),
    REF_PTR(9),
    SAFE_PTR(10),
    OBJECT(11),
    S64(12);
    
    public final int value;
    
    private MachineType(int value) { this.value = value; }
    
    public static MachineType getValue(int value) {
        for (MachineType type : MachineType.values()) {
            if (type.value == value) 
                return type;
        }
        return MachineType.VOID;
    }
}
