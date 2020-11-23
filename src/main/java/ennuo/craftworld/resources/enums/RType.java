package ennuo.craftworld.resources.enums;

public enum RType {
    UNKNOWN(0),
    TEXTURE(1),
    MESH(2),
    PIXEL_SHADER(3),
    VERTEX_SHADER(4),
    ANIM(5),
    GUID_SUBST(6),
    GFXMATERIAL(7),
    SPU_ELF(8),
    LEVEL(9),
    FILENAME(10),
    SCRIPT(11),
    SETTINGS_CHARACTER(12),
    FILE_OF_BYTES(13),
    SETTINGS_SOFT_PHYS(14),
    FONTFACE(15),
    MATERIAL(16),
    DOWNLOADABLE_CONTENT(17),
    EDITOR_SETTINGS(18),
    JOINT(19),
    GAME_CONSTANTS(20),
    POPPET_SETTINGS(21),
    CACHED_LEVEL_DATA(22),
    SYNCED_PROFILE(23),
    BEVEL(24),
    GAME(25),
    SETTINGS_NETWORK(26),
    PACKS(27),
    BIG_PROFILE(28),
    SLOT_LIST(29),
    TRANSLATION(30),
    ADVENTURE_CREATE_PROFILE(31),
    LOCAL_PROFILE(32),
    LIMITS_SETTINGS(33),
    TUTORIALS(34),
    GUID_LIST(35),
    AUDIO_MATERIALS(36),
    SETTINGS_FLUID(37),
    PLAN(38),
    TEXTURE_LIST(39),
    MUSIC_SETTINGS(40),
    MIXER_SETTINGS(41),
    REPLAY_CONFIG(42),
    PALETTE(43),
    STATICMESH(44),
    ANIMATED_TEXTURE(45),
    VOIP_RECORDING(46),
    PINS(47),
    INSTRUMENT(48),
    SAMPLE(49),
    OUFIT_LIST(50),
    PAINT_BRUSH(51),
    THING_RECORDING(52),
    PAINTING(53),
    QUEST(54),
    ANIMATION_BANK(55),
    ANIMATION_SET(56),
    SKELETON_MAP(57),
    SKELETON_REGISTRY(58),
    SKELETON_ANIM_STYLES(59),
    CROSSPLAY_VITA(60),
    STREAMING_CHUNK(61),
    SHARED_ADVENTURE_DATA(62),
    ADVENTURE_PLAY_PROFILE(63),
    ANIMATION_MAP(64),
    CACHED_COSTUME_DATA(64),
    DATALABELS(66),
    ADVENTURE_MAPS(67);
    
    public final int value;
    
    private RType(int value) { this.value = value; }
    
    public static RType getValue(int value) {
        for (RType type : RType.values()) {
            if (type.value == value) 
                return type;
        }
        return RType.UNKNOWN;
    }
    
}
