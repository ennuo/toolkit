package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.things.components.switches.SwitchSignal;
import cwlib.types.data.Revision;

import org.joml.Vector3f;

public class PPhysicsTweak implements Serializable {
    @Deprecated public float activation;
    public float tweakGravity;
    public float tweakBuoyancy;
    public Vector3f tweakDampening;
    public Vector3f input;
    public Vector3f middleVel;
    public float velRange;
    public float accelStrength;
    public float decelStrength;
    public byte directionModifier;
    public byte movementModifier;
    public boolean localSpace;
    public int configuration;
    public boolean hideInPlayMode;
    public int colorIndex;
    public String name;
    public int followerPlayerMode;
    @Deprecated public int teamFilter;
    public int behavior;
    public boolean allowInOut, allowUpDown;
    public float minRange, maxRange;
    public boolean followKey;
    public float angleRange;
    public boolean flee, stabiliser;
    public boolean shardDephysicalised, shardPhysicsAudio;
    public boolean isLBP2PhysicsTweak;
    public float maximumMass;
    public boolean canPush;
    public int zBehavior;
    public float lastKnownActivation;
    public boolean waitingToMove;

    public byte zPhase;
    public short gridSnap, gridStrength;
    public float gridGoalW;
    public Vector3f gridGoal;

    /* Vita */
    public int usePanel, followType;
    public float followerDeceleration;
    public byte playerFilter;

    @SuppressWarnings("unchecked")
    @Override public PPhysicsTweak serialize(Serializer serializer, Serializable structure) {
        PPhysicsTweak tweak = (structure == null) ? new PPhysicsTweak() : (PPhysicsTweak) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x2c4) 
            tweak.activation = serializer.f32(tweak.activation);

        tweak.tweakGravity = serializer.f32(tweak.tweakGravity);
        if (version > 0x2ff)
            tweak.tweakBuoyancy = serializer.f32(tweak.tweakBuoyancy);

        if (version < 0x281)
            serializer.f32(0);
        
        if (version > 0x280)
            tweak.tweakDampening = serializer.v3(tweak.tweakDampening);
        tweak.input = serializer.v3(tweak.input);
        tweak.middleVel = serializer.v3(tweak.middleVel);

        if (version < 0x281)
            serializer.v3(null);
        if (version > 0x280)
            tweak.velRange = serializer.f32(tweak.velRange);
        if (version < 0x281)
            serializer.v3(null);

        if (version > 0x280) {
            tweak.accelStrength = serializer.f32(tweak.accelStrength);
            if (version > 0x285)
                tweak.decelStrength = serializer.f32(tweak.decelStrength);
        }

        if (version < 0x281) {
            serializer.v3(null);
            serializer.i32(0);
        }

        if (subVersion > 0xa3) {
            tweak.directionModifier = serializer.i8(tweak.directionModifier);
            tweak.movementModifier = serializer.i8(tweak.movementModifier);
        }

        tweak.localSpace = serializer.bool(tweak.localSpace);

        if (!serializer.isWriting() && version < 0x38a && tweak.velRange != 0.0f) {
            tweak.accelStrength = tweak.accelStrength / tweak.velRange;
            tweak.decelStrength = tweak.decelStrength / tweak.velRange;
        }

        if (version > 0x278)
            tweak.configuration = serializer.i32(tweak.configuration);

        if (version >= 0x27a && version < 0x327)
            serializer.thing(null);

        if (version > 0x282) 
            tweak.hideInPlayMode = serializer.bool(tweak.hideInPlayMode);
        
        if (version > 0x28d)
            tweak.colorIndex = serializer.i32(tweak.colorIndex);
        if (version > 0x2db)
            tweak.name = serializer.wstr(tweak.name);

        if (version < 0x3e8) {
            if (version >= 0x2ae)
                tweak.teamFilter = serializer.i32(tweak.teamFilter);
        } else {
            if (subVersion < 0x132 && version >= 0x2ae)
                tweak.teamFilter = serializer.i32(tweak.teamFilter);
            tweak.followerPlayerMode = serializer.i32(tweak.followerPlayerMode);   
        }

