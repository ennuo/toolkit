package cwlib.enums;

import cwlib.io.ValueEnum;

public enum GameMode implements ValueEnum<Integer> {
    NORMAL(0),
    COMPETITIVE(1),
    CUT_SCENE(2),
    SINGLE_PLAYER(3),
    SOCIAL(4),
    TEAMS(5);

    // old enum
    // normal 0
    // competitive 1
    // teams 2
    // social 3

    private final int value;
    private GameMode(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    /**
     * Attempts to get a GameMode from value.
     * @param value Game mode value
     * @return GameMode
     */
    public static GameMode fromValue(int value) {
        for (GameMode mode : GameMode.values()) {
            if (mode.value == value) 
                return mode;
        }
        return GameMode.NORMAL;
    }
}
