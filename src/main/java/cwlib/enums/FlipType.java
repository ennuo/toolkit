package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * Refers to how a mirrored bone gets flipped.
 */
public enum FlipType implements ValueEnum<Byte> {
    /**
     * X.pos, X.rot
     */
    MAX(0),

    /**
     * Y.pos, Y.rot + prerot
     */
    BIP_ROOT(1),

    /**
     * Z.pos, Z.rot + prerot
     */
    BIP_PELVIS(2),

    /**
     * Z.pos, Z.rot
     */
    BIP_BONE(3),

    /**
     * Z.pos, Z.rot + null prerot
     */
    PARENT_WAS_BIP_BONE(4),

    /**
     * X.pos, X.rot + prerot
     */
    GRANDPARENT_WAS_BIP_BONE(5),

    /**
     * No flip, straight copy from mirror bone.
     */
    COPY(6);

    private final byte value;
    private FlipType(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }

    public static FlipType fromValue(int value) {
        for (FlipType type : FlipType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
