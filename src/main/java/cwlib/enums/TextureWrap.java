package cwlib.enums;

import cwlib.io.ValueEnum;

public enum TextureWrap implements ValueEnum<Byte> {
    WRAP(1),
    MIRROR(2),
    CLAMP_TO_EDGE(3),
    BORDER(4),
    CLAMP(5),
    MIRROR_ONCE_CLAMP_TO_EDGE(6),
    MIRROR_ONCE_BORDER(7),
    MIRROR_ONCE_CLAMP(8);
    
    private final byte value;
    private TextureWrap(int value) {
        this.value = (byte) (value & 0xFF);
    }

    public Byte getValue() { return this.value; }
    public static TextureWrap fromValue(byte value) {
        for (TextureWrap wrap : TextureWrap.values()) {
            if (wrap.value == value) 
                return wrap;
        }
        return null;
    }
}
