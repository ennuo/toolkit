package instrument;

import cwlib.types.data.GUID;

// TODO: LBP3 instruments
public enum StandardInstrument implements Instrument {
    BAIYON_GUILDFORD(143479, "Baiyon Guildford"),
    BAIYON_KYOTO(148320, "Baiyon Kyoto"),
    STATIC_NOISE(125386, "Static Noise"),
    CLARINET(186895, "Clarinet"),
    CONCERTINA(132146, "Concertina"),
    BASSOON(186892, "Bassoon"),
    BRASSES(186893, "Brasses"),
    GLASS_HARMONICA(187921, "Glass Harmonica"),
    TUBULAR_BELLS(186899, "Tubular Bells"),
    MUSIC_BOX(186897, "Music Box"),
    CARILLON(180212, "Kalimba"),
    KALIMBA(187885, "Marimba"),
    GLOCKENSPIEL(129017, "Glockenspiel"),
    VIBRAPHONE(187877, "Vibraphone"),
    EIGHT_BIT_KIT(127540, "8 Bit Kit"),
    BAIYON_KIT(148321, "Baiyon Kit"),
    ACUSTIC_KIT(129031, "Acustic Kit"),
    BEATBOX_KIT_1(129040, "Beatbox Kit 1"),
    BEATBOX_KIT_2(129049, "Beatbox Kit 2"),
    SYNTH_DRUMS_KIT(129066, "Synth Drums Kit"),
    SYNTH_KIT(129057, "Synth Kit"),
    GUITAR(182710, "Guitar"),
    COL_LEGNO(174236, "Col Legno"),
    HARP(129021, "Harp"),
    BASS(125100, "Bass"),
    POWERCHORD_ELECTRIC_GUITAR(138776, "Powerchord Electric Guitar"),
    ELECTRIC_GUITAR(132205, "Electric Guitar"),
    DISTORTED_ELECTRIC_GUITAR(125101, "Distorted Electric Guitar"),
    EKTARA(174225, "Ektara"),
    KOTO(182709, "Koto"),
    LEGATO_STRINGS(186898, "Legato Strings"),
    PIZZICATO_STRINGS(187847, "Pizzicato Stings"),
    PIZZICATO_DOUBLE_BASS(182708, "Pizzicato Double Bass"),
    SYNTH_STRINGS(129087, "Synth Strings"),
    HARPICHORD(129074, "Harpichord"),
    BAIYON_BASS_1(148318, "Baiyon Bass 1"),
    BAIYON_BASS_2(148319, "Baiyon Bass 2"),
    BAIYON_BRIGHT(148322, "Baiyon Bright"),
    SYNTH_BELLS(129086, "Synth Bells"),
    GHOST(129076, "Ghost"),
    MIME(129077, "Mime"),
    SAW_WAVE(129083, "Saw Wave"),
    PULSE_WAVE(129080, "Pulse Wave"),
    SQUARE_WAVE(129085, "Square Wave"),
    SINE_WAVE(129084, "Sine Wave"),
    TRIANGLE_WAVE(129089, "Triangle Wave"),
    ELECTRONIC_PIANO(129075, "Electronic Piano"),
    SPACE_PIANO(129015, "Space Piano"),
    WOODPECKER(129090, "Woodpecker"),
    RAY_GUN(129081, "Ray Gun"),
    ROBOT(129082, "Robot"),
    NOISE(129079, "Noise"),
    TENNIS(129088, "Tennis"),
    BAIYON_TRILL(148323, "Baiyon Trill"),
    WORM(129091, "Worm"),
    MOSQUITO(129078, "Mosquito"),
    PIANO(122737, "Piano"),
    HONKY_TONK_PIANO(122721, "Honky Tonk Piano"),
    AAH_CHOIR(186894, "Aah Choir");

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
