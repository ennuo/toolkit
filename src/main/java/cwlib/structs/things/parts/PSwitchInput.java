package cwlib.structs.things.parts;

import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.npc.NpcBehavior;
import cwlib.structs.things.components.switches.SwitchSignal;

public class PSwitchInput implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    @GsonRevision(max=0x2c3) @Deprecated public SwitchSignal activation;
    @GsonRevision(max=0x2c3) @Deprecated public int updateFrame;
    public Thing dataSource;
    @GsonRevision(min=0x274) public int updateType;
    @GsonRevision(min=0x275) public int lethalType;
    @GsonRevision(min=0x288) public boolean includeRigidConnectors;
    @GsonRevision(min=0x28b) public int lethalActivationFrame;
    @GsonRevision(min=0x28b) public boolean lethalInverted;
    @GsonRevision(min=0x298) public boolean hideInPlayMode;
    
    @GsonRevision(min=0x299, max=0x2c3)
    @Deprecated public boolean oneShot;

    @GsonRevision(min=0x390) public boolean disableLethalAudio;

    @GsonRevision(min=0x29b) public NpcBehavior sackbotBehavior;

    @GsonRevision(min=0x2ab, max=0x2d4)
    @Deprecated public int sackbotColor;
    
    @GsonRevision(min=0x2d5) public int sackbotObjectColorIndex;
    @GsonRevision(min=0x2c4) public int behavior;
    @GsonRevision(min=0x309) public int effectDestroy;
    @GsonRevision(min=0x3ed) public byte playerMode;

    @SuppressWarnings("unchecked")
    @Override public PSwitchInput serialize(Serializer serializer, Serializable structure) {
        PSwitchInput input = (structure == null) ? new PSwitchInput() : (PSwitchInput) structure;
        
        int version = serializer.getRevision().getVersion();

        if (version < 0x2c4) {
            input.activation = serializer.struct(input.activation, SwitchSignal.class);
            input.updateFrame = serializer.i32(input.updateFrame);
        }

        input.dataSource = serializer.thing(input.dataSource);

        // ESwitchInputUpdateType
        // ACTIVATION 0
        // DESTROY 1
        // LETHAL 2
        // REGISTER_COUNT 3
        // PHYSICS_TWEAK_X 4
        // PHYSICS_TWEAK_Y 5
        // PHYSICS_TWEAK_ROTATION 6
        // CIRCUITBOARD 7
        // SIGNED_ACTIVATION 8
        // TIMER_ACTIVATION 9
        // SACKBOT_BEHAVIOR 10
        // SACKBOT_WAYPOINT 11
        // SACKBOT_LOOKAT 12
        // SACKBOT_JUMP 13
        // SACKBOT_GRAB 14
        // SACKBOT_LAYERCHANGE 15


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
            input.sackbotColor = serializer.i32(input.sackbotColor);
        
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
