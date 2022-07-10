package cwlib.structs.things.parts;

import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
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
    public float length, angle, offsetTime;
    public boolean invertAngle;
    public ResourceDescriptor settings;
    public int[] boneIdx;
    public Vector4f boneLengths;
    public int type;
    public float strength;
    public boolean stiff;
    public Vector3f slideDir;
    public int animationPattern;
    public float animationRange, animationTime,
    animationPhase, animationSpeed, animationPause;
    public float aAngleOffset, bAngleOffset;
    public float modStartFrames, modDeltaFrames, modScale;
    public boolean modDriven;
    public byte interactPlayMode, interactEditMode;
    public float renderScale;
    public int jointSoundEnum;
    public float tweakTargetMaxLength, tweakTargetMinLength;
    public boolean currentlyEditing;
    public boolean modScaleActive, hideInPlayMode;
    public int behaviour;
    public Vector3f[] railKnotVector;
    public byte railInteractions;
    public boolean canBeTweakedByPoppetPowerup, createdByPoppetPowerup;
    public boolean oldJointOutputBehavior;

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