package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * The type of decal rendered.
 */
public enum DecalType implements ValueEnum<Byte> {
    STICKER(0),
    PAINT(1),
    EYETOY(2);

    private final byte value;
    private DecalType(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }

    public static DecalType fromValue(byte value) {
        for (DecalType type : DecalType.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
