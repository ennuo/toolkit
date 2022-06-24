package cwlib.enums;

import cwlib.io.ValueEnum;

public enum TriggerType implements ValueEnum<Integer> {
    RADIUS(0),
    RECT(1),
    SWITCH(2),
    TOUCH(3),
    RADIUS_3D(4);
    
    private final int value;
    private TriggerType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }
    public static TriggerType fromValue(int value) {
        for (TriggerType type : TriggerType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
