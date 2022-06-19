package cwlib.enums;

import cwlib.io.ValueEnum;

public enum InventorySortMode implements ValueEnum<Integer> {
    INVALID(-1),
    DATE_OLDEST(1),
    DATE_NEWEST(2),
    @Deprecated MOST_RECENTLY_USED(3),
    @Deprecated NUM_USES(4),
    LOCATION(5),
    CATEGORY(6),
    SIZE(7),
    COLOR(8),
    NAME(9),
    CREATOR(10);

    private final int value;
    private InventorySortMode(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    public static InventorySortMode fromValue(int value) {
        for (InventorySortMode mode : InventorySortMode.values()) {
            if (mode.value == value) 
                return mode;
        }
        return InventorySortMode.INVALID;
    }
}