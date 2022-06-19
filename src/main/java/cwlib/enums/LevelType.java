package cwlib.enums;

import cwlib.io.ValueEnum;

public enum LevelType implements ValueEnum<Integer> {
    MAIN_PATH(0),
    MINI_LEVEL(1),
    MINI_GAME(2),
    TUTORIAL(3),
    RANDOM_CRAP(4),
    BOSS(5),
    VERSUS(6),
    CUTSCENE(7),
    LBP2_DLC_HUB(8);

    private final int value;
    private LevelType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    /**
     * Attempts to get a LevelType from value.
     * @param value Level type value
     * @return LevelType
     */
    public static LevelType fromValue(int value) {
        for (LevelType type : LevelType.values()) {
            if (type.value == value) 
                return type;
        }
        return LevelType.MAIN_PATH;
    }
}
