package cwlib.enums;

import cwlib.io.ValueEnum;

public enum SlotType implements ValueEnum<Integer> {
    DEVELOPER(0),
    USER_CREATED_ON_SERVER(1),
    USER_CREATED_STORED_LOCAL(2),
    LOCAL_GROUP(3),
    DEVELOPER_GROUP(4),
    POD(5),
    FAKE(6),
    REMOTE_MOON(7),
    DLC_LEVEL(8),
    DLC_PACK(9),
    PLAYLIST(10),
    DEVELOPER_ADVENTURE(11),
    DEVELOPER_ADVENTURE_PLANET(12),
    DEVELOPER_ADVENTURE_AREA(13),
    USER_ADVENTURE_PLANET_PUBLISHED(14),
    ADVENTURE_PLANET_LOCAL(15),
    ADVENTURE_LEVEL_LOCAL(16),
    ADVENTURE_AREA_LEVEL(17);

    private final int value;

    private SlotType(int value) { this.value = value; }
    public Integer getValue() { return this.value; }

    /**
     * Attempts to get a SlotType from value.
     * @param value Slot type value
     * @return SlotType
     */
    public static SlotType fromValue(int value) {
        for (SlotType type : SlotType.values()) {
            if (type.value == value)
                return type;
        }
        return SlotType.DEVELOPER;
    }

    public boolean isGroup() {
        return this.equals(SlotType.DEVELOPER_GROUP) ||
                this.equals(SlotType.LOCAL_GROUP) ||
                this.equals(SlotType.DLC_PACK) ||
                this.equals(SlotType.DEVELOPER_ADVENTURE_AREA) ||
                this.equals(SlotType.ADVENTURE_AREA_LEVEL);
    }

    public boolean isLink() {
        return this.equals(SlotType.DEVELOPER) ||
                this.equals(SlotType.DLC_LEVEL) ||
                this.equals(SlotType.ADVENTURE_LEVEL_LOCAL) ||
                this.equals(SlotType.DEVELOPER_ADVENTURE);
    }
}
