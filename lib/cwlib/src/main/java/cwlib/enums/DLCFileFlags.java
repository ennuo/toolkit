package cwlib.enums;

// 0x844 = goty
// 

public class DLCFileFlags
{
    public static final int NONE = 0x0;
    public static final int OWNED = 0x1;
    public static final int AUTO_ADD_DISABLED = 0x2;
    public static final int IS_GOTY_GIVEAWAY = 0x4;
    public static final int IS_BETA = 0x8;
    public static final int IS_SPECIAL_EDITION = 0x10; // combine with goty giveaway, more accurately IS_LBP2
    // 0x20
    // 0x40
    public static final int IS_MOVE_PACK =  0x80;
    // 0x100 = IS_???_PACK
    public static final int NO_INDIVIDUAL_PACKS = 0x200; // don't try to check if children are cheaper than bundle
    public static final int NO_BUNDLED_PACK = 0x400; // dont try to check if bundle is cheaper than children
    public static final int IS_CROSS_PLAY = 0x800;
    public static final int IS_DC_COMICS = 0x1000;
}
