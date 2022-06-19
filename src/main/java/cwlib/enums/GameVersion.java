package cwlib.enums;

import cwlib.types.data.Revision;

public class GameVersion {
    public static final int LBP1 = (1 << 0);
    public static final int LBP2 = (1 << 1);
    public static final int LBP3 = (1 << 2);
    
    public static int getFlag(Revision revision) {
        if (revision.getVersion() <= 0x332) return GameVersion.LBP1;
        if (revision.isLBP3()) return GameVersion.LBP3;
        return GameVersion.LBP2;
    }
}
