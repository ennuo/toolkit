package cwlib.enums;

import cwlib.io.Serializable;
import cwlib.io.ValueEnum;
import cwlib.resources.*;
import cwlib.resources.custom.*;

/**
 * All valid resource types used by the
 * LittleBigPlanet games.
 */
public enum ResourceType implements ValueEnum<Integer> {
    INVALID(null, 0, "unknown/", ""),
    TEXTURE("TEX", 1, "textures/", ".tex"),
    GTF_TEXTURE("GTF", 1, "textures/", ".tex"),
    MESH("MSH", 2, RMesh.class, "meshes/", ".mol"),
    PIXEL_SHADER(null, 3, "shaders/fragment/", ".fpo"),
    VERTEX_SHADER(null, 4, "shaders/vertex/", ".vpo"),
    ANIMATION("ANM", 5, RAnimation.class, "animations/", ".anim"),
    GUID_SUBSTITUTION("GSB", 6, "guid_subst/", ".gsub"),
    GFX_MATERIAL("GMT", 7, RGfxMaterial.class, "gfx_materials/", ".gmat"),
    SPU_ELF(null, 8, "spu/", ".sbu"),
    LEVEL("LVL", 9, RLevel.class, "levels/", ".bin"),
    FILENAME(null, 10, "text/", ".txt"), // Could be anything really, but generally will refer to either FSB or BIK
    SCRIPT("FSH", 11, "scripts/", ".ff"),
    SETTINGS_CHARACTER("CHA", 12, "character_settings/", ".cha"),
    FILE_OF_BYTES(null, 13, "raw_data/", ".raw"),
    SETTINGS_SOFT_PHYS("SSP", 14, "softphys_settings/", ".sph"),
    FONTFACE("FNT", 15, "fonts/", ".fnt"),
    MATERIAL("MAT", 16, RMaterial.class, "physics_materials/", ".mat"),
    DOWNLOADABLE_CONTENT("DLC", 17, RDLC.class, "dlc/", ".dlc"),
    EDITOR_SETTINGS(null, 18, "editor_settings/", ".edset"),
    JOINT("JNT", 19, RJoint.class, "joints/", ".joint"),
    GAME_CONSTANTS("CON", 20, "constants/", ".con"),
    POPPET_SETTINGS("POP", 21, "poppet_settings/", ".pop"),
    CACHED_LEVEL_DATA("CLD", 22, "cached/levels/", ".cld"),
    SYNCED_PROFILE("PRF", 23, "profiles/synced/", ".pro"),
    BEVEL("BEV", 24, RBevel.class, "bevels/", ".bev"),
    GAME("GAM", 25, "game/", ".game"),
    SETTINGS_NETWORK("NWS", 26, "network_settings/", ".nws"),
    PACKS("PCK", 27, RPacks.class, "packs/", ".pck"),
    BIG_PROFILE("BPR", 28, RBigProfile.class, "profiles/big/", ".bpr"),
    SLOT_LIST("SLT", 29, RSlotList.class, "slots/", ".slt"),
    TRANSLATION(null, 30, "translations/", ".trans"),
    ADVENTURE_CREATE_PROFILE("ADC", 31, RAdventureCreateProfile.class, "adventure_data/create/", ".adc"),
    LOCAL_PROFILE("IPR", 32, RLocalProfile.class, "profiles/local/", ".ipr"),
    LIMITS_SETTINGS("LMT", 33, "limits/", ".lmt"),
    TUTORIALS("TUT", 34, "tutorials/", ".tut"),
    GUID_LIST("GLT", 35, "guids/", ".glst"),
    AUDIO_MATERIALS("AUM", 36, "audio_materials/", ".aum"),
    SETTINGS_FLUID("SSF", 37, "fluid_settings/", ".flu"),
    PLAN("PLN", 38, RPlan.class, "plans/", ".plan"),
    TEXTURE_LIST("TXL", 39, "texture_lists/", ".yuv"),
    MUSIC_SETTINGS("MUS", 40, "music_settings/", ".mus"),
    MIXER_SETTINGS("MIX", 41, "mixer_settings/", ".mix"),
    REPLAY_CONFIG("REP", 42, "replays/", ".rep"),
    PALETTE("PAL", 43, RPalette.class, "palettes/", ".pal"),
    STATIC_MESH("SMH", 44, "static_meshes/", ".smh"),
    ANIMATED_TEXTURE("ATX", 45, "animated_textures/", ".atx"),
    VOIP_RECORDING("VOP", 46, "audio/", ".vop"),
    PINS("PIN", 47, "pins/", ".pin"),
    INSTRUMENT("INS", 48, RInstrument.class, "instruments/", ".rinst"),
    SAMPLE(null, 49, "samples/", ".smp"),
    OUTFIT_LIST("OFT", 50, "outfits/", ".oft"),
    PAINT_BRUSH("PBR", 51, "paintbrushes/", ".pbr"),
    THING_RECORDING("REC", 52, "recordings/", ".rec"),
    PAINTING("PTG", 53, "paintings/", ".ptg"),
    QUEST("QST", 54, "quests/", ".qst"),
    ANIMATION_BANK("ABK", 55, "animations/banks/", ".abnk"),
    ANIMATION_SET("AST", 56, "animations/sets/", ".aset"),
    SKELETON_MAP("SMP", 57, "skeletons/maps/", ".smap"),
    SKELETON_REGISTRY("SRG", 58, "skeletons/registries/", ".sreg"),
    SKELETON_ANIM_STYLES("SAS", 59, "skeleton/animation_styles/", ".sas"),
    CROSSPLAY_VITA(null, 60, "crossplay_data/", ".cpv"),
    STREAMING_CHUNK("CHK", 61, "streaming_chunks/", ".chk"),
    ADVENTURE_SHARED_DATA("ADS", 62, "adventure_data/shared/", ".ads"),
    ADVENTURE_PLAY_PROFILE("ADP", 63, "adventure_data/play_profiles/", ".adp"),
    ANIMATION_MAP("AMP", 64, "animations/maps/", ".amap"),
    CACHED_COSTUME_DATA("CCD", 65, "cached_costume_data/", ".ccd"),
    DATA_LABELS("DLA", 66, "datalabels/", ".dla"),
    ADVENTURE_MAPS("ADM", 67, "adventure_data/maps/", ".adm"),

