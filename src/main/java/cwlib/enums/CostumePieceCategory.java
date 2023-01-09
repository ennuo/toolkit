package cwlib.enums;

import java.util.EnumSet;

public enum CostumePieceCategory {
    BEARD(0),
    FEET(1),
    EYES(2),
    GLASSES(3),
    MOUTH(4),
    MOUSTACHE(5),
    NOSE(6),
    HAIR(7),
    HEAD(8),
    NECK(9),
    TORSO(10),
    LEGS(11),
    HANDS(12),
    WAIST(13);

    private CostumePieceCategory(int index) {
        this.index = index;
        this.flag = (1 << index);
    }

    private final int index;
    private final int flag;

    public int getIndex() { return this.index; }
    public int getFlag() { return this.flag; }

    public static int getFlags(EnumSet<CostumePieceCategory> set) {
        int flags = 0;
        CostumePieceCategory[] categories = CostumePieceCategory.values();
        for (int i = 0; i < categories.length; ++i) {
            CostumePieceCategory category = categories[i];
            if (set.contains(category))
                flags |= (1 << i);
        }
        return flags;
    }
    
    public static EnumSet<CostumePieceCategory> fromFlags(int flags) {
        EnumSet<CostumePieceCategory> set = EnumSet.noneOf(CostumePieceCategory.class);
        CostumePieceCategory[] categories = CostumePieceCategory.values();
        for (int i = 0; i < categories.length; ++i) {
            if ((flags & (1 << i)) == 0) continue;
            set.add(categories[i]);
        }
        return set;
    }
    
    public static String getPrimaryName(EnumSet<CostumePieceCategory> set) {
        if (set == null || set.isEmpty()) return "none";
        return set.iterator().next().name().toLowerCase();
    }
}
