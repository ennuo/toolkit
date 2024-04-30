package cwlib.enums;

import cwlib.io.ValueEnum;

public enum SwitchKeyType implements ValueEnum<Integer>
{
    MAGNETIC(0),
    IMPACT(1);

    private final int value;

    SwitchKeyType(int value)
    {
        this.value = value;
    }

    public Integer getValue()
    {
        return this.value;
    }

    public static SwitchKeyType fromValue(int value)
    {
        for (SwitchKeyType type : SwitchKeyType.values())
        {
            if (type.value == value)
                return type;
        }
        return null;
    }
}
