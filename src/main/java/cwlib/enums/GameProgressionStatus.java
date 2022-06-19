package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * Status for story mode progression
 * in the original LittleBigPlanet.
 */
public enum GameProgressionStatus implements ValueEnum<Integer> {
    NEW_GAME(0),
    POD_ARRIVAL(1),
    ENTERED_STORY_LEVEL(2),
    FIRST_LEVEL_COMPLETED(3),
    GAME_PROGRESSION_COMPLETED(4),
    FIRST_GROUP_COMPLETED(4);

    private final int value;
    private GameProgressionStatus(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    /**
     * Attempts to get GameProgressionStatus from value.
     * @param value Game progression status value
     * @return GameProgressionStatus
     */
    public static GameProgressionStatus fromValue(int value) {
        for (GameProgressionStatus status : GameProgressionStatus.values()) {
            if (status.value == value) 
                return status;
        }
        return GameProgressionStatus.NEW_GAME;
    }
}
