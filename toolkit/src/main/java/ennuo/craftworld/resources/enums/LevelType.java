package ennuo.craftworld.resources.enums;

public enum LevelType {
    COOPERATIVE(0),
    MINI_LEVEL(1),
    MINI_GAME(2),
    TUTORIAL(3),
    RANDOM_CRAP(4),
    BOSS(5),
    VERSUS(6),
    CUTSCENE(7),
    LBP2_DLC_HUB(8);
    
    public final int value;
    
    private LevelType(int value) { this.value = value; }
    
    public static LevelType getValue(int value) {
        for (LevelType type : LevelType.values()) {
            if (type.value == value) 
                return type;
        }
        return LevelType.COOPERATIVE;
    }
}