    // Custom Toolkit/Workbench resources

    BONE_SET("BST", 128, RBoneSet.class, "bonesets/", ".boneset"),
    SHADER_CACHE("CGC", 129, RShaderCache.class, "shader_caches/", ".shadercache"),
    SCENE_GRAPH("SCE", 130, RSceneGraph.class, "scenes/", ".sg"),
    TYPE_LIBRARY("LIB", 131, "type_library/", ".lib");
    
    private final String header;
    private final int value;
    private final Class<? extends Serializable> compressable;
    private final String folder;
    private final String extension;
    
    private ResourceType(String magic, int value, Class<? extends Serializable> clazz, String folder, String extension) {
        this.header = magic;
        this.value = value;
        this.folder = folder;
        this.compressable = clazz;
        this.extension = extension;
    }

    private ResourceType(String magic, int value, String folder, String extension) {
        this.header = magic;
        this.value = value;
        this.compressable = null;
        this.folder = folder;
        this.extension = extension;
    }

    public String getHeader() { return this.header; }
    public Integer getValue() { return this.value; }
    public Class<? extends Serializable> getCompressable() { return this.compressable; }
    public String getFolder() { return this.folder; }
    public String getExtension() { return this.extension; }
    
    /**
     * Attempts to get a valid ResourceType from a 3-byte magic header.
     * @param value Magic header
     * @return Resource type
     */
    public static ResourceType fromMagic(String value) {
        if (value.length() > 3)
            value = value.substring(0, 3);
        value = value.toUpperCase();
        for (ResourceType type : ResourceType.values()) {
            if (type.header == null) continue;
            if (type.header.equals(value)) 
                return type;
        }
        return ResourceType.INVALID;
    }
    
    /**
     * Attempts to get a valid ResourceType from the value index.
     * @param value Resource value index
     * @return Resource type
     */
    public static ResourceType fromType(int value) {
        for (ResourceType type : ResourceType.values()) {
            if (type.value == value) 
                return type;
        }
        return ResourceType.INVALID;
    }
}
