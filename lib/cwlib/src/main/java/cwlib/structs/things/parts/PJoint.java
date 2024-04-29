package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PJoint implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x100;

    public Thing a, b;
    public Vector3f aContact, bContact;
    public float length = 1.01f, angle, offsetTime;
    public boolean invertAngle;
    public ResourceDescriptor settings;
    public int[] boneIdx = new int[] { -1, -1 };
    public Vector4f boneLengths = new Vector4f(0.07920773f, 0.07920773f, 0.07920773f, 1.0f);
    public int type = 10;
    public float strength = 0.027f;

    @GsonRevision(min = 0x18d)
    public boolean stiff;
    @GsonRevision(min = 0x18d)
    public Vector3f slideDir;

    public int animationPattern;
    public float animationRange = 1.0f, animationTime = 60.0f,
        animationPhase, animationSpeed, animationPause;
    public float aAngleOffset, bAngleOffset;
    public float modStartFrames = -1.0f, modDeltaFrames, modScale = 1.0f;

    @GsonRevision(max = 0x2c3)
    @Deprecated
    public boolean modDriven;

    @GsonRevision(max = 0x306)
    @Deprecated
    public byte interactPlayMode = 0, interactEditMode = 2;

    public float renderScale = 1.0f;

    @GsonRevision(min = 0x16a)
    public int jointSoundEnum = 1;

    @GsonRevision(min = 0x21d)
    public float tweakTargetMaxLength, tweakTargetMinLength;

    @GsonRevision(min = 0x21f)
    public boolean currentlyEditing;

    @GsonRevision(min = 0x230, max = 0x2c3)
    @Deprecated
    public boolean modScaleActive;

    @GsonRevision(min = 0x25d)
    public boolean hideInPlayMode;

    @GsonRevision(min = 0x2c4)
    public int behaviour;

    @GsonRevision(lbp3 = true, min = 0xed)
    public Vector3f[] railKnotVector;

    @GsonRevision(lbp3 = true, min = 0x197)
    public byte railInteractions;

    @GsonRevision(lbp3 = true, min = 0x19f)
    public boolean canBeTweakedByPoppetPowerup, createdByPoppetPowerup;

    @GsonRevision(lbp3 = true, min = 0x1a4)
    public boolean oldJointOutputBehavior;

    // EJointPattern
    // WAVE 0
    // FORWARDS 1
    // FLIPPER 2

    // EJointType
    // LEGACY 0
    // ELASTIC 1
    // SPRING 2
    // CHAIN 3
    // PISTON 4
    // STRING 5
    // ROD 6
    // (START OF ANGULAR)
    // BOLT 7
    // SPRING_ANGULAR 8
    // MOTOR 9
    // QUANTIZED 10

    public PJoint() { }

    public PJoint(Thing a, Thing b)
    {
        this.a = a;
        this.b = b;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        a = serializer.reference(a, Thing.class);
        b = serializer.reference(b, Thing.class);
        aContact = serializer.v3(aContact);
        bContact = serializer.v3(bContact);
        length = serializer.f32(length);
        angle = serializer.f32(angle);
        offsetTime = serializer.f32(offsetTime);
        invertAngle = serializer.bool(invertAngle);
        settings = serializer.resource(settings, ResourceType.JOINT);
        boneIdx = serializer.intarray(boneIdx);
        boneLengths = serializer.v4(boneLengths);
        type = serializer.i32(type);
        strength = serializer.f32(strength);

        if (version < 0x18d)
            serializer.f32(0); // Unknown
        else
        {
            stiff = serializer.bool(stiff);
            slideDir = serializer.v3(slideDir);
        }

        animationPattern = serializer.i32(animationPattern);
        animationRange = serializer.f32(animationRange);
        animationTime = serializer.f32(animationTime);
        animationPhase = serializer.f32(animationPhase);
        animationSpeed = serializer.f32(animationSpeed);
        animationPause = serializer.f32(animationPause);

        aAngleOffset = serializer.f32(aAngleOffset);
        bAngleOffset = serializer.f32(bAngleOffset);

        modStartFrames = serializer.f32(modStartFrames);
        modDeltaFrames = serializer.f32(modDeltaFrames);
        modScale = serializer.f32(modScale);

        if (version < 0x2c4)
            modDriven = serializer.bool(modDriven);

        if (version < 0x307)
        {
            interactPlayMode = serializer.i8(interactPlayMode);
            interactEditMode = serializer.i8(interactEditMode);
        }

        renderScale = serializer.f32(renderScale);

        if (version > 0x169)
            jointSoundEnum = serializer.i32(jointSoundEnum);

        if (version > 0x280)
        {
            tweakTargetMaxLength = serializer.f32(tweakTargetMaxLength);
            tweakTargetMinLength = serializer.f32(tweakTargetMinLength);
        }
        else if (version > 0x21c)
        {
            tweakTargetMaxLength = serializer.i32(Math.round(tweakTargetMaxLength));
            tweakTargetMinLength = serializer.i32(Math.round(tweakTargetMinLength));
        }

        if (version > 0x21e)
            currentlyEditing = serializer.bool(currentlyEditing);

        if (version > 0x22f && version < 0x2c4)
            modScaleActive = serializer.bool(modScaleActive);

        if (version > 0x25c)
            hideInPlayMode = serializer.bool(hideInPlayMode);

        if (version > 0x2c3)
            behaviour = serializer.i32(behaviour);

        if (subVersion >= 0xed)
        {
            if (serializer.isWriting())
            {
                MemoryOutputStream stream = serializer.getOutput();
                if (railKnotVector != null)
                {
                    stream.i32(railKnotVector.length);
                    for (Vector3f vector : railKnotVector)
                        serializer.v3(vector);
                }
                else stream.i32(0);
            }
            else
            {
                MemoryInputStream stream = serializer.getInput();
                railKnotVector = new Vector3f[stream.i32()];
                for (int i = 0; i < railKnotVector.length; ++i)
                    railKnotVector[i] = stream.v3();
            }
        }

        if (subVersion >= 0x197)
            railInteractions = serializer.i8(railInteractions);

        if (subVersion >= 0x19f)
        {
            canBeTweakedByPoppetPowerup = serializer.bool(canBeTweakedByPoppetPowerup);
            createdByPoppetPowerup = serializer.bool(createdByPoppetPowerup);
        }

        if (subVersion >= 0x1a4)
            oldJointOutputBehavior = serializer.bool(oldJointOutputBehavior);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PJoint.BASE_ALLOCATION_SIZE;
        if (this.railKnotVector != null) size += (this.railKnotVector.length * 0xC);
        if (this.boneIdx != null) size += (this.boneIdx.length * 0x4);
        return size;
    }
}