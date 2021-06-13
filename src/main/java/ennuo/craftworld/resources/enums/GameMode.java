package ennuo.craftworld.resources.enums;

public enum GameMode {
    NONE(0),
    VERSUS(1),
    CUTSCENE(2),
    TEAM_VERSUS(5);
    
    public final int value;
    
    private GameMode(int value) { this.value = value; }
    
    public static GameMode getValue(int value) {
        for (GameMode type : GameMode.values()) {
            if (type.value == value) 
                return type;
        }
        return GameMode.NONE;
    }
    
}
