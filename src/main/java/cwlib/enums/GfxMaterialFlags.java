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
    
    public static final int DEFAULT = 
        MAX_PRIORITY | RECEIVE_SHADOWS | RECEIVE_SUN | RECEIVE_SPRITELIGHTS;
}
