package ennuo.craftworld.resources.enums;

import java.util.EnumSet;

public enum CostumePieceCategory {
    BEARD,
    FEET,
    EYES,
    GLASSES,
    MOUTH,
    MOUSTACHE,
    NOSE,
    HAIR,
    HEAD,
    NECK,
    TORSO,
    LEGS,
    HANDS,
    WAIST;
    
    public static int getFlags(EnumSet<InventoryObjectType> set) {
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
