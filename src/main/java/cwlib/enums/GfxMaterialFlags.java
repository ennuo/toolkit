package cwlib.enums;

public final class GfxMaterialFlags {
    public static final int TWO_SIDED = 0x1;
    public static final int FURRY = 0x2;
    public static final int WIRE = 0x4;
    public static final int MAX_PRIORITY = 0x8;
    public static final int SQUISHY = 0x10;
    public static final int NO_INSTANCE_TEXTURE = 0x20;
    public static final int RECEIVE_SHADOWS = 0x100;
    public static final int RECEIVE_SUN = 0x200;
    public static final int RECEIVE_SPRITELIGHTS = 0x400;
    public static final int ALPHA_CLIP = 0x800; // If this flag is set in LBP2, if color_out.w == 0, the pixel will be discarded.

    // 0x2000, some flag in LBP3, unknown usage (Only a few materials DONT have this set)
    // 0x8000, some flag in LBP3, unknown usage (related to toon shading)

    // DEFAULT = 1800
    // DEFAULT_TWO_SIDED = 1801
    // DEFAULT_ALPHA_CLIP = 3848
    // DEFAULT_ALPHA_CLIP_TWO_SIDED = 3849
    
    public static final int DEFAULT = 
        MAX_PRIORITY | RECEIVE_SHADOWS | RECEIVE_SUN | RECEIVE_SPRITELIGHTS;
}
