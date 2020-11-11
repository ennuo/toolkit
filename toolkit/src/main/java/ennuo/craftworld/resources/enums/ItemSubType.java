package ennuo.craftworld.resources.enums;

public enum ItemSubType {
    NONE(0L),
    EYES(0x4L),
    GLASSES(0x8L),
    HAIR(0x80L),
    HEAD(0x100L),
    MOUSTACHE(0x20L),
    MOUTH(0x10L),
    FEET(0x2L),
    HANDS(0x1000L),
    LEGS(0x800L),
    NECK(0x200L),
    TORSO(0x400L),
    WAIST(0x2000L),
    OUTFITS(0x80000000L),
    SPECIAL(0x04000000L),
    
    PLAYER_COLOUR_LBP_1(0),
    PLAYER_COLOUR_LBP_2(1),
    PLAYER_COLOUR_LBP_3(2),
    PLAYER_COLOUR_LBP_4(3),
    PLAYER_COLOUR_LBP_5(4),
    PLAYER_COLOUR_LBP_6(5),
    PLAYER_COLOUR_LBP_7(6),
    PLAYER_COLOUR_LBP_8(7),
    PLAYER_COLOUR_LBP_9(8),
    PLAYER_COLOUR_LBP_10(9),
    PLAYER_COLOUR_LBP_11(10),
    PLAYER_COLOUR_LBP_12(11),
    PLAYER_COLOUR_LBP3_13(12),
    PLAYER_COLOUR_LBP3_14(13),
    PLAYER_COLOUR_LBP3_15(14),
    PLAYER_COLOUR_LBP3_16(15),
    PLAYER_COLOUR_LBP3_17(16),
    PLAYER_COLOUR_LBP3_18(17),
    PLAYER_COLOUR_LBP3_19(18),
    PLAYER_COLOUR_LBP3_20(19),
    PLAYER_COLOUR_LBP3_21(20),
    PLAYER_COLOUR_LBP3_22(21),
    PLAYER_COLOUR_LBP3_23(22),
    PLAYER_COLOUR_LBP3_24(23);
    
    public final long value;
    private ItemSubType(long value) { this.value = value; }
    
    public static ItemSubType getValue(long value, ItemType main) {
        
        if (main.equals(main.COLOURS)) {
            long colourValue = value + 1;
            String prefix = "PLAYER_COLOUR_LBP_";
            if (value > 12) prefix = "PLAYER_COLOUR_LBP3_";
            ItemSubType colour = ItemSubType.valueOf(prefix + colourValue);
            if (colour == null) return ItemSubType.NONE;
            return colour;
        }
        
        for (ItemSubType type : ItemSubType.values()) {
            if (type.value == value) 
                return type;
        }
        return ItemSubType.NONE;
    }
}
