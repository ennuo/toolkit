package cwlib.structs.things.parts;

import org.joml.Matrix4f;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;

public class PControlinator implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x80;

    public Thing attachedPlayer, lastAttachedPlayer, prompt, promptPlayer;
    public int colorIndex;
    public float radius;
    public boolean sideMode;
    public int remoteControlState;
    public boolean disablePoppetControls, autoDock, overrideSackbot, killRiderOnCreatureDeath;
    public Thing padSwitch;
    public int parentBoneIndex;
    public Matrix4f parentBoneOffset;
    public byte playerMode, layerRange;

    @SuppressWarnings("unchecked")
    @Override public PControlinator serialize(Serializer serializer, Serializable structure) {
        PControlinator dc = (structure == null) ? new PControlinator() : (PControlinator) structure;

        int version = serializer.getRevision().getVersion();
        int subVersion = serializer.getRevision().getSubVersion();
        
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
