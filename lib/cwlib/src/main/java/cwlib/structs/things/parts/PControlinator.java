package cwlib.structs.things.parts;

import org.joml.Matrix4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class PControlinator implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public Thing attachedPlayer, lastAttachedPlayer, prompt, promptPlayer;
    public int colorIndex;
    public float radius;
    public boolean sideMode;
    public int remoteControlState;
    public boolean disablePoppetControls, autoDock, overrideSackbot;
    @GsonRevision(lbp3 = true, min = 0x6f)
    public boolean killRiderOnCreatureDeath;
    public Thing padSwitch;

    @GsonRevision(lbp3 = true, min = 0x4c)
    public byte parentBoneIndex;
    @GsonRevision(lbp3 = true, min = 0x4c)
    public Matrix4f parentBoneOffset;

    @GsonRevision(min = 0x3ed)
    public byte playerMode;
    @GsonRevision(lbp3 = true, min = 0x18a)
    public byte layerRange;

    /* Vita */
    @GsonRevision(min = 0xd, branch = 0x4431)
    public float tiltMax;
    @GsonRevision(min = 0xe, branch = 0x4431)
    public float tiltMin;
    @GsonRevision(min = 0x1d, branch = 0x4431)
    public int remotePlayer;
    @GsonRevision(min = 0x51, branch = 0x4431)
    public boolean playAudio;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        attachedPlayer = serializer.thing(attachedPlayer);
        lastAttachedPlayer = serializer.thing(lastAttachedPlayer);
        prompt = serializer.thing(prompt);
        promptPlayer = serializer.thing(promptPlayer);

        colorIndex = serializer.i32(colorIndex);

        radius = serializer.f32(radius);
        sideMode = serializer.bool(sideMode);

        remoteControlState = serializer.i32(remoteControlState);

        disablePoppetControls = serializer.bool(disablePoppetControls);
        autoDock = serializer.bool(autoDock);
        overrideSackbot = serializer.bool(overrideSackbot);
        if (subVersion > 0x6e)
            killRiderOnCreatureDeath = serializer.bool(killRiderOnCreatureDeath);

        padSwitch = serializer.thing(padSwitch);

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();
            if (vita >= 0xd) // 0x3c0
                tiltMax = serializer.f32(tiltMax);
            if (vita >= 0xe) // 0x3c0
                tiltMin = serializer.f32(tiltMin);
            if (vita >= 0x1d) // 0x3d4
                remotePlayer = serializer.i32(remotePlayer);
            if (vita >= 0x51) // 0x3e1
                playAudio = serializer.bool(playAudio);
        }

        if (subVersion > 0x4b)
        {
            parentBoneIndex = serializer.i8(parentBoneIndex);
            parentBoneOffset = serializer.m44(parentBoneOffset);
        }

        if (version > 0x3ec)
            playerMode = serializer.i8(playerMode);
        if (subVersion > 0x189)
            layerRange = serializer.i8(layerRange);
    }

    @Override
    public int getAllocatedSize()
    {
        return PControlinator.BASE_ALLOCATION_SIZE;
    }
}
