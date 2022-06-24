package cwlib.enums;

import cwlib.io.Serializable;
import cwlib.io.ValueEnum;
import cwlib.resources.RAdventureCreateProfile;
import cwlib.resources.RBevel;
import cwlib.resources.RBigProfile;
import cwlib.resources.RGfxMaterial;
import cwlib.resources.RJoint;
import cwlib.resources.RLocalProfile;
import cwlib.resources.RMaterial;
import cwlib.resources.RMesh;
import cwlib.resources.RPacks;
import cwlib.resources.RPalette;
import cwlib.resources.RPlan;
import cwlib.resources.RSlotList;
import cwlib.resources.custom.RBoneSet;
import cwlib.resources.custom.RShaderCache;

/**
 * All valid resource types used by the
 * LittleBigPlanet games.
 */
public enum ResourceType implements ValueEnum<Integer> {
    INVALID(null, 0, ".raw"),
    TEXTURE("TEX", 1, ".tex"),
    GTF_TEXTURE("GTF", 1, ".tex"),
    MESH("MSH", 2, RMesh.class, ".mol"),
    PIXEL_SHADER(null, 3, ".fpo"),
    VERTEX_SHADER(null, 4, ".vpo"),
    ANIMATION("ANM", 5, ".anim"),
    GUID_SUBSTITUTION("GSB", 6, ".gsub"),
    GFX_MATERIAL("GMT", 7, RGfxMaterial.class, ".gmat"),
    SPU_ELF(null, 8, ".sbu"),
    LEVEL("LVL", 9, ".bin"),
    FILENAME(null, 10, ".txt"), // Could be anything really, but generally will refer to either FSB or BIK
    SCRIPT("FSH", 11, ".ff"),
    SETTINGS_CHARACTER("CHA", 12, ".cha"),
    FILE_OF_BYTES(null, 13, ".raw"),
    SETTINGS_SOFT_PHYS("SSP", 14, ".sph"),
    FONTFACE("FNT", 15, ".fnt"),
    MATERIAL("MAT", 16, RMaterial.class, ".mat"),
    DOWNLOADABLE_CONTENT("DLC", 17, ".dlc"),
    EDITOR_SETTINGS(null, 18, ".edset"),
    JOINT("JNT", 19, RJoint.class, ".joint"),
    GAME_CONSTANTS("CON", 20, ".con"),
    POPPET_SETTINGS("POP", 21, ".pop"),
    CACHED_LEVEL_DATA("CLD", 22, ".cld"),
    SYNCED_PROFILE("PRF", 23, ".pro"),
    BEVEL("BEV", 24, RBevel.class, ".bev"),
    GAME("GAM", 25, ".game"),
    SETTINGS_NETWORK("NWS", 26, ".nws"),
    PACKS("PCK", 27, RPacks.class, ".pck"),
    BIG_PROFILE("BPR", 28, RBigProfile.class, ".bpr"),
    SLOT_LIST("SLT", 29, RSlotList.class, ".slt"),
    TRANSLATION(null, 30, ".trans"),
    ADVENTURE_CREATE_PROFILE("ADC", 31, RAdventureCreateProfile.class, ".adc"),
    LOCAL_PROFILE("IPR", 32, RLocalProfile.class, ".ipr"),
    LIMITS_SETTINGS("LMT", 33, ".lmt"),
    TUTORIALS("TUT", 34, ".tut"),
    GUID_LIST("GLT", 35, ".glst"),
    AUDIO_MATERIALS("AUM", 36, ".aum"),
    SETTINGS_FLUID("SSF", 37, ".flu"),
    PLAN("PLN", 38, RPlan.class, ".plan"),
    TEXTURE_LIST("TXL", 39, ".yuv"),
    MUSIC_SETTINGS("MUS", 40, ".mus"),
    MIXER_SETTINGS("MIX", 41, ".mix"),
    REPLAY_CONFIG("REP", 42, ".rep"),
    PALETTE("PAL", 43, RPalette.class, ".pal"),
    STATIC_MESH("SMH", 44, ".smh"),
    ANIMATED_TEXTURE("ATX", 45, ".atx"),
    VOIP_RECORDING("VOP", 46, ".vop"),
    PINS("PIN", 47, ".pin"),
    INSTRUMENT("INS", 48, ".rinst"),
    SAMPLE(null, 49, ".smp"),
    OUTFIT_LIST("OFT", 50, ".oft"),
    PAINT_BRUSH("PBR", 51, ".pbr"),
    THING_RECORDING("REC", 52, ".rec"),
    PAINTING("PTG", 53, ".ptg"),
    QUEST("QST", 54, ".qst"),
    ANIMATION_BANK("ABK", 55, ".abnk"),
    ANIMATION_SET("AST", 56, ".aset"),
    SKELETON_MAP("SMP", 57, ".smap"),
    SKELETON_REGISTRY("SRG", 58, ".sreg"),
    SKELETON_ANIM_STYLES("SAS", 59, ".sas"),
    CROSSPLAY_VITA(null, 60, ".cpv"),
    STREAMING_CHUNK("CHK", 61, ".chk"),
    ADVENTURE_SHARED_DATA("ADS", 62, ".ads"),
    ADVENTURE_PLAY_PROFILE("ADP", 63, ".adp"),
    ANIMATION_MAP("AMP", 64, ".amap"),
    CACHED_COSTUME_DATA("CCD", 65, ".ccd"),
    DATA_LABELS("DLA", 66, ".dla"),
    ADVENTURE_MAPS("ADM", 67, ".adm"),

    // Custom Toolkit/Workbench resources

    BONE_SET("BST", 128, RBoneSet.class, ".boneset"),
    SHADER_CACHE("CGC", 129, RShaderCache.class, ".shadercache");
    
    private final String header;
    private final int value;
    private final Class<? extends Serializable> compressable;
    private final String extension;
    
    private ResourceType(String magic, int value, Class<? extends Serializable> clazz, String extension) {
        this.header = magic;
        this.value = value;
        this.compressable = clazz;
        this.extension = extension;
    }

    private ResourceType(String magic, int value, String extension) {
        this.header = magic;
        this.value = value;
        this.compressable = null;
        this.extension = extension;
    }

    public String getHeader() { return this.header; }
    public Integer getValue() { return this.value; }
    public Class<? extends Serializable> getCompressable() { return this.compressable; }
    public String getExtension() { return this.extension; }
    
    /**
     * Attempts to get a valid ResourceType from a 3-byte magic header.
     * @param value Magic header
     * @return Resource type
     */
    public static ResourceType fromMagic(String value) {
        if (value.length() > 3)
            value = value.substring(0, 3);
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
