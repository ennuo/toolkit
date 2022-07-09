package cwlib.enums;

import cwlib.io.ValueEnum;

public enum PlayMode implements ValueEnum<Integer> {
    TRIGGER_BY_FALLOFF(0x0),
    TRIGGER_BY_IMPACT(0x1),
    TRIGGER_BY_DESTROY(0x2);
    
    private final int value;
    private PlayMode(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    public static PlayMode fromValue(int value) {
        for (PlayMode mode : PlayMode.values()) {
            if (mode.value == value) 
                return mode;
        }
        return null;
    }
}
