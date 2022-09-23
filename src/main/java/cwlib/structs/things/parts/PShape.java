package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.AudioMaterial;
import cwlib.enums.Branch;
import cwlib.enums.LethalType;
import cwlib.enums.ResourceType;
import cwlib.enums.ShapeFlags;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
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
    @GsonRevision(min=0x15c) public ResourceDescriptor oldMaterial;

    public float thickness = 90.0f;

    @GsonRevision(min=0x227) public float massDepth = 1.0f;

    /**
     * RGBA color of the shape.
     */
    public int color = 0xFFFFFFFF;

    /**
     * Brightness of the color of the shape.
     */
    @GsonRevision(min=0x301) public float brightness;

    public float bevelSize = 10.0f;

    public transient Matrix4f COM =  new Matrix4f().identity();

    @GsonRevision(min=0x303) public int behavior;

    @GsonRevision(min=0x303) public int colorOff;
    @GsonRevision(min=0x303) public float brightnessOff;

    @GsonRevision(max=0x306) public byte interactPlayMode, interactEditMode = 1;

    public LethalType lethalType = LethalType.NOT;


    public AudioMaterial soundEnumOverride = AudioMaterial.NONE;
    @GsonRevision(min=0x2a3) public byte playerNumberColor;
    
    public short flags = ShapeFlags.DEFAULT_FLAGS;

    @GsonRevision(min=0x307) public transient ContactCache contactCache = new ContactCache();

    @GsonRevision(min=0x3bd) public byte stickiness, grabbability, grabFilter;

    @GsonRevision(min=0x3c1) public byte colorOpacity, colorOffOpacity;

    @GsonRevision(lbp3=true,min=0x12c) public byte colorShininess;

    @GsonRevision(min=0x3e2) public boolean canCollect;
    @GsonRevision(lbp3=true,min=0x42) public boolean ghosty;

    @GsonRevision(lbp3=true,min=0x186) public boolean defaultClimbable;
    @GsonRevision(lbp3=true,min=0x4b) public boolean currentlyClimbable;

    @GsonRevision(lbp3=true,min=0x63) public boolean headDucking;
    @GsonRevision(lbp3=true,min=0x82) public boolean isLBP2Shape;
    @GsonRevision(lbp3=true,min=0x8a) public boolean isStatic;
    @GsonRevision(lbp3=true,min=0xe6) public boolean collidableSackboy;

    @GsonRevision(lbp3=true,min=0x11a) public boolean partOfPowerUp, cameraExcluderIsSticky;

    @GsonRevision(lbp3=true,min=0x19a) public boolean ethereal;
    @GsonRevision(lbp3=true,min=0x149) public byte zBias;

    @GsonRevision(lbp3=true,min=0x14c) public byte fireDensity, fireLifetime;

    /* Vita fields */

    @GsonRevision(branch=0x4431, min=0x5) public byte touchability;
    @GsonRevision(branch=0x4431, min=0x26) public boolean invisibleTouch;
    @GsonRevision(branch=0x4431, min=0x34) public byte bouncePadBehavior;
    @GsonRevision(branch=0x4431, min=0x5f) public float zBiasVita;
    @GsonRevision(branch=0x4431, min=0x7a) public boolean touchWhenInvisible;

    public PShape() {};
    public PShape(Vector3f[] vertices) {
        this.polygon.vertices = vertices;
        this.polygon.loops = new int[] { vertices.length };
        this.polygon.requiresZ = true;
    } 

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
        
        if (version < 0x13c)
            serializer.u8(0);

        shape.thickness = serializer.f32(shape.thickness);
        if (version >= 0x227)
            shape.massDepth = serializer.f32(shape.massDepth);
        
        if (version <= 0x389) {
            if (serializer.isWriting())
                serializer.getOutput().v4(Colors.RGBA32.fromARGB(shape.color));
            else {
                Vector4f color = serializer.getInput().v4();
                shape.color = Colors.RGBA32.getARGB(color);
            }
        } else shape.color = serializer.i32(shape.color);

        if (version < 0x13c)
            serializer.resource(null, ResourceType.TEXTURE);

        if (version >= 0x301)
            shape.brightness = serializer.f32(shape.brightness);
        
        shape.bevelSize = serializer.f32(shape.bevelSize);

        if (version < 0x13c)
            serializer.i32(0);
        
        if (version <= 0x340 || version >= 0x38e)
            shape.COM = serializer.m44(shape.COM);

        if (version <= 0x306) {
            shape.interactPlayMode = serializer.i8(shape.interactPlayMode);
            shape.interactEditMode = serializer.i8(shape.interactEditMode);
        }

        if (version >= 0x303)
            shape.behavior = serializer.i32(shape.behavior);


        if (version >= 0x303) {
            if (version < 0x38a) {
                if (serializer.isWriting())
                serializer.getOutput().v4(Colors.RGBA32.fromARGB(shape.colorOff));
                else {
                    Vector4f color = serializer.getInput().v4();
                    shape.colorOff = Colors.RGBA32.getARGB(color);
                }
            } else shape.colorOff = serializer.i32(shape.colorOff);
            shape.brightnessOff = serializer.f32(shape.brightnessOff);
        }

        if (version <= 0x345)
            shape.lethalType = serializer.enum32(shape.lethalType, true);
        else {
            if (serializer.isWriting()) serializer.getOutput().i16(shape.lethalType.getValue().shortValue());
            else shape.lethalType = LethalType.fromValue(serializer.getInput().u16());
        }

        if (version < 0x2b5) {
            if (!serializer.isWriting()) {
                MemoryInputStream stream = serializer.getInput();
                shape.flags = 0;
                if (stream.bool()) shape.flags |= ShapeFlags.COLLIDABLE_GAME;
                if (version >= 0x224 && stream.bool()) shape.flags |= ShapeFlags.COLLIDABLE_POPPET;
                if (stream.bool()) shape.flags |= ShapeFlags.COLLIDABLE_WITH_PARENT;
            } else {
                MemoryOutputStream stream = serializer.getOutput();
                stream.bool((shape.flags & ShapeFlags.COLLIDABLE_GAME) != 0);
                if (version >= 0x224)
                    stream.bool((shape.flags & ShapeFlags.COLLIDABLE_POPPET) != 0);
                stream.bool((shape.flags & ShapeFlags.COLLIDABLE_WITH_PARENT) != 0);
            }
        }

        if (version < 0x13c) {
            serializer.u8(0);
            serializer.f32(0); // Is this a float, or is it just because it's an early revision?
        }
        
        shape.soundEnumOverride = serializer.enum32(shape.soundEnumOverride);

        if (version >= 0x29d && version < 0x30c) {
            serializer.f32(0); // restitution
            if (version < 0x2b5)
                serializer.u8(0); // unk
        }

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

        // head > 0x3c0
        if (revision.has(Branch.DOUBLE11, 0x5))
            shape.touchability = serializer.i8(shape.touchability);

        if (subVersion >= 0x12c)
            shape.colorShininess = serializer.i8(shape.colorShininess);

        if (version >= 0x3e2)
            shape.canCollect = serializer.bool(shape.canCollect);

        if (revision.isVita()) {
            int vita = revision.getBranchRevision();
            if (vita >= 0x26)
                shape.invisibleTouch = serializer.bool(shape.invisibleTouch);
            if (vita >= 0x34)
                shape.bouncePadBehavior = serializer.i8(shape.bouncePadBehavior);
            if (vita >= 0x5f)
                shape.zBiasVita = serializer.f32(shape.zBiasVita);
            if (vita >= 0x7a)
                shape.touchWhenInvisible = serializer.bool(shape.touchWhenInvisible);
        }
        
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