package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.npc.NpcBehavior;
import cwlib.structs.things.components.switches.SwitchSignal;

public class PSwitchInput implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    @Deprecated public SwitchSignal activation;
    @Deprecated public int updateFrame;
    public Thing dataSource;
    public int updateType, lethalType;
    public boolean includeRigidConnectors;
    public int lethalActivationFrame;
    public boolean lethalInverted, hideInPlayMode;
    @Deprecated public boolean oneShot;
    public boolean disableLethalAudio;
    public NpcBehavior sackbotBehavior;
    public int sackbotColor;
    public int sackbotObjectColorIndex;
    public int behavior;
    public int effectDestroy;
    public byte playerMode;

    @SuppressWarnings("unchecked")
    @Override public PSwitchInput serialize(Serializer serializer, Serializable structure) {
        PSwitchInput input = (structure == null) ? new PSwitchInput() : (PSwitchInput) structure;
        
        int version = serializer.getRevision().getVersion();

        if (version < 0x2c4) {
            input.activation = serializer.struct(input.activation, SwitchSignal.class);
            input.updateFrame = serializer.i32(input.updateFrame);
        }

        input.dataSource = serializer.thing(input.dataSource);

        if (version > 0x273)
            input.updateType = serializer.i32(input.updateType);
        if (version > 0x274)
            input.lethalType = serializer.i32(input.lethalType);

        if (version > 0x277 && version < 0x327)
            serializer.thing(null); // portThing

        if (version > 0x287)
            input.includeRigidConnectors = serializer.bool(input.includeRigidConnectors);

        if (version > 0x28a) {
            input.lethalActivationFrame = serializer.i32(input.lethalActivationFrame);
            input.lethalInverted = serializer.bool(input.lethalInverted);
        }

        if (version > 0x297)
            input.hideInPlayMode = serializer.bool(input.hideInPlayMode);

        if (version > 0x298 && version < 0x2c4)
            input.oneShot = serializer.bool(input.oneShot);

        if (version > 0x38f)
            input.disableLethalAudio = serializer.bool(input.disableLethalAudio);

        if (version > 0x29a)
            input.sackbotBehavior = serializer.reference(input.sackbotBehavior, NpcBehavior.class);

        if (version > 0x2aa && version < 0x2d5)
            serializer.i32(0); // sackbotColor
        
        if (version > 0x2d4)
            input.sackbotObjectColorIndex = serializer.s32(input.sackbotObjectColorIndex);
        if (version > 0x2c3)
            input.behavior = serializer.i32(input.behavior);
        if (version > 0x308)
            input.effectDestroy = serializer.i32(input.effectDestroy);
        if (version > 0x3ec)
            input.playerMode = serializer.i8(input.playerMode);
        
        return input;
    }

    @Override public int getAllocatedSize() {
        int size = PSwitchInput.BASE_ALLOCATION_SIZE;
        return size;
    }
}
