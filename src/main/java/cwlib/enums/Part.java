package cwlib.enums;

import java.lang.reflect.Field;
import java.util.ArrayList;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.*;

/**
 * Part related enumerations used for serialization.
 */
public enum Part {
    BODY(0x0, PartHistory.BODY, PBody.class),
    JOINT(0x1, PartHistory.JOINT, PJoint.class),
    WORLD(0x2, PartHistory.WORLD, null),
    RENDER_MESH(0x3, PartHistory.RENDER_MESH, PRenderMesh.class),
    POS(0x4, PartHistory.POS, PPos.class),
    TRIGGER(0x5, PartHistory.TRIGGER, PTrigger.class),
    YELLOWHEAD(0x6, PartHistory.YELLOWHEAD, null),
    AUDIO_WORLD(0x7, PartHistory.AUDIO_WORLD, null),
    ANIMATION(0x8, PartHistory.ANIMATION, PAnimation.class),
    GENERATED_MESH(0x9, PartHistory.GENERATED_MESH, PGeneratedMesh.class),
    LEVEL_SETTINGS(0xA, PartHistory.LEVEL_SETTINGS, null),
    SPRITE_LIGHT(0xB, PartHistory.SPRITE_LIGHT, null),
    SCRIPT_NAME(0xC, PartHistory.SCRIPT_NAME, PScriptName.class),
    CREATURE(0xD, PartHistory.CREATURE, null),
    CHECKPOINT(0xE, PartHistory.CHECKPOINT, PCheckpoint.class),
    STICKERS(0xF, PartHistory.STICKERS, PStickers.class),
    DECORATIONS(0x10, PartHistory.DECORATIONS, null),
    SCRIPT(0x11, PartHistory.SCRIPT, null),
    SHAPE(0x12, PartHistory.SHAPE, PShape.class),
    EFFECTOR(0x13, PartHistory.EFFECTOR, PEffector.class),
    EMITTER(0x14, PartHistory.EMITTER, null),
    REF(0x15, PartHistory.REF, null),
    METADATA(0x16, PartHistory.METADATA, null),
    COSTUME(0x17, PartHistory.COSTUME, null),
    CAMERA_TWEAK(0x18, PartHistory.CAMERA_TWEAK, null),
    SWITCH(0x19, PartHistory.SWITCH, null),
    SWITCH_KEY(0x1a, PartHistory.SWITCH_KEY, PSwitchKey.class),
    GAMEPLAY_DATA(0x1b, PartHistory.GAMEPLAY_DATA, null),
    ENEMY(0x1c, PartHistory.ENEMY, null),
    GROUP(0x1d, PartHistory.GROUP, PGroup.class),
    PHYSICS_TWEAK(0x1e, PartHistory.PHYSICS_TWEAK, null),
    NPC(0x1f, PartHistory.NPC, null),
    SWITCH_INPUT(0x20, PartHistory.SWITCH_INPUT, null),
    MICROCHIP(0x21, PartHistory.MICROCHIP, null),
    MATERIAL_TWEAK(0x22, PartHistory.MATERIAL_TWEAK, null),
    MATERIAL_OVERRIDE(0x23, PartHistory.MATERIAL_OVERRIDE, null),
    INSTRUMENT(0x24, PartHistory.INSTRUMENT, PInstrument.class),
    SEQUENCER(0x25, PartHistory.SEQUENCER, null),
    CONTROLINATOR(0x26, PartHistory.CONTROLINATOR, null),
    POPPET_POWERUP(0x27, PartHistory.POPPET_POWERUP, null),
    POCKET_ITEM(0x28, PartHistory.POCKET_ITEM, null),
    TRANSITION(0x29, PartHistory.TRANSITION, null),
    FADER(0x2a, PartHistory.FADER, null),
    ANIMATION_TWEAK(0x2b, PartHistory.ANIMATION_TWEAK, null),
    WIND_TWEAK(0x2c, PartHistory.WIND_TWEAK, null),
    POWER_UP(0x2d, PartHistory.POWER_UP, null),
    HUD_ELEM(0x2e, PartHistory.HUD_ELEM, null),
    TAG_SYNCHRONIZER(0x2f, PartHistory.TAG_SYNCHRONIZER, null),
    WORMHOLE(0x30, PartHistory.WORLD, null),
    QUEST(0x31, PartHistory.QUEST, null),
    CONNECTOR_HOOK(0x32, PartHistory.CONNECTOR_HOOK, null),
    ATMOSPHERIC_TWEAK(0x33, PartHistory.ATMOSHPERIC_TWEAK, null),
    STREAMING_DATA(0x34, PartHistory.STREAMING_DATA, null),
    STREAMING_HINT(0x35, PartHistory.STREAMING_HINT, null);

