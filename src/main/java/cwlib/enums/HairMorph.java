package cwlib.enums;

import cwlib.io.ValueEnum;

public enum HairMorph implements ValueEnum<Integer> {
    HAT(0),
    HELMET(1),
    FRINGE(2);

    private final int value;
    private HairMorph(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    public static HairMorph fromValue(int value) {
        for (HairMorph morph : HairMorph.values()) {
            if (morph.value == value) 
                return morph;
        }
        return null;
    }
}
