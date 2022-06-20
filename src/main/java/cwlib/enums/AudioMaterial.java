package cwlib.enums;

import cwlib.io.ValueEnum;

/**
 * Sound effects played when interacting
 * with materials.
 */
public enum AudioMaterial implements ValueEnum<Integer> {
    NONE(0),
    STONE(1),
    METAL(2),
    WOOD(3),
    POLYSTYRENE(4),
    CLOTH(5),
    CARDBOARD(6),
    SPONGE(7),
    RUBBER(8),
    CREATIVE(9),
    FIRE(10),
    ICE(11),
    ELECTRICITY(12),
    GLASS(13),
    SACKBOY(14),
    BUOYANT(15),
    SLIME(16),
    FOIL(17),
    WICKER(18),
    AFRICAN_DRUM(19),
    METAL_GRILL(20),
    SKATEBOARD(21),
    SANDPAPER(22),
    CUTLERY(23),
    PLASTIC(24),
    MATCHBOX(25),
    GLASS_BOTTLE(26),
    FRUIT(27),
    GOLFBALL(28),
    FOOTBALL(29),
    BEACHBALL(30),
    BASKETBALL(31),
    METAL_LIGHT(32),
    DISSOLVE(33),
    PAINTBALL(34),
    SILENCE(35),
    FOIL_HARD(36),
    TREASURE(37),
    SAND(38),
    METAL_DIGITAL(39),
    BUBBLEWRAP(40),
    BOOKS(41),
    BISCUIT(42),
    SOIL(43),
    PAPER(44),
    CLOTH_WET(45),
    LEATHER(46),
    FLOATY(47),
    COLLECTABELL(48);

    private final int value;
    private AudioMaterial(int value) {
        this.value = value;
    }

    public Integer getValue() { return this.value; }

    /**
     * Attempts to get an audio material from value.
     * @param value Sound enum value
     * @return Audio material
     */
    public static AudioMaterial fromValue(int value) {
        for (AudioMaterial type : AudioMaterial.values()) {
            if (type.value == value) 
                return type;
        }
        return null;
    }
}
