package cwlib.enums;

public enum ModCompatibility {
    
    LBP1(1),
    LBP2(2),
    LBP3(7 | 11),
    LBP3_PS3(7),
    LBP3_PS4(11),
    LBPV(16),
    PS3(7),
    PS4(8),
    ALL(31);
    
    public final int value;
    private ModCompatibility(int value) { this.value = value; } 
    
    public static ModCompatibility getValue(int value) {
        for (ModCompatibility type : ModCompatibility.values()) {
            if (type.value == value) 
                return type;
        }
        return ModCompatibility.ALL;
    }
    
}
