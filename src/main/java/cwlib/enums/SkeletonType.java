package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * Refers to a specific character's
 * skeleton type in LittleBigPlanet 3 RMesh files.
 */
public enum SkeletonType implements ValueEnum<Byte> {
    SACKBOY(0),
    GIANT(1),
    DWARF(2),
    BIRD(3),
    QUAD(4);

    private final byte value;
    private SkeletonType(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }

    public static SkeletonType fromValue(byte value) {
        for (SkeletonType type : SkeletonType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