        if (version > 0x2c5)
            tweak.behavior = serializer.i32(tweak.behavior);
        if (version > 0x2dc) {
            tweak.allowInOut = serializer.bool(tweak.allowInOut);
            tweak.allowUpDown = serializer.bool(tweak.allowUpDown);
            tweak.minRange = serializer.f32(tweak.minRange);
            tweak.maxRange = serializer.f32(tweak.maxRange);
            tweak.followKey = serializer.bool(tweak.followKey);
        }

        if (version > 0x381)
            tweak.angleRange = serializer.f32(tweak.angleRange);

        if (version > 0x2fe)
            tweak.flee = serializer.bool(tweak.flee);

        if (subVersion > 0x6f)
            tweak.stabiliser = serializer.bool(tweak.stabiliser);
        if (subVersion > 0x12f) {
            tweak.shardDephysicalised = serializer.bool(tweak.shardDephysicalised);
            tweak.shardPhysicsAudio = serializer.bool(tweak.shardPhysicsAudio);
        }
        if (subVersion > 0x43)
            tweak.isLBP2PhysicsTweak = serializer.bool(tweak.isLBP2PhysicsTweak);

        if (version > 0x36a) {
            tweak.maximumMass = serializer.f32(tweak.maximumMass);
            tweak.canPush = serializer.bool(tweak.canPush);
            tweak.zBehavior = serializer.s32(tweak.zBehavior);
            tweak.lastKnownActivation = serializer.f32(tweak.lastKnownActivation);
            tweak.waitingToMove = serializer.bool(tweak.waitingToMove);
        }

        if (subVersion > 0xf6)
            tweak.zPhase = serializer.i8(tweak.zPhase);

        // move recording, again will figure out the fields later
        if (version > 0x3b8 && tweak.configuration == 0xd) {
            serializer.resource(null, ResourceType.THING_RECORDING);
            serializer.f32(0);
            if (version < 0x3c4) serializer.u8(0);
            serializer.u8(0);
            serializer.u8(0);
            serializer.v3(null);
            serializer.u8(0);
            serializer.m44(null);
            serializer.f32(0);
            if (version > 0x3c4)
                serializer.u8(0);
        }

        if (revision.isVita()) { 
            int vita = revision.getBranchRevision();
            if (vita >= 0x11) // 0x3c0
                tweak.usePanel = serializer.i32(tweak.usePanel);
            if (vita >= 0x11 && vita < 0x4b) serializer.u8(0); // 0x3c0
            if (vita >= 0x1f) // 0x3d4
                tweak.followType = serializer.i32(tweak.followType);
            if (vita >= 0x4b)
                tweak.followerDeceleration = serializer.f32(tweak.followerDeceleration);
            if (vita >= 0x55)
                tweak.playerFilter = serializer.i8(tweak.playerFilter);
        }
        

        if (subVersion >= 0x1 && subVersion < 0x17) {
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
        }

        if (subVersion >= 0x5 && subVersion < 0x17) {
            serializer.f32(0);
            serializer.u8(0);
            serializer.u8(0);
        }

        // attract-o-gel, ill figure out the fields later
        if (tweak.configuration == 0xe && version > 0x3e3) {
            serializer.f32(0);
            serializer.i32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.i32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.u8(0);
            serializer.u8(0);
            serializer.u8(0);
            serializer.u8(0);
        }

        if (subVersion > 0x56) {
            tweak.gridSnap = serializer.i16(tweak.gridSnap);
            tweak.gridStrength = serializer.i16(tweak.gridStrength);
        }

        if (subVersion > 0x99)
            tweak.gridGoalW = serializer.f32(tweak.gridGoalW);
        
        if (subVersion > 0x98)
            tweak.gridGoal = serializer.v3(tweak.gridGoal);
        

        return tweak;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
}
