package cwlib.enums;

public enum CellGcmEnumForGtf {
    B8(0x81),
    A1R5G5B5(0x82),
    A4R4G4B4(0x83),
    R5G6B5(0x84),
    A8R8G8B8(0x85),
    DXT1(0x86),
    DXT3(0x87),
    DXT5(0x88),
    G8B8(0x8b),
    R5G5B5(0x8f);

    private final int value;
    private CellGcmEnumForGtf(int value) { this.value = value; }
    public int getValue() { return this.value; }
    public static CellGcmEnumForGtf fromValue(int value) {
        for (CellGcmEnumForGtf type : CellGcmEnumForGtf.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
