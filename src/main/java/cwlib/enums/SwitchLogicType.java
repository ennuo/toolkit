package cwlib.enums;

import cwlib.io.ValueEnum;

public enum SwitchLogicType implements ValueEnum<Integer> {
    AND(0),
    OR(1),
    XOR(2),
    NOT(3),
    NOP(4);
    
    private final int value;
    private SwitchLogicType(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }
    public static SwitchLogicType fromValue(int value) {
        for (SwitchLogicType type : SwitchLogicType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
