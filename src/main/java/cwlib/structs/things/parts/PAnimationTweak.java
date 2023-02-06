package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

public class PAnimationTweak implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xB0;

    public float animSpeed = 1.0f;

    @GsonRevision(lbp3=true, min=0x1e)
    public float animPos = 0.0f;

    @GsonRevision(lbp3=true, min=0x25)
    public float animBlendTime = 0.0f;
    
    public int behavior;

    @GsonRevision(lbp3=true, min=0x25)
    public int blendAction;

    @GsonRevision(lbp3=true, min=0x1e)
    public ResourceDescriptor anim;

    @GsonRevision(lbp3=true, min=0x1e)
    public boolean animLoop = true;

    @GsonRevision(lbp3=true, min=0x20)
    public GUID cachedMeshFile = new GUID(1087);

    @GsonRevision(lbp3=true, min=0x21)
    public GUID containerFile = null;

    @GsonRevision(lbp3=true, min=0x21)
    public float animStart = 0.0f, animEnd = 1.0f;

    @GsonRevision(lbp3=true, min=0x26)
    public boolean animPlaying = true;

    @GsonRevision(lbp3=true, min=0x27)
    public boolean animResetOnInactive = true;

    @GsonRevision(lbp3=true, min=0x13f, max=0x143)
    @GsonRevision(lbp3=true, min=0x147)
    public float rootRotationX;

    @GsonRevision(lbp3=true, min=0x3a)
    public float rootRotationY;

    @GsonRevision(lbp3=true, min=0x3a)
    public boolean usesRootRotation = false;

    @GsonRevision(lbp3=true, min=0x43)
    public int type, animToOverride;

    @GsonRevision(lbp3=true, min=0x67)
    public GUID tweakMeshFile = new GUID(1087);

    @GsonRevision(lbp3=true, min=0x6a)
    public int containerType, subContainerIndex;

    @GsonRevision(lbp3=true, min=0x6b)
    public String actorName;

    @GsonRevision(lbp3=true, min=0x152)
    public float yawRate, pitchRate, rollRate;

    @GsonRevision(lbp3=true, min=0x152)
    public float yawPosition, pitchPosition, rollPosition;

    @GsonRevision(lbp3=true, min=0x152)
    public float rootRotationZ;

    @SuppressWarnings("unchecked")
    @Override public PAnimationTweak serialize(Serializer serializer, Serializable structure) {
        PAnimationTweak tweak = (structure == null) ? new PAnimationTweak() : (PAnimationTweak) structure;

        int subVersion = serializer.getRevision().getSubVersion();

        tweak.animSpeed = serializer.f32(tweak.animSpeed);
        if (subVersion > 0x1d)
            tweak.animPos = serializer.f32(tweak.animPos);

        if (subVersion >= 0x25)
            tweak.animBlendTime = serializer.f32(tweak.animBlendTime);
        tweak.behavior = serializer.i32(tweak.behavior);
        if (subVersion >= 0x25)
            tweak.blendAction = serializer.i32(tweak.blendAction);
        
        if (subVersion > 0x1d) {
            tweak.anim = serializer.resource(tweak.anim, ResourceType.ANIMATION);
            tweak.animLoop = serializer.bool(tweak.animLoop);
        }

        if (subVersion > 0x1f)
            tweak.cachedMeshFile = serializer.guid(tweak.cachedMeshFile);
        if (subVersion > 0x20) {
            tweak.containerFile = serializer.guid(tweak.containerFile);
            tweak.animStart = serializer.f32(tweak.animStart);
            tweak.animEnd = serializer.f32(tweak.animEnd);
        }

        if (subVersion > 0x25)
            tweak.animPlaying = serializer.bool(tweak.animPlaying);
        if (subVersion > 0x26)
            tweak.animResetOnInactive = serializer.bool(tweak.animResetOnInactive);

        if (subVersion > 0x146 || (subVersion > 0x13e && subVersion < 0x143))
            tweak.rootRotationX = serializer.f32(tweak.rootRotationX);
        
        if (subVersion > 0x39) {
            tweak.rootRotationY = serializer.f32(tweak.rootRotationY);
            tweak.usesRootRotation = serializer.bool(tweak.usesRootRotation);
        }

        if (subVersion > 0x42) {
            tweak.type = serializer.i32(tweak.type);
            tweak.animToOverride = serializer.i32(tweak.animToOverride);
        }

        if (subVersion >= 0x5c && subVersion < 0x152)
            serializer.u8(0);

        if (subVersion > 0x66)
            tweak.tweakMeshFile = serializer.guid(tweak.tweakMeshFile);
        
        if (subVersion > 0x69) {
            tweak.containerType = serializer.i32(tweak.containerType);
            tweak.subContainerIndex = serializer.i32(tweak.subContainerIndex);
        }

        if (subVersion > 0x6a)
            tweak.actorName = serializer.wstr(tweak.actorName);

        if (subVersion > 0x151) {
            tweak.yawRate = serializer.f32(tweak.yawRate);
            tweak.pitchRate = serializer.f32(tweak.pitchRate);
            tweak.rollRate = serializer.f32(tweak.rollRate);

            tweak.yawPosition = serializer.f32(tweak.yawPosition);
            tweak.pitchPosition = serializer.f32(tweak.pitchPosition);
            tweak.rollPosition = serializer.f32(tweak.rollPosition);
            
            tweak.rootRotationZ = serializer.f32(tweak.rootRotationZ);
        }

        return tweak;
    }

    @Override public int getAllocatedSize() {
        int size = PAnimationTweak.BASE_ALLOCATION_SIZE;
        if (this.actorName != null)
            size += (this.actorName.length() * 0x2);
        return size;
    }
}
