package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import cwlib.enums.AudioMaterial;
import cwlib.enums.LethalType;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.shapes.ContactCache;
import cwlib.structs.things.components.shapes.Polygon;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.Colors;

/**
 * Used for collisions and other properties of materials.
 */
public class PShape implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    /**
     * Polygon defining the collision of this Thing.
     */
    public Polygon polygon = new Polygon();

    /**
     * Physical properties of this shape.
     */
    public ResourceDescriptor material = 
        new ResourceDescriptor(10716, ResourceType.MATERIAL);

    /**
     * Old physical properties of this shape.
     */
    public ResourceDescriptor oldMaterial;

    public float thickness = 90.0f;

    public float massDepth = 1.0f;

    /**
     * RGBA color of the shape.
     */
    public int color = 0xFFFFFFFF;

    /**
     * Brightness of the color of the shape.
     */
    public float brightness;

    public float bevelSize = 10.0f;

    public Matrix4f COM =  new Matrix4f().identity();

    public int behavior;

    public int colorOff;
    public float brightnessOff;

    public byte interactPlayMode, interactEditMode;

    public LethalType lethalType = LethalType.NOT;

    public boolean collidableGame, collidablePoppet, collidableWithParent;

    public AudioMaterial soundEnumOverride = AudioMaterial.NONE;
    public byte playerNumberColor;

    public short flags;

    public ContactCache contactCache = new ContactCache();

    public byte stickiness;

    public byte grabbability;
    public byte grabFilter;

    public byte colorOpacity;
    public byte colorOffOpacity;
    public byte colorShininess;

    public boolean canCollect;
    public boolean ghosty;

    public boolean defaultClimbable;
    public boolean currentlyClimbable;

    public boolean headDucking;
    public boolean isLBP2Shape;
    public boolean isStatic;
    public boolean collidableSackboy;

    public boolean partOfPowerUp;
    public boolean cameraExcluderIsSticky;

    public boolean ethereal;
    public byte zBias;

    public byte fireDensity;
    public byte fireLifetime;

    /* Vita fields */

    @SuppressWarnings("unused") public byte touchability;
    @SuppressWarnings("unused") public boolean invisibleTouch;
    @SuppressWarnings("unused") public byte bouncePadBehavior;
    @SuppressWarnings("unused") public boolean touchWhenInvisible;

    @SuppressWarnings("unchecked")
    @Override public PShape serialize(Serializer serializer, Serializable structure) {
        PShape shape = (structure == null) ? new PShape() : (PShape) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        shape.polygon = serializer.struct(shape.polygon, Polygon.class);
        shape.material = serializer.resource(shape.material, ResourceType.MATERIAL);
        if (version >= 0x15c)
            shape.oldMaterial = serializer.resource(shape.oldMaterial, ResourceType.MATERIAL);
        
        shape.thickness = serializer.f32(shape.thickness);
        if (version >= 0x227)
            shape.massDepth = serializer.f32(shape.massDepth);

        if (version <= 0x389) {
            if (serializer.isWriting())
                serializer.getOutput().v4(Colors.RGBA32.toVector(shape.color));
            else {
                Vector4f color = serializer.getInput().v4();
                shape.color = Colors.RGBA32.fromVector(color);
            }
        } else shape.color = serializer.i32(shape.color);

        if (version >= 0x301)
            shape.brightness = serializer.f32(shape.brightness);
        
        shape.bevelSize = serializer.f32(shape.bevelSize);
        
        if (version <= 0x340 || version >= 0x38e)
            shape.COM = serializer.m44(shape.COM);

        if (version <= 0x306) {
            shape.interactPlayMode = serializer.i8(shape.interactPlayMode);
            shape.interactEditMode = serializer.i8(shape.interactEditMode);
        }

        if (version >= 0x303)
            shape.behavior = serializer.i32(shape.behavior);

        if (version >= 0x38a)
            shape.colorOff = serializer.i32(shape.colorOff);
        if (version >= 0x303)
            shape.brightnessOff = serializer.f32(shape.brightnessOff);

        if (version <= 0x345)
            shape.lethalType = serializer.enum32(shape.lethalType);
        else {
            if (serializer.isWriting()) serializer.getOutput().i16(shape.lethalType.getValue().shortValue());
            else shape.lethalType = LethalType.fromValue(serializer.getInput().u16());
        }

        if (version <= 0x2b4) {
            shape.collidableGame = serializer.bool(shape.collidableGame);
            if (version >= 0x224)
                shape.collidablePoppet = serializer.bool(shape.collidablePoppet);
            shape.collidableWithParent = serializer.bool(shape.collidableWithParent);
        }

        shape.soundEnumOverride = serializer.enum32(shape.soundEnumOverride);

        if (version >= 0x2a3) {
            if (version <= 0x367)
                shape.playerNumberColor = (byte) serializer.i32(shape.playerNumberColor);
            else
                shape.playerNumberColor = serializer.i8(shape.playerNumberColor);
        }

        if (version >= 0x2b5) {
            if (version <= 0x345)
                shape.flags = serializer.i8((byte)(shape.flags));
            else
                shape.flags = serializer.i16(shape.flags);
        }

        if (version >= 0x307)
            shape.contactCache = serializer.struct(shape.contactCache, ContactCache.class);

        if (version >= 0x3bd) {
            shape.stickiness = serializer.i8(shape.stickiness);
            shape.grabbability = serializer.i8(shape.grabbability);
            shape.grabFilter = serializer.i8(shape.grabFilter);
        }

        if (version >= 0x3c1) {
            shape.colorOpacity = serializer.i8(shape.colorOpacity);
            shape.colorOffOpacity = serializer.i8(shape.colorOffOpacity);
        }

        if (subVersion >= 0x12c)
            shape.colorShininess = serializer.i8(shape.colorShininess);

        if (version >= 0x3e2)
            shape.canCollect = serializer.bool(shape.canCollect);


        if (subVersion >= 0x42 && subVersion < 0xc6)
            serializer.u8(0);

        if (subVersion >= 0x42)
            shape.ghosty = serializer.bool(shape.ghosty);

        if (subVersion >= 0x186)
            shape.defaultClimbable = serializer.bool(shape.defaultClimbable);
        if (subVersion >= 0x4b)
            shape.currentlyClimbable = serializer.bool(shape.currentlyClimbable);

        if (subVersion >= 0x63)
            shape.headDucking = serializer.bool(shape.headDucking);
        if (subVersion >= 0x82)
            shape.isLBP2Shape = serializer.bool(shape.isLBP2Shape);
        if (subVersion >= 0x8a)
            shape.isStatic = serializer.bool(shape.isStatic);
        if (subVersion >= 0xe6)
            shape.collidableSackboy = serializer.bool(shape.collidableSackboy);
        if (subVersion >= 0x11a) {
            shape.partOfPowerUp = serializer.bool(shape.partOfPowerUp);
            shape.cameraExcluderIsSticky = serializer.bool(shape.cameraExcluderIsSticky);
        }
        
        if (subVersion >= 0x19a)
            shape.ethereal = serializer.bool(shape.ethereal);
        
        // Unknown value
        if (subVersion >= 0x120 && subVersion < 0x135)
            serializer.u8(0);
        
        if (subVersion >= 0x149)
            shape.zBias = serializer.i8(shape.zBias);
        
        if (subVersion >= 0x14c) {
            shape.fireDensity = serializer.i8(shape.fireDensity);
            shape.fireLifetime = serializer.i8(shape.fireLifetime);
        }

        return shape;
    }

    @Override public int getAllocatedSize() { return BASE_ALLOCATION_SIZE; }
}