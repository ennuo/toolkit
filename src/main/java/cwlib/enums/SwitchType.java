package cwlib.enums;

import cwlib.io.ValueEnum;

public enum SwitchType implements ValueEnum<Integer> {
    MAGNETIC(0),
    IMPACT(1);
    
    private final int value;
    private SwitchType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }
    public static SwitchType fromValue(int value) {
        for (SwitchType type : SwitchType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
