package cwlib.enums;

import java.util.EnumSet;

public final class InventoryObjectSubType {
    public static final int NONE = 0x0;

    public static final int EARTH = 0x0;
    public static final int MOON = 0x1;
    public static final int ADVENTURE = 0x2;
    public static final int EXTERNAL = 0x4;

    public static final int CREATURE_MASK_GIANT = 0x00200000;
    public static final int CREATURE_MASK_DWARF = 0x00400000;
    public static final int CREATURE_MASK_BIRD = 0x00600000;
    public static final int CREATURE_MASK_QUAD = 0x00800000;

    public static final int CREATURE_MASK = 0x03E00000;
    
    public static final int PAINTING = 0x02000000;
    public static final int SPECIAL_COSTUME = 0x04000000;
    public static final int PLAYER_AVATAR = 0x08000000;
    public static final int EARTH_DECORATION = 0x10000000;

    public static final int MADE_BY_ME = 0x20000000;
    public static final int MADE_BY_OTHERS = 0x40000000;
    public static final int FULL_COSTUME = 0x80000000;
    public static final int MADE_BY_ANYONE = MADE_BY_ME | MADE_BY_OTHERS;
    
    public static String getTypeString(EnumSet<InventoryObjectType> type, int subType) {
        if (type.isEmpty()) return "NONE";
        if (type.contains(InventoryObjectType.PLAYER_COLOUR))
            return String.format("PLAYER_COLOUR_%2d", subType);
        if (type.contains(InventoryObjectType.COSTUME) || type.contains(InventoryObjectType.USER_COSTUME)) {
            if ((subType & FULL_COSTUME) != 0) return "OUTFIT";
            if ((subType & SPECIAL_COSTUME) != 0) return "SPECIAL";
            CostumePieceCategory[] categories = CostumePieceCategory.values(); 
            for (int i = 0; i < categories.length; ++i)
                if ((subType & (1 << i)) != 0)
                    return categories[i].name();
            return "NONE";
        }
        if (type.contains(InventoryObjectType.STICKER) && ((subType & PAINTING) != 0))
            return "PAINTING";
        if (type.contains(InventoryObjectType.USER_PLANET)) {
            if (subType == EARTH) return "EARTH";
            else if (subType == MOON) return "MOON";
            else if (subType == ADVENTURE) return "ADVENTURE";
            else if (subType == EXTERNAL) return "EXTERNAL";
        }
        return "NONE";
    }
}
