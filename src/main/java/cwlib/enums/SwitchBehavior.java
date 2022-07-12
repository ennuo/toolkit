package cwlib.enums;

import cwlib.io.ValueEnum;

public enum SwitchBehavior implements ValueEnum<Integer> {
    OFF_ON(0),
    SPEED_SCALE(1),
    DIRECTION(2),
    ONE_SHOT(3);
    
    private final int value;
    private SwitchBehavior(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }
    public static SwitchBehavior fromValue(int value) {
        for (SwitchBehavior behavior : SwitchBehavior.values()) {
            if (behavior.value == value) 
                return behavior;
        }
        return null;
    }
}
