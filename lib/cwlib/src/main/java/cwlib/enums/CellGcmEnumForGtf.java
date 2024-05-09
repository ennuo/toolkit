package cwlib.enums;

public enum CellGcmEnumForGtf
{
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

    CellGcmEnumForGtf(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return this.value;
    }

    public boolean isDXT()
    {
        return this == DXT1 || this == DXT3 || this == DXT5;
    }

    public int getDepth()
    {
        switch (this)
        {
            case B8:
                return 1;
            case A1R5G5B5:
            case A4R4G4B4:
            case R5G6B5:
            case G8B8:
                return 2;
            case DXT1:
                return 8;
            case DXT3:
            case DXT5:
                return 16;
            default:
                return 4;
        }
    }

    public int getImageSize(int width, int height)
    {
        int pitch = getPitch(width);
        if (this.isDXT())
            return pitch * ((height + 3) / 4);
        return pitch * height;
    }
    
    public int getPitch(int width)
    {
        if (this == DXT1) 
            return ((width + 3) / 4) * 8;
        else if (this == DXT3 || this == DXT5)
            return ((width + 3) / 4) * 16;

        return width * getDepth();
    }

    public static CellGcmEnumForGtf fromValue(int value)
    {
        value &= ~(CellGcm.LN | CellGcm.UN);
        for (CellGcmEnumForGtf type : CellGcmEnumForGtf.values())
        {
            if (type.value == value)
                return type;
        }
        return null;
    }
}
