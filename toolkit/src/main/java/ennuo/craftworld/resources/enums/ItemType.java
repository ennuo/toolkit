package ennuo.craftworld.resources.enums;

public enum ItemType {
    NONE(0),
    MATERIALS(1L << 0L),
    OBJECTS(1L << 1L),
    DECORATIONS(1L << 2L),
    STICKERS(1L << 3L),
    COSTUMES(1L << 4L),
    SKINS(1L << 5L),
    JOINTS(1L << 6L),
    CREATED_OBJECTS(1L << 7L),
    BACKGROUNDS(1L << 8L),
    GAMEPLAY_KITS(1L << 9L),
    PHOTOS(1L << 10L),
    USER_PHOTOS((1L << 10L) | (1L << 3L)),
    SHAPES(1L << 11L),
    PAINT(1L << 12L, 1L << 3L),
    SEQUENCER(1L << 21L, 1L << 12L),
    DANGER(1L << 13L),
    EYETOY(1L << 14L),
    GADGET(1L << 15L),
    TOOL(1L << 16L),
    CHARACTERS(1L << 17L),
    CREATED_CHARACTERS((1L << 17L) | (1L << 7L)),
    CREATURES(1L << 18L),
    COLOURS(1L << 19L),
    USER_COSTUMES((1L << 4L) | (1L << 20L)),
    MUSIC(1L << 21L),
    PHOTOBOOTH((1L << 3L) | (1L << 10L) | (1L << 23L)),
    SOUND(1L << 22L),
    KEYS(0, 1L << 23L),
    PLANETS(0, 1L << 24L),
    USER_KEYS(0, (1L << 21L) | (1L << 9L)),
    EMITTED(0, 1L << 25L),
    GUN(0, 1L << 26L),
    FUNCTIONS(0, 1L << 27L),
    NPC_COSTUME(0, 1L << 28L),
    INSTRUMENT(0, 1L << 29L),
    POD(1L << 30L),
    ALL(1L << 31L);
    
    public final long value;
    public final long valueLBP2;
    public final long valueLBP1;
    
    
    private ItemType(long v1) { this.value = v1; this.valueLBP2 = v1; this.valueLBP1 = v1; }
    private ItemType(long v1, long v2) { this.value = v2; this.valueLBP2 = v2; this.valueLBP1 = v1; }
    private ItemType(long v1, long v2, long v3) { this.value = v3; this.valueLBP2 = v2; this.valueLBP1 = v1; }
    
    
    public long getValue(int revision) {
        if (revision <= 0x272) return valueLBP1;
        else if (revision <= 0x3f8) return valueLBP2;
        return value;
    }
    
    public static ItemType getValue(long value, int revision) {
        if (revision <= 0x272 && ((value | 0x10000L) == value))
            return ItemType.TOOL;
        
        for (ItemType type : ItemType.values()) {
            if (revision <= 0x272) {
                if (type.valueLBP1 == value)
                    return type;
            }
            
            else if (revision <= 0x3f8) {
                if (type.valueLBP2 == value)
                    return type;
            }
            
            if (type.value == value)
                return type;
        }
        return ItemType.NONE;
    }
}
