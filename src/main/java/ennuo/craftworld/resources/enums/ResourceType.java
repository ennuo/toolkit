package ennuo.craftworld.resources.enums;

public enum ResourceType {
    INVALID(null, 0),
    TEXTURE("TEX", 1),
    GTF_TEXTURE("GTF", 1),
    GXT_TEXTURE("GXT", 1),
    MESH("MSH", 2),
    PIXEL_SHADER(null, 3),
    VERTEX_SHADER(null, 4),
    ANIMATION("ANM", 5),
    GUID_SUBSTITUTION("GSB", 6),
    GFX_MATERIAL("GMT", 7),
    SPU_ELF(null, 8),
    LEVEL("LVL", 9),
    FILENAME(null, 10),
    SCRIPT("FSH", 11),
    SETTINGS_CHARACTER("CHA", 12),
    FILE_OF_BYTES(null, 13),
    SETTINGS_SOFT_PHYS("SSP", 14),
    FONTFACE("FNT", 15),
    MATERIAL("MAT", 16),
    DOWNLOADABLE_CONTENT("DLC", 17),
    EDITOR_SETTINGS(null, 18),
    JOINT("JNT", 19),
    GAME_CONSTANTS("CON", 20),
    POPPET_SETTINGS("POP", 21),
    CACHED_LEVEL_DATA("CLD", 22),
    SYNCED_PROFILE("PRF", 23),
    BEVEL("BEV", 24),
    GAME("GAM", 25),
    SETTINGS_NETWORK("NWS", 26),
    PACKS("PCK", 27),
    BIG_PROFILE("BPR", 28),
    SLOT_LIST("SLT", 29),
    TRANSLATION(null, 30),
    ADVENTURE_CREATE_PROFILE("ADC", 31),
    LOCAL_PROFILE("IPR", 32),
    LIMITS_SETTINGS("LMT", 33),
    TUTORIALS("TUT", 34),
    GUID_LIST("GLT", 35),
    AUDIO_MATERIALS("AUM", 36),
    SETTINGS_FLUID("SSF", 37),
    PLAN("PLN", 38),
    TEXTURE_LIST("TXL", 39),
    MUSIC_SETTINGS("MUS", 40),
    MIXER_SETTINGS("MIX", 41),
    REPLAY_CONFIG("REP", 42),
    PALETTE("PAL", 43),
    STATIC_MESH("SMH", 44),
    ANIMATED_TEXTURE("ATX", 45),
    VOIP_RECORDING("VOP", 46),
    PINS("PIN", 47),
    INSTRUMENT("INS", 48),
    SAMPLE(null, 49),
    OUTFIT_LIST("OFT", 50),
    PAINT_BRUSH("PBR", 51),
    THING_RECORDING("REC", 52),
    PAINTING("PTG", 53),
    QUEST("QST", 54),
    ANIMATION_BANK("ABK", 55),
    ANIMATION_SET("AST", 56),
    SKELETON_MAP("SMP", 57),
    SKELETON_REGISTRY("SRG", 58),
    SKELETON_ANIM_STYLES("SAS", 59),
    CROSSPLAY_VITA(null, 60),
    STREAMING_CHUNK("CHK", 61),
    SHARED_ADVENTURE_DATA("ADS", 62),
    ADVENTURE_PLAY_PROFILE("ADP", 63),
    ANIMATION_MAP("AMP", 64),
    CACHED_COSTUME_DATA("CCD", 65),
    DATA_LABELS("DLA", 66),
    ADVENTURE_MAPS("ADM", 67);
    
    public final String header;
    public final int value;
    
    private ResourceType(String magic, int value) {
        this.header = magic;
        this.value = value;
    }
    
    public static ResourceType fromMagic(String value) {
        for (ResourceType type : ResourceType.values()) {
            if (type.header == null) continue;
            if (type.header.equals(value)) 
                return type;
        }
        return ResourceType.INVALID;
    }
    
    public static ResourceType fromType(int value) {
        for (ResourceType type : ResourceType.values()) {
            if (type.value == value) 
                return type;
        }
        return ResourceType.INVALID;
    }
}
