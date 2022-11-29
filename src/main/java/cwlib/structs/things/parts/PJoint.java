package cwlib.structs.things.parts;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PJoint implements Serializable {
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

    @GsonRevision(min=0x18d)
    public boolean stiff;
    @GsonRevision(min=0x18d)
    public Vector3f slideDir;
    
    public int animationPattern;
    public float animationRange = 1.0f, animationTime = 60.0f,
    animationPhase, animationSpeed, animationPause;
    public float aAngleOffset, bAngleOffset;
    public float modStartFrames = -1.0f, modDeltaFrames, modScale = 1.0f;

    @GsonRevision(max=0x2c3)
    @Deprecated public boolean modDriven;

    @GsonRevision(max=0x306)
    @Deprecated public byte interactPlayMode, interactEditMode;
    
    public float renderScale = 1.0f;

    @GsonRevision(min=0x16a)
    public int jointSoundEnum = 1;
    
    @GsonRevision(min=0x21d)
    public float tweakTargetMaxLength, tweakTargetMinLength;

    @GsonRevision(min=0x21f)
    public boolean currentlyEditing;

    @GsonRevision(min=0x230,max=0x2c3)
    @Deprecated public boolean modScaleActive;
    
    @GsonRevision(min=0x25d)
    public boolean hideInPlayMode;

    @GsonRevision(min=0x2c4)
    public int behaviour;

    @GsonRevision(lbp3=true,min=0xed)
    public Vector3f[] railKnotVector;

    @GsonRevision(lbp3=true,min=0x197)
    public byte railInteractions;

    @GsonRevision(lbp3=true,min=0x19f)
    public boolean canBeTweakedByPoppetPowerup, createdByPoppetPowerup;

    @GsonRevision(lbp3=true,min=0x1a4)
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

    public PJoint() {};
    public PJoint(Thing a, Thing b) {
        this.a = a;
        this.b = b;
    }

    @SuppressWarnings("unchecked")
    @Override public PJoint serialize(Serializer serializer, Serializable structure) {
        PJoint joint = (structure == null) ? new PJoint() : (PJoint) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        joint.a = serializer.reference(joint.a, Thing.class);
        joint.b = serializer.reference(joint.b, Thing.class);
        joint.aContact = serializer.v3(joint.aContact);
        joint.bContact = serializer.v3(joint.bContact);
        joint.length = serializer.f32(joint.length);
        joint.angle = serializer.f32(joint.angle);
        joint.offsetTime = serializer.f32(joint.offsetTime);
        joint.invertAngle = serializer.bool(joint.invertAngle);
        joint.settings = serializer.resource(joint.settings, ResourceType.JOINT);
        joint.boneIdx = serializer.intarray(joint.boneIdx);
        joint.boneLengths = serializer.v4(joint.boneLengths);
        joint.type = serializer.i32(joint.type);
        joint.strength = serializer.f32(joint.strength);

        if (version < 0x18d)
            serializer.f32(0); // Unknown
        else {
            joint.stiff = serializer.bool(joint.stiff);
            joint.slideDir = serializer.v3(joint.slideDir);
        }

        joint.animationPattern = serializer.i32(joint.animationPattern);
        joint.animationRange = serializer.f32(joint.animationRange);
        joint.animationTime = serializer.f32(joint.animationTime);
        joint.animationPhase = serializer.f32(joint.animationPhase);
        joint.animationSpeed = serializer.f32(joint.animationSpeed);
        joint.animationPause = serializer.f32(joint.animationPause);

        joint.aAngleOffset = serializer.f32(joint.aAngleOffset);
        joint.bAngleOffset = serializer.f32(joint.bAngleOffset);

        joint.modStartFrames = serializer.f32(joint.modStartFrames);
        joint.modDeltaFrames = serializer.f32(joint.modDeltaFrames);
        joint.modScale = serializer.f32(joint.modScale);

        if (version < 0x2c4)
            joint.modDriven = serializer.bool(joint.modDriven);
        
        if (version < 0x307) {
            joint.interactPlayMode = serializer.i8(joint.interactPlayMode);
            joint.interactEditMode = serializer.i8(joint.interactEditMode);
        }

        joint.renderScale = serializer.f32(joint.renderScale);

        if (version > 0x169)
            joint.jointSoundEnum = serializer.i32(joint.jointSoundEnum);

        if (version > 0x280) {
            joint.tweakTargetMaxLength = serializer.f32(joint.tweakTargetMaxLength);
            joint.tweakTargetMinLength = serializer.f32(joint.tweakTargetMinLength);
        } else if (version > 0x21c) {
            joint.tweakTargetMaxLength = serializer.i32(Math.round(joint.tweakTargetMaxLength));
            joint.tweakTargetMinLength = serializer.i32(Math.round(joint.tweakTargetMinLength));
        }

        if (version > 0x21e)
            joint.currentlyEditing = serializer.bool(joint.currentlyEditing);

        if (version > 0x22f && version < 0x2c4)
            joint.modScaleActive = serializer.bool(joint.modScaleActive);

        if (version > 0x25c)
            joint.hideInPlayMode = serializer.bool(joint.hideInPlayMode);

        if (version > 0x2c3)
            joint.behaviour = serializer.i32(joint.behaviour);

        if (subVersion >= 0xed) {
            if (serializer.isWriting()) {
                MemoryOutputStream stream = serializer.getOutput();
                if (joint.railKnotVector != null) {
                    stream.i32(joint.railKnotVector.length);
                    for (Vector3f vector : joint.railKnotVector)
                        serializer.v3(vector);
                } else stream.i32(0);
            } else {
                MemoryInputStream stream = serializer.getInput();
                joint.railKnotVector = new Vector3f[stream.i32()];
                for (int i = 0; i < joint.railKnotVector.length; ++i)
                    joint.railKnotVector[i] = stream.v3();
            }
        }

        if (subVersion >= 0x197)
            joint.railInteractions = serializer.i8(joint.railInteractions);

        if (subVersion >= 0x19f) {
            joint.canBeTweakedByPoppetPowerup = serializer.bool(joint.canBeTweakedByPoppetPowerup);
            joint.createdByPoppetPowerup = serializer.bool(joint.createdByPoppetPowerup);
        }

        if (subVersion >= 0x1a4)
            joint.oldJointOutputBehavior = serializer.bool(joint.oldJointOutputBehavior);

        return joint;
    }
    
    @Override public int getAllocatedSize() {
        int size = PJoint.BASE_ALLOCATION_SIZE;
        if (this.railKnotVector != null) size += (this.railKnotVector.length * 0xC);
        if (this.boneIdx != null) size += (this.boneIdx.length * 0x4);
        return size; 
    }
}