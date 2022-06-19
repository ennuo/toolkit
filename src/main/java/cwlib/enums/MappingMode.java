package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * UV mapping modes for bevel vertices,
 * Thanks to DokkeFyxen for figuring out what each do.
 */
public enum MappingMode implements ValueEnum<Byte> {
    HIDDEN(0),

    /**
     * Standard XYZ UV Mapping.
     */
    DAVE(1),
    
    /**
     * Wraps vertically; separate faces.
     */
    CYLINDER_01(2),

    /**
     * Wraps vertical; all one face.
     */
    CYLINDER(3),

    /**
     * Wraps vertically; seperate faces, stretches on Y axis.
     */
    PLANARXZ_01(4),

    /**
     * Wraps vertically; all one face, stretches on Y axis.
     */
    PLANARXZ(5);

    private final byte value;
    private MappingMode(int value) {
        this.value = (byte) value;
    }

    public Byte getValue() { return this.value; }

    public static MappingMode fromValue(int value) {
        for (MappingMode mode : MappingMode.values()) {
            if (mode.value == value) 
                return mode;
        }
        return null;
    }
}
