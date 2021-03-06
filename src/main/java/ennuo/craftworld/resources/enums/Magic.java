package ennuo.craftworld.resources.enums;

public enum Magic {
    MESH("MSHb"),
    ANIMATION("ANMb"),
    GUID_SUBST("GSBb"),
    GFX_MATERIAL("GMTb"),
    LEVEL("LVLb"),
    SCRIPT("FSHb"),
    SETTINGS_CHARACTER("CHAb"),
    SETTINGS_SOFT_PHYS("SSPb"),
    FONTFACE("FNTb"),
    MATERIAL("MATb"),
    DOWNLOADABLE_CONTENT("DLCb"),
    JOINT("JNTb"),
    GAME_CONSTANTS("CONb"),
    POPPET_SETTINGS("POPb"),
    CACHED_LEVEL_DATA("CLDb"),
    SYNCED_PROFILE("PRFb"),
    BEVEL("BEVb"),
    GAME("GAMb"),
    SETTINGS_NETWORK("NETb"),
    PACKS("PCKb"),
    BIG_PROFILE("BPRb"),
    SLOT_LIST("SLTb"),
    ADVENTURE_CREATE_PROFILE("ADCb"),
    LOCAL_PROFILE("IPRb"),
    LIMIT_SETTINGS("LMTb"),
    TUTORIALS("TUTb"),
    GUID_LIST("GLTb"),
    AUDIO_MATERIAL("AUMb"),
    SETTINGS_FLUID("SSFb"),
    PLAN("PLNb"),
    TEXTURE_LIST("TXLb"),
    MUSIC_SETTINGS("MUSb"),
    MIXER_SETTINGS("MIXb"),
    REPLAY_CONFIG("REPb"),
    PALETTE("PALb"),
    ANIMATED_TEXTURE("ATXb"),
    VOIP_RECORDING("VOPb"),
    PINS("PINb"),
    INSTRUMENT("INSb"),
    OUTFIT_LIST("OUTb"),
    PAINTBRUSH("PBRb"),
    THING_RECORDING("RECb"),
    PAINTING("PTGb"),
    QUEST("QSTb"),
    ANIMATION_BANK("ABKb"),
    ANIMATION_SET("ASTb"),
    SKELETON_MAP("SMPb"),
    SKELETON_REGISTRY("SRGb"),
    SKELETON_ANIM_STYLE("SASb"),
    STREAMING_CHUNK("CHKb"),
    SHARED_ADVENTURE_DATA("ADSb"),
    ADVENTURE_PLAY_PROFILE("ADPb"),
    ANIMATION_MAP("AMPb"),
    CACHED_COSTUME_DATA("CCDb"),
    DATA_LABELS("DLAb"),
    ADVENTURE_MAPS("ADMb");
    
    public final String value;
    private Magic(String value) { this.value = value; }
}
