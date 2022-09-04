package cwlib.enums;

import cwlib.io.ValueEnum;

public enum GameMode implements ValueEnum<Integer> {
    NONE(0),
    VERSUS(1),
    CUTSCENE(2),
    TEAM_VERSUS(3);

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
        return GameMode.NONE;
    }
}
