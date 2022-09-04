package cwlib.structs.things.parts;

import org.joml.Matrix4f;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.types.data.Revision;

public class PControlinator implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public Thing attachedPlayer, lastAttachedPlayer, prompt, promptPlayer;
    public int colorIndex;
    public float radius;
    public boolean sideMode;
    public int remoteControlState;
    public boolean disablePoppetControls, autoDock, overrideSackbot;
    @GsonRevision(lbp3=true,min=0x6f) public boolean killRiderOnCreatureDeath;
    public Thing padSwitch;

    @GsonRevision(lbp3=true,min=0x4c) public int parentBoneIndex;
    @GsonRevision(lbp3=true,min=0x4c) public Matrix4f parentBoneOffset;

    @GsonRevision(min=0x3ed) public byte playerMode;
    @GsonRevision(lbp3=true,min=0x18a) public byte layerRange;

    /* Vita */
    @GsonRevision(min=0xd, branch=0x4431) public float tiltMax;
    @GsonRevision(min=0xe, branch=0x4431) public float tiltMin;
    @GsonRevision(min=0x1d, branch=0x4431) public int remotePlayer;
    @GsonRevision(min=0x51, branch=0x4431) public boolean playAudio;

    @SuppressWarnings("unchecked")
    @Override public PControlinator serialize(Serializer serializer, Serializable structure) {
        PControlinator dc = (structure == null) ? new PControlinator() : (PControlinator) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();
        
        dc.attachedPlayer = serializer.thing(dc.attachedPlayer);
        dc.lastAttachedPlayer = serializer.thing(dc.lastAttachedPlayer);
        dc.prompt = serializer.thing(dc.prompt);
        dc.promptPlayer = serializer.thing(dc.promptPlayer);

        dc.colorIndex = serializer.i32(dc.colorIndex);

        dc.radius = serializer.f32(dc.radius);
        dc.sideMode = serializer.bool(dc.sideMode);

        dc.remoteControlState = serializer.i32(dc.remoteControlState);

        dc.disablePoppetControls = serializer.bool(dc.disablePoppetControls);
        dc.autoDock = serializer.bool(dc.autoDock);
        dc.overrideSackbot = serializer.bool(dc.overrideSackbot);
        if (subVersion > 0x6e)
            dc.killRiderOnCreatureDeath = serializer.bool(dc.killRiderOnCreatureDeath);
        
        dc.padSwitch = serializer.thing(dc.padSwitch);

        if (revision.isVita()) {
            int vita = revision.getBranchRevision();
            if (vita >= 0xd) // 0x3c0
                dc.tiltMax = serializer.f32(dc.tiltMax);
            if (vita >= 0xe) // 0x3c0
                dc.tiltMin = serializer.f32(dc.tiltMin);
            if (vita >= 0x1d) // 0x3d4
                dc.remotePlayer = serializer.i32(dc.remotePlayer);
            if (vita >= 0x51) // 0x3e1
                dc.playAudio = serializer.bool(dc.playAudio);
        }

        if (subVersion > 0x4b) {
            dc.parentBoneIndex = serializer.i32(dc.parentBoneIndex);
            dc.parentBoneOffset = serializer.m44(dc.parentBoneOffset);
        }

        if (version > 0x3ec)
            dc.playerMode = serializer.i8(dc.playerMode);
        if (subVersion > 0x189)
            dc.layerRange = serializer.i8(dc.layerRange);
        
        return dc;
    }

    @Override public int getAllocatedSize() { return PControlinator.BASE_ALLOCATION_SIZE; }
}
