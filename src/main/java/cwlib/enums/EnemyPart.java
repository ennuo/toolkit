package cwlib.enums;

import cwlib.io.ValueEnum;

public enum EnemyPart implements ValueEnum<Integer> {
    LEG(0),
    EYE(1),
    BRAIN(2),
    WHEEL(3),
    ROCKET(4),
    PAINT(5);
    
    private final int value;
    private EnemyPart(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }
    public static EnemyPart fromValue(int value) {
        for (EnemyPart part : EnemyPart.values()) {
            if (part.value == value) 
                return part;
        }
        return null;
    }
}
