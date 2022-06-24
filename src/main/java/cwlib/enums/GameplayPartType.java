package cwlib.enums;

import cwlib.io.ValueEnum;

public enum GameplayPartType implements ValueEnum<Integer> {
    UNDEFINED(0),
    LEVEL_KEY(1),
    PRIZE_BUBBLE(2),
    SCORE_BUBBLE(3),
    COLLECTABUBBLE(4),
    POCKET_ITEM_BUBBLE(5),
    TREASURE_ITEM(6),
    OBJECT_SAVER(7),
    RUMBLER(8);
    
    private final int value;
    private GameplayPartType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }
    public static GameplayPartType fromValue(int value) {
        for (GameplayPartType part : GameplayPartType.values()) {
            if (part.value == value) 
                return part;
        }
        return null;
    }
}
