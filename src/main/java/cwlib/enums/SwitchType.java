package cwlib.enums;

import cwlib.io.ValueEnum;

public enum SwitchType implements ValueEnum<Integer> {
    INVALID(-1),
    BUTTON(0),
    LEVER(1),
    TRINARY(2),
    PROXIMITY(3),
    KEY(4),
    STICKER(5),
    GRAB(6),
    PRESSURE(7),
    PAINT(8),
    CONTROLLER_BINARY(9),
    CONTROLLER_ANALOG(10),
    SACKBOT_KEY(11),
    SACKBOT_PLAYER(12),
    SACKBOT_BOT(13),
    AND(14),
    COUNTDOWN(15),
    TIMER(16),
    TOGGLE(17),
    IMPACT(18),
    RANDOM(19),
    DIRECTION(20),
    OR(21),
    XOR(22),
    NOT(23),
    NOP(24),
    MOISTURE(25),
    INACTIVE(26),
    SIGN_SPLIT(27),
    ALWAYS_ON(28),
    ANIMATIC(29),
    SCORE(30),
    DEATH(31),
    CUTSCENE_CAM_FINISHED(32),
    CUTSCENE_CAM_ACTIVE(33),
    MAGIC_MOUTH(34),
    SELECTOR(35),
    MICROCHIP(36),
    CIRCUIT_BOARD(37),
    CONTROL_PAD(38),
    PROJECTILE(39),
    CIRCUIT_NODE(40),
    ANGLE(41),
    VELOCITY_LINEAR(42),
    VELOCITY_ANGULAR(43),
    MOVINATOR_PAD(44),
    MOTION_RECORDER(45),
    FILTER(46),
    COLOUR_GATE(47),
    WAVE_GENERATOR(48),
    EMITTEE(49),
    POCKET_ITEM(50),
    POSE(51),
    ADVANCED_COUNTDOWN(52),
    STATE_SENSOR(53),
    KEY_REMOTE(54),
    VITA_PAD(55),
    TWEAK_TRIGGER(56),
    WORM_HOLE(57),
    QUEST(58),
    GRID_MOVER(59),
    QUEST_SENSOR(60),
    ADVENTURE_ITEM_GETTER(61),
    SHARDINATOR(62),
    @Deprecated POCKET_ITEM_DISPENSER(63),
    POCKET_ITEM_PEDESTAL(64),
    DATA_SAMPLER(65),
    TREASURE_SLOT(66),
    STREAMING_HINT(67),
    JOINT_POSITION(68),
    SLIDE(69),
    IN_OUT_MOVER(70),
    SAVE_CHIP(71),
    PLATFORM_SENSOR(72),
    RACE_END(73),
    ANTI_STREAMING(74),
    GAME_LIVE_STREAMING_CHOICE(75),
    SEARCHLIGHT(76),
    POPIT_CURSOR_SENSOR(77),
    PROGRESS_BOARD(78),
    BOUNCER(79),
    KILL_TWEAKER(80),
    POWERUP_TWEAKER(81),
    RACE_START(82),
    DECORATION_MOUNT(83),
    SPRING_SENSOR(84);
    
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
