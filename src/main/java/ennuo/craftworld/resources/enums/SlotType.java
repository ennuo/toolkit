package ennuo.craftworld.resources.enums;

public enum SlotType {
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
    ADVENTURE_AREA_LEVEL(17),
    MAX(18);
    
    public final int value;
    
    private SlotType(int value) { this.value = value; }
    
    public static SlotType getValue(int value) {
        for (SlotType type : SlotType.values()) {
            if (type.value == value) 
                return type;
        }
        return SlotType.DEVELOPER;
    }
    
    public boolean isGroup() {
       return this.equals(SlotType.DEVELOPER_GROUP) || this.equals(SlotType.LOCAL_GROUP) || this.equals(SlotType.DLC_PACK);
    }
    
    public boolean isLink() {
        return this.equals(SlotType.DEVELOPER) || this.equals(SlotType.DLC_LEVEL);
    }
    
}
