package cwlib.enums;

import java.util.EnumSet;

/**
 * All possible modifiers for functions, fields, etc in a script.
 */
public enum ModifierType {
    STATIC(0x0),
    NATIVE(0x1),
    EPHEMERAL(0x2),
    PINNED(0x3),
    CONST(0x4),
    PUBLIC(0x5),
    PROTECTED(0x6),
    PRIVATE(0x7),
    PROPERTY(0x8),
    ABSTRACT(0x9),
    VIRTUAL(0xa),
    OVERRIDE(0xb),
    DIVERGENT(0xc),
    EXPORT(0xd);

    private final int value;
    private ModifierType(int value) { this.value = value; }

    public int getValue() { return this.value; }

    public static short getFlags(EnumSet<ModifierType> set) {
        short flags = 0;
        if (set == null) return flags;
        for (ModifierType type : set)
            flags |= (1 << type.value);
        return flags;
    }

    public static EnumSet<ModifierType> fromValue(int value) {
        EnumSet<ModifierType> bitset = EnumSet.noneOf(ModifierType.class);
        for (ModifierType type : ModifierType.values())
            if ((value & (1 << type.value)) != 0)
                bitset.add(type);
        return bitset;
    }

    public static String toModifierString(EnumSet<ModifierType> set) {
        String[] modifiers = new String[set.size()];
        int i = 0;
        for (ModifierType type : set) 
            modifiers[i++] = type.toString().toLowerCase();
        return String.join(" ", modifiers);
    }
}
