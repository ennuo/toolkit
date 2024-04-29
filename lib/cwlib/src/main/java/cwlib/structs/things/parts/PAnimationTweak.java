package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;

public class PAnimationTweak implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0xB0;

    public float animSpeed = 1.0f;

    @GsonRevision(lbp3 = true, min = 0x1e)
    public float animPos = 0.0f;

    @GsonRevision(lbp3 = true, min = 0x25)
    public float animBlendTime = 0.0f;

    public int behavior;

    @GsonRevision(lbp3 = true, min = 0x25)
    public int blendAction;

    @GsonRevision(lbp3 = true, min = 0x1e)
    public ResourceDescriptor anim;

    @GsonRevision(lbp3 = true, min = 0x1e)
    public boolean animLoop = true;

    @GsonRevision(lbp3 = true, min = 0x20)
    public GUID cachedMeshFile = new GUID(1087);

    @GsonRevision(lbp3 = true, min = 0x21)
    public GUID containerFile = null;

    @GsonRevision(lbp3 = true, min = 0x21)
    public float animStart = 0.0f, animEnd = 1.0f;

    @GsonRevision(lbp3 = true, min = 0x26)
    public boolean animPlaying = true;

    @GsonRevision(lbp3 = true, min = 0x27)
    public boolean animResetOnInactive = true;

    @GsonRevision(lbp3 = true, min = 0x13f, max = 0x143)
    @GsonRevision(lbp3 = true, min = 0x147)
    public float rootRotationX;

    @GsonRevision(lbp3 = true, min = 0x3a)
    public float rootRotationY;

    @GsonRevision(lbp3 = true, min = 0x3a)
    public boolean usesRootRotation = false;

    @GsonRevision(lbp3 = true, min = 0x43)
    public int type, animToOverride;

    @GsonRevision(lbp3 = true, min = 0x67)
    public GUID tweakMeshFile = new GUID(1087);

    @GsonRevision(lbp3 = true, min = 0x6a)
    public int containerType, subContainerIndex;

    @GsonRevision(lbp3 = true, min = 0x6b)
    public String actorName;

    @GsonRevision(lbp3 = true, min = 0x152)
    public float yawRate, pitchRate, rollRate;

    @GsonRevision(lbp3 = true, min = 0x152)
    public float yawPosition, pitchPosition, rollPosition;

    @GsonRevision(lbp3 = true, min = 0x152)
    public float rootRotationZ;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        animSpeed = serializer.f32(animSpeed);
        if (subVersion > 0x1d)
            animPos = serializer.f32(animPos);

        if (subVersion >= 0x25)
            animBlendTime = serializer.f32(animBlendTime);
        behavior = serializer.i32(behavior);
        if (subVersion >= 0x25)
            blendAction = serializer.i32(blendAction);

        if (subVersion > 0x1d)
        {
            anim = serializer.resource(anim, ResourceType.ANIMATION);
            animLoop = serializer.bool(animLoop);
        }

        if (subVersion > 0x1f)
            cachedMeshFile = serializer.guid(cachedMeshFile);
        if (subVersion > 0x20)
        {
            containerFile = serializer.guid(containerFile);
            animStart = serializer.f32(animStart);
            animEnd = serializer.f32(animEnd);
        }

        if (subVersion > 0x25)
            animPlaying = serializer.bool(animPlaying);
        if (subVersion > 0x26)
            animResetOnInactive = serializer.bool(animResetOnInactive);

        if (subVersion > 0x146 || (subVersion > 0x13e && subVersion < 0x143))
            rootRotationX = serializer.f32(rootRotationX);

        if (subVersion > 0x39)
        {
            rootRotationY = serializer.f32(rootRotationY);
            usesRootRotation = serializer.bool(usesRootRotation);
        }

        if (subVersion > 0x42)
        {
            type = serializer.i32(type);
            animToOverride = serializer.i32(animToOverride);
        }

        if (subVersion >= 0x5c && subVersion < 0x152)
            serializer.u8(0);

        if (subVersion > 0x66)
            tweakMeshFile = serializer.guid(tweakMeshFile);

        if (subVersion > 0x69)
        {
            containerType = serializer.i32(containerType);
            subContainerIndex = serializer.i32(subContainerIndex);
        }

        if (subVersion > 0x6a)
            actorName = serializer.wstr(actorName);

        if (subVersion > 0x151)
        {
            yawRate = serializer.f32(yawRate);
            pitchRate = serializer.f32(pitchRate);
            rollRate = serializer.f32(rollRate);

            yawPosition = serializer.f32(yawPosition);
            pitchPosition = serializer.f32(pitchPosition);
            rollPosition = serializer.f32(rollPosition);

            rootRotationZ = serializer.f32(rootRotationZ);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = PAnimationTweak.BASE_ALLOCATION_SIZE;
        if (this.actorName != null)
            size += (this.actorName.length() * 0x2);
        return size;
    }
}
