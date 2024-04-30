package cwlib.structs.things.parts;

import com.google.gson.annotations.JsonAdapter;
import cwlib.enums.Branch;
import cwlib.enums.LethalType;
import cwlib.enums.ResourceType;
import cwlib.enums.ShapeFlags;
import cwlib.io.Serializable;
import cwlib.io.gson.AudioMaterialSerializer;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.things.components.shapes.ContactCache;
import cwlib.structs.things.components.shapes.Polygon;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.util.Colors;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Used for collisions and other properties of materials.
 */
public class PShape implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    /**
     * Polygon defining the collision of this Thing.
     */
    public Polygon polygon = new Polygon();

    /**
     * Physical properties of this shape.
     */
    public ResourceDescriptor material =
        new ResourceDescriptor(10724, ResourceType.MATERIAL);

    /**
     * Old physical properties of this shape.
     */
    @GsonRevision(min = 0x15c)
    public ResourceDescriptor oldMaterial;

    public float thickness = 90.0f;

    @GsonRevision(min = 0x227)
    public float massDepth = 1.0f;

    /**
     * RGBA color of the shape.
     */
    public int color = 0xFFFFFFFF;

    /**
     * Brightness of the color of the shape.
     */
    @GsonRevision(min = 0x301)
    public float brightness;

    public float bevelSize = 10.0f;

    public transient Matrix4f COM = new Matrix4f().identity();

    @GsonRevision(min = 0x303)
    public int behavior;

    @GsonRevision(min = 0x303)
    public int colorOff;
    @GsonRevision(min = 0x303)
    public float brightnessOff;

    @GsonRevision(max = 0x306)
    public byte interactPlayMode, interactEditMode = 1;

    public LethalType lethalType = LethalType.NOT;


    @JsonAdapter(AudioMaterialSerializer.class)
    public int soundEnumOverride;

    @GsonRevision(min = 0x2a3)
    public byte playerNumberColor;

    public short flags = ShapeFlags.DEFAULT_FLAGS;

    @GsonRevision(min = 0x307)
    public transient ContactCache contactCache = new ContactCache();

    @GsonRevision(min = 0x3bd)
    public byte stickiness, grabbability, grabFilter;

    @GsonRevision(min = 0x3c1)
    public byte colorOpacity, colorOffOpacity;

    @GsonRevision(lbp3 = true, min = 0x12c)
    public byte colorShininess;

    @GsonRevision(min = 0x3e2)
    public boolean canCollect;
    @GsonRevision(lbp3 = true, min = 0x42)
    public boolean ghosty;

    @GsonRevision(lbp3 = true, min = 0x186)
    public boolean defaultClimbable;
    @GsonRevision(lbp3 = true, min = 0x4b)
    public boolean currentlyClimbable;

    @GsonRevision(lbp3 = true, min = 0x63)
    public boolean headDucking;
    @GsonRevision(lbp3 = true, min = 0x82)
    public boolean isLBP2Shape;
    @GsonRevision(lbp3 = true, min = 0x8a)
    public boolean isStatic;
    @GsonRevision(lbp3 = true, min = 0xe6)
    public boolean collidableSackboy;

    @GsonRevision(lbp3 = true, min = 0x11a)
    public boolean partOfPowerUp, cameraExcluderIsSticky;

    @GsonRevision(lbp3 = true, min = 0x19a)
    public boolean ethereal;
    @GsonRevision(lbp3 = true, min = 0x149)
    public byte zBias;

    @GsonRevision(lbp3 = true, min = 0x14c)
    public byte fireDensity, fireLifetime;

    /* Vita fields */

    @GsonRevision(branch = 0x4431, min = 0x5)
    public byte touchability;
    @GsonRevision(branch = 0x4431, min = 0x26)
    public boolean invisibleTouch;
    @GsonRevision(branch = 0x4431, min = 0x34)
    public byte bouncePadBehavior;
    @GsonRevision(branch = 0x4431, min = 0x5f)
    public float zBiasVita;
    @GsonRevision(branch = 0x4431, min = 0x7a)
    public boolean touchWhenInvisible;

    public PShape() { }

    public PShape(Vector3f[] vertices)
    {
        this.polygon.vertices = vertices;
        this.polygon.loops = new int[] { vertices.length };
        this.polygon.requiresZ = true;
    }

    public PShape(float massDepth, float thickness, Vector3f[] vertices)
    {
        this(vertices);
        this.massDepth = massDepth;
        this.thickness = thickness;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        polygon = serializer.struct(polygon, Polygon.class);
        material = serializer.resource(material, ResourceType.MATERIAL);
        if (version >= 0x15c)
            oldMaterial = serializer.resource(oldMaterial, ResourceType.MATERIAL);

        if (version < 0x13c)
            serializer.u8(0);

        thickness = serializer.f32(thickness);
        if (version >= 0x227)
            massDepth = serializer.f32(massDepth);

        if (version <= 0x389)
        {
            if (serializer.isWriting())
                serializer.getOutput().v4(Colors.RGBA32.fromARGB(color));
            else
            {
                Vector4f color = serializer.getInput().v4();
                this.color = Colors.RGBA32.getARGB(color);
            }
        }
        else color = serializer.i32(color);

        if (version < 0x13c)
            serializer.resource(null, ResourceType.TEXTURE);

        if (version >= 0x301)
            brightness = serializer.f32(brightness);

        bevelSize = serializer.f32(bevelSize);

        if (version < 0x13c)
            serializer.i32(0);

        if (version <= 0x340 || version >= 0x38e)
            COM = serializer.m44(COM);

        if (version <= 0x306)
        {
            interactPlayMode = serializer.i8(interactPlayMode);
            interactEditMode = serializer.i8(interactEditMode);
        }

        if (version >= 0x303)
            behavior = serializer.i32(behavior);


        if (version >= 0x303)
        {
            if (version < 0x38a)
            {
                if (serializer.isWriting())
                    serializer.getOutput().v4(Colors.RGBA32.fromARGB(colorOff));
                else
                {
                    Vector4f color = serializer.getInput().v4();
                    colorOff = Colors.RGBA32.getARGB(color);
                }
            }
            else colorOff = serializer.i32(colorOff);
            brightnessOff = serializer.f32(brightnessOff);
        }

        if (version <= 0x345)
            lethalType = serializer.enum32(lethalType, true);
        else
        {
            if (serializer.isWriting())
                serializer.getOutput().i16(lethalType.getValue().shortValue());
            else lethalType = LethalType.fromValue(serializer.getInput().u16());
        }

        if (version < 0x2b5)
        {
            if (!serializer.isWriting())
            {
                MemoryInputStream stream = serializer.getInput();
                flags = 0;
                if (stream.bool()) flags |= ShapeFlags.COLLIDABLE_GAME;
                if (version >= 0x224 && stream.bool())
                    flags |= ShapeFlags.COLLIDABLE_POPPET;
                if (stream.bool()) flags |= ShapeFlags.COLLIDABLE_WITH_PARENT;
            }
            else
            {
                MemoryOutputStream stream = serializer.getOutput();
                stream.bool((flags & ShapeFlags.COLLIDABLE_GAME) != 0);
                if (version >= 0x224)
                    stream.bool((flags & ShapeFlags.COLLIDABLE_POPPET) != 0);
                stream.bool((flags & ShapeFlags.COLLIDABLE_WITH_PARENT) != 0);
            }
        }

        if (version < 0x13c)
        {
            serializer.u8(0);
            serializer.f32(0); // Is this a float, or is it just because it's an early
            // revision?
        }

        soundEnumOverride = serializer.i32(soundEnumOverride);

        if (version >= 0x29d && version < 0x30c)
        {
            serializer.f32(0); // restitution
            if (version < 0x2b5)
                serializer.u8(0); // unk
        }

        if (version >= 0x2a3)
        {
            if (version <= 0x367)
                playerNumberColor = (byte) serializer.i32(playerNumberColor);
            else
                playerNumberColor = serializer.i8(playerNumberColor);
        }

        if (version >= 0x2b5)
        {
            if (version <= 0x345)
                flags = serializer.i8((byte) (flags));
            else
                flags = serializer.i16(flags);
        }

        if (version >= 0x307)
            contactCache = serializer.struct(contactCache, ContactCache.class);

        if (version >= 0x3bd)
        {
            stickiness = serializer.i8(stickiness);
            grabbability = serializer.i8(grabbability);
            grabFilter = serializer.i8(grabFilter);
        }

        if (version >= 0x3c1)
        {
            colorOpacity = serializer.i8(colorOpacity);
            colorOffOpacity = serializer.i8(colorOffOpacity);
        }

        // head > 0x3c0
        if (revision.has(Branch.DOUBLE11, 0x5))
            touchability = serializer.i8(touchability);

        if (subVersion >= 0x12c)
            colorShininess = serializer.i8(colorShininess);

        if (version >= 0x3e2)
            canCollect = serializer.bool(canCollect);

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();
            if (vita >= 0x26)
                invisibleTouch = serializer.bool(invisibleTouch);
            if (vita >= 0x34)
                bouncePadBehavior = serializer.i8(bouncePadBehavior);
            if (vita >= 0x5f)
                zBiasVita = serializer.f32(zBiasVita);
            if (vita >= 0x7a)
                touchWhenInvisible = serializer.bool(touchWhenInvisible);
        }

        if (subVersion >= 0x42 && subVersion < 0xc6)
            serializer.u8(0);

        if (subVersion >= 0x42)
            ghosty = serializer.bool(ghosty);

        if (subVersion >= 0x186)
            defaultClimbable = serializer.bool(defaultClimbable);
        if (subVersion >= 0x4b)
            currentlyClimbable = serializer.bool(currentlyClimbable);

        if (subVersion >= 0x63)
            headDucking = serializer.bool(headDucking);
        if (subVersion >= 0x82)
            isLBP2Shape = serializer.bool(isLBP2Shape);
        if (subVersion >= 0x8a)
            isStatic = serializer.bool(isStatic);
        if (subVersion >= 0xe6)
            collidableSackboy = serializer.bool(collidableSackboy);
        if (subVersion >= 0x11a)
        {
            partOfPowerUp = serializer.bool(partOfPowerUp);
            cameraExcluderIsSticky = serializer.bool(cameraExcluderIsSticky);
        }

        if (subVersion >= 0x19a)
            ethereal = serializer.bool(ethereal);

        // Unknown value
        if (subVersion >= 0x120 && subVersion < 0x135)
            serializer.u8(0);

        if (subVersion >= 0x149)
            zBias = serializer.i8(zBias);

        if (subVersion >= 0x14c)
        {
            fireDensity = serializer.i8(fireDensity);
            fireLifetime = serializer.i8(fireLifetime);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        return BASE_ALLOCATION_SIZE;
    }
}