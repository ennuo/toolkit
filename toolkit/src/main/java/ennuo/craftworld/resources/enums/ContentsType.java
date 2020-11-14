package ennuo.craftworld.resources.enums;

public enum ContentsType {
    THEME(0),
    PACK(1),
    LEVEL(2),
    COSTUME(3),
    ADVENTURE(5);
    
    public final int value;
    
    private ContentsType(int value) { this.value = value; }
    
    public static ContentsType getValue(int value) {
        for (ContentsType type : ContentsType.values()) {
            if (type.value == value) 
                return type;
        }
        return ContentsType.THEME;
    }
    
}
