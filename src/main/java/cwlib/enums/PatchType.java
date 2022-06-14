package cwlib.enums;

public enum PatchType {
    LAMS(0),
    UNKNOWN(-1);
    
    public final int value;
    
    private PatchType(int value) { this.value = value; }
    
    public static PatchType getValue(int value) {
        for (PatchType type : PatchType.values()) {
            if (type.value == value) 
                return type;
        }
        return PatchType.UNKNOWN;
    }
    
}
