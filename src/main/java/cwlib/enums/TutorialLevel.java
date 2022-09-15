package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * Represents every possible tutorial level
 * and video.
 */
public enum TutorialLevel implements ValueEnum<Integer> {
    UNKNOWN(0x0),
    NONE(0x1),
    LEVEL_10_MOVING_OBJECTS(0x2),
    LEVEL_11_CREATING_AND_DELETING_OBJECTS(0x3),
    LEVEL_13_ADDING_MUSIC(0x4),
    LEVEL_15_CRAFT_MATERIALS_PART_1(0x5),
    LEVEL_16_CORNER_EDITING_TOOL(0x6),
    LEVEL_17_CRAFT_MATERIALS_PART_2(0x7),
    LEVEL_18_FLOOD_FILL_TOOL(0x8),
    LEVEL_19_SOUND_OBJECTS(0x9),
    LEVEL_21_MAGIC_MOUTHS(0xa),
    LEVEL_29_DANGEROUS_STUFF(0xb),
    LEVEL_30_BOLTS(0xc),
    LEVEL_31_SPRUNG_BOLTS(0xd),
    LEVEL_32_STRING(0xe),
    LEVEL_33_ELASTIC(0xf),
    LEVEL_34_RODS(0x10),
    LEVEL_35_SPRINGS(0x11),
    LEVEL_36_MOTORIZED_BOLTS(0x12),
    LEVEL_37_OSCILLATOR_BOLTS(0x13),
    LEVEL_38_CHAINS(0x14),
    LEVEL_39_PISTONS(0x15),
    LEVEL_40_EMITTERS(0x16),
    LEVEL_41_BUTTON_SWITCHES(0x17),
    LEVEL_42_TWO_WAY_SWITCHES(0x18),
    LEVEL_43_STICKER_SWITCHES(0x19),
    LEVEL_44_GRAB_SWITCH(0x1a),
    LEVEL_45_THREE_WAY_SWITCHES(0x1b),
    LEVEL_46_PROXIMITY_SWITCHES(0x1c),
    LEVEL_47_MAGNETIC_KEY_SWITCHES(0x1d),
    LEVEL_48_DISSOLVE_MATERIAL(0x1e),
    LEVEL_49_EXPLOSIVE_OBJECTS(0x1f),
    LEVEL_50_ROCKETS(0x20),
    LEVEL_51_CREATURE_KIT(0x21),

    VIDEO_CAMERA_ZONES(0x22),
    VIDEO_CAPTURE_OBJECTS(0x23),
    VIDEO_CHECKPOINTS(0x24),
    VIDEO_ENTRANCE(0x25),
    VIDEO_FLOATY(0x26),
    VIDEO_GLOBAL_CONTROLS(0x27),
    VIDEO_KEYS(0x28),
    VIDEO_POINT_BUBBLES(0x29),
    VIDEO_PRIZE_BUBBLES(0x2a),
    VIDEO_RACE_KIT(0x2b),
    VIDEO_SCORING_POST(0x2c),
    VIDEO_EYETOY(0x2d),
    VIDEO_SNAPSHOT_CAM(0x2e),
    VIDEO_MAGIC_EYE(0x2f),
    VIDEO_NO_JOIN_POST(0x30);

    private final int value;
    private TutorialLevel(int value) { this.value = value; }
    public Integer getValue() { return this.value; }
    
    /**
     * Attempts to get a ToolType from value.
     * @param value Tool type value
     * @return ToolType
     */
    public static TutorialLevel fromValue(int value) {
        for (TutorialLevel level : TutorialLevel.values()) {
            if (level.value == value) 
                return level;
        }
        return TutorialLevel.UNKNOWN;
    }
}
