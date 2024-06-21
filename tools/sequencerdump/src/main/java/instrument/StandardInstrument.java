package instrument;

import cwlib.types.data.GUID;

// TODO: complete instrument list
public enum StandardInstrument implements Instrument {
    HARP(129021, "Harp"),
    ACUSTIC_KIT(129031, "Acustic Kit"),
    GHOST(129076, "Ghost"),
    RAY_GUN(129081, "Ray Gun"),
    ROBOT(129082, "Robot"),
    SINE_WAVE(129084, "Sine Wave"),
    SQUARE_WAVE(129085, "Square Wave"),
    TRIANGLE_WAVE(129089, "Triangle Wave"),
    BAIYON_KIT(148321, "Baiyon Kit"),
    MUSIC_BOX(186897, "Music Box");

    final GUID guid;
    final String name;

    StandardInstrument(long guid, String name) {
        this.guid = new GUID(guid);
        this.name = name;
    }

    @Override
    public GUID getGUID() {
        return guid;
    }

    @Override
    public String getName() {
        return name;
    }
}