    /**
     * Minimum version required for this part
     * to be serialized.
     */
    private final int version;

    /**
     * Index of this part.
     */
    private final int index;

    /**
     * Serializable class reference
     */
    private final Class<?> serializable;

    /**
     * Creates a part.
     * @param index Index of this part
     * @param version The minimum version required for this part to be serialized
     * @param serializable The serializable class represented by this part
     */
    private Part(int index, int version, Class<?> serializable) {
        this.index = index;
        this.version = version;
        this.serializable = serializable;
    }

    /**
     * Prepares a name used when serializating
     * this part using reflection.
     * @return Field name
     */
    public String getNameForReflection() {
        String name = this.name().toLowerCase();
        String[] words = name.split("_");

        for (int i = 1; i < words.length; ++i) {
            String word = words[i];
            char c = word.charAt(0);
            words[i] = word.replace(c, Character.toUpperCase(c));
        }

        name = String.join("", words);

        /* Switch is a reserved keyword */
        if (name == "switch")
            name = "switchBase";
        
        return name;
    }

    /**
     * Gets all the parts that can be serialized
     * by a set of flags.
     * @param flags Flags determing what parts can be serialized by a Thing
     * @return Parts
     */
    public static Part[] fromFlags(long flags) {
        ArrayList<Part> parts = new ArrayList<>(64);
        for (Part part : Part.values())
            if (((1L << part.index) & flags) != 0)
                parts.add(part);
        return parts.toArray(Part[]::new);
    }

    /**
     * Checks whether or not a thing contains this part
     * based on version and part flags.
     * @param version Version determing what parts existed at this point
     * @param flags Flags determing what parts can be serialized by a Thing
     * @return Whether or not a thing contains this part
     */
    public boolean hasPart(int version, long flags) {
        return version >= this.version
            && ((1L << this.index) & flags) != 0;
    }

    /**
     * (De)serializes this part to a stream.
     * @param <T> Type of part
     * @param thing Thing that potentially contains this part
     * @param version Version determing what parts existed at this point
     * @param flags Flags determing what parts can be serialized by this Thing
     * @param serializer Instance of a serializer stream
     * @return Whether or not the operation succeeded
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> boolean serialize(Thing thing, int version, long flags, Serializer serializer) {
        Field field = null;
        try {
            /* The Thing doesn't have this part, so it's "successful" */
            if (!this.hasPart(version, flags))
                return true;

            if (this.serializable == null)
                return false;

            /* Get the part field via reflection */
            field = thing.getClass().getDeclaredField(this.getNameForReflection());
            if (field == null) return false;
            field.setAccessible(true); // Oh, dear
            T part = (T) field.get(thing);

            /* Serialize the value and set the field if we're not writing */
            T value = serializer.reference(part, (Class<T>)this.serializable);
            if (!serializer.isWriting())
                field.set(thing, value);
            
            /* If we got to this point, it succeeded. */
            return true;
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
            /* Don't really care */
        } finally {
            /* Make sure we restore the original accessibility */
            if (field != null)
                field.setAccessible(false);
        }
        return false; // D'oh!
    }
}
