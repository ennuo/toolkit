package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.Branch;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PCreature implements Serializable {
    public static class SpringData implements Serializable {
        public static final int BASE_ALLOCATION_SIZE = 0x30;

        public Thing springThing;
        public int springTimer;
        public Vector3f springDirection, springThingPosition;

        @SuppressWarnings("unchecked")
        @Override public SpringData serialize(Serializer serializer, Serializable structure) {
            SpringData data = (structure == null) ? new SpringData() : (SpringData) structure;

            data.springThing = serializer.thing(data.springThing);
            data.springTimer = serializer.i32(data.springTimer);
            data.springDirection = serializer.v3(data.springDirection);
            data.springThingPosition = serializer.v3(data.springThingPosition);

            return data;
        }

        @Override public int getAllocatedSize() { return SpringData.BASE_ALLOCATION_SIZE; }
    }

    public ResourceDescriptor config;
    public int jumpFrame;
    public float groundDistance;
    public Vector3f groundNormal;
    public Thing grabJoint, jumpingOff;
    public int state, subState, stateTimer;
    public float speedModifier, jumpModifier, strengthModifier;
    public int zMode, playerAwareness, moveDirection;
    public Vector3f forceThatSmashedCreature;
    public int crushFrames;
    public float awarenessRadius;
    public int airTime;
    public int[] bouncepadThingUIDs, grabbedThingUIDs;
    public boolean haveNotTouchedGroundSinceUsingJetpack;
    public Thing[] legList, lifeSourceList;
    public Thing lifeCreature, aiCreature;
    public int jumpInterval, jumpIntervalPhase;
    public boolean meshDirty;
    public Thing[] eyeList, brainAiList, brainLifeList;
    public boolean reactToLethal;
    public Matrix4f oldAnimMatrix;
    public float animOffset;
    public Vector3f groundNormalRaw, groundNormalSmooth;
    public float bodyAdjustApplied, switchScale;
    public Vector3f gunDirAndDashVec;
    public int gunDirAndDashVecW;
    public Thing resourceThing;
    public int gunFireFrame, bulletCount, bulletImmuneTimer;
    public Thing bulletEmitter0, bulletEmitter1;
    public int bulletPosIndex, maxBulletCount;
    public float ammoFillFactor;
    public boolean gunDirPrecisionMode;
    public int fireRate;
    public float gunAccuracy;
    public Vector3f bulletEmitOffset;
    public float bulletEmitRotation;
    public Thing gunThing, gunTrigger;
    public int lastGunTriggerUID, airTimeLeft;
    public float amountBodySubmerged, amountHeadSubmerged;
    public boolean hasScubaGear;
    @Deprecated public ResourceDescriptor headPiece;
    public boolean outOfWaterJumpBoost;
    public ResourceDescriptor handPiece;
    public Thing head, toolTetherJoint;
    public float toolTetherWidth;
    public Thing jetpack;
    public int wallJumpDir;
    public Vector3f wallJumpPos;
    public Vector3f[] bootContactForceList;
    public int gunType;
    public boolean wallJumpMat;
    @Deprecated public Thing lastDirectControlPrompt;
    public Thing directControlPrompt;
    @Deprecated public Vector3f smoothedDirectControlStick;
    @Deprecated public short directControlAnimFrame;
    @Deprecated public byte directControlAnimState;
    @Deprecated public byte directControlMode;
    public int responsiblePlayer, responsibleFramesLeft;
    public boolean canDropPowerup;
    public byte capeExtraMaxVelocityCap;
    public int behavior, effectDestroy = 6;
    public Thing whipSim, alternateFormWorld;
    public int hookHatState;
    public Thing hookHatBogey;
    public int flyingState, flyingTimer, flyingFlumpFrame,
    flyingImpulseFrame, flyingFlapButtonTimer, flyingBrakeTimer,
    flyingGrabFallTimer;
    public float flyingLegScale;
    public Vector4f flyingVels;
    public boolean flyingFlapLockout, flyingFallLockout, flyingInWind, flyingThrustLatched;
    public short glidingTime;
    public byte springState;
    public boolean springHasSprung;
    public SpringData currentSpringData;
    public byte springPower;
    public boolean springSeparateForces;
    public byte springForce, springStateTimer;

    @SuppressWarnings("unchecked")
    @Override public PCreature serialize(Serializer serializer, Serializable structure) {
        PCreature creature = (structure == null) ? new PCreature() : (PCreature) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        creature.config = serializer.resource(creature.config, ResourceType.SETTINGS_CHARACTER);
        if (version < 0x155) {
            if (serializer.isWriting()) serializer.getOutput().i32(0);
            else {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                for (int i = 0; i < count; ++i)
                    stream.v3();
            }
        }

        creature.jumpFrame = serializer.s32(creature.jumpFrame);

        creature.groundDistance = serializer.f32(creature.groundDistance);
        creature.groundNormal = serializer.v3(creature.groundNormal);

        creature.grabJoint = serializer.thing(creature.grabJoint);
        if (version < 0x13c) serializer.thing(null);
        creature.jumpingOff = serializer.thing(creature.jumpingOff);

        creature.state = serializer.i32(creature.state);
        if (subVersion >= 0x132)
            creature.subState = serializer.i32(creature.subState);
        creature.stateTimer = serializer.i32(creature.stateTimer);

        creature.speedModifier = serializer.f32(creature.speedModifier);
        creature.jumpModifier = serializer.f32(creature.jumpModifier);
        creature.strengthModifier = serializer.f32(creature.strengthModifier);

        if (version < 0x142) serializer.v4(null);

        creature.zMode = serializer.i32(creature.zMode);

        if (version < 0x146) serializer.i32(0);
        if (version > 0x145 && version < 0x1f0) {
            serializer.i32(0);
            serializer.i32(0);
            serializer.bool(false);
            serializer.bool(false);
            serializer.i32(0);
        }

        creature.playerAwareness = serializer.s32(creature.playerAwareness);
        creature.moveDirection = serializer.s32(creature.moveDirection);

        if (version < 0x1f0) serializer.f32(0);

        creature.forceThatSmashedCreature = serializer.v3(creature.forceThatSmashedCreature);
        creature.crushFrames = serializer.i32(creature.crushFrames);

        creature.awarenessRadius = serializer.f32(creature.awarenessRadius);

        if (version >= 0x1df)
            creature.airTime = serializer.i32(creature.airTime);
        if (version >= 0x354) {
            creature.bouncepadThingUIDs = serializer.intarray(creature.bouncepadThingUIDs);
            creature.grabbedThingUIDs = serializer.intarray(creature.grabbedThingUIDs);
        }

        if (version >= 0x221)
            creature.haveNotTouchedGroundSinceUsingJetpack = serializer.bool(creature.haveNotTouchedGroundSinceUsingJetpack);

        if (version >= 0x15d) {
            creature.legList = serializer.thingarray(creature.legList);
            creature.lifeSourceList = serializer.thingarray(creature.lifeSourceList);
            creature.lifeCreature = serializer.thing(creature.lifeCreature);
            creature.aiCreature = serializer.thing(creature.aiCreature);
        }

        if (version >= 0x163) {
            creature.jumpInterval = serializer.i32(creature.jumpInterval);
            creature.jumpIntervalPhase = serializer.i32(creature.jumpIntervalPhase);
        }

        if (0x162 < version && version < 0x16d) serializer.bool(false);

        if (version >= 0x169) creature.meshDirty = serializer.bool(creature.meshDirty);

        if (version >= 0x166) {
            creature.eyeList = serializer.thingarray(creature.eyeList);
            creature.brainAiList = serializer.thingarray(creature.brainAiList);
            creature.brainLifeList = serializer.thingarray(creature.brainLifeList);
        }

        if (0x177 < version && version < 0x1e3) serializer.f32(0);

        if (version >= 0x19c)
            creature.reactToLethal = serializer.bool(creature.reactToLethal);

        if (version >= 0x1a9) {
            creature.oldAnimMatrix = serializer.m44(creature.oldAnimMatrix);
            creature.animOffset = serializer.f32(creature.animOffset);
        }

        if (0x1ed < version && version < 0x225)
            serializer.s32(0);
        
        if (version >= 0x1fc) creature.groundNormalRaw = serializer.v3(creature.groundNormalRaw);
        if (version >= 0x212) {
            creature.groundNormalSmooth = serializer.v3(creature.groundNormalSmooth);
            creature.bodyAdjustApplied = serializer.f32(creature.bodyAdjustApplied);
        }

        if (version >= 0x240 && version < 0x2c4)
            creature.switchScale = serializer.f32(creature.switchScale);

        if (version > 0x242 && version < 0x24d)
            serializer.resource(null, ResourceType.PLAN);

        if (version >= 0x243)
            creature.gunDirAndDashVec = serializer.v3(creature.gunDirAndDashVec);
        if (subVersion >= 0x19e)
            creature.gunDirAndDashVecW = serializer.i32(creature.gunDirAndDashVecW);
        
        if (version >= 0x246)
            creature.resourceThing = serializer.thing(creature.resourceThing);

        if (version >= 0x247) 
            creature.gunFireFrame = serializer.i32(creature.gunFireFrame);
        if (version >= 0x248) 
            creature.bulletCount = serializer.i32(creature.bulletCount);
        if (version >= 0x24a)
            creature.bulletImmuneTimer = serializer.i32(creature.bulletImmuneTimer);
        if (version >= 0x24d)
            creature.bulletEmitter0 = serializer.thing(creature.bulletEmitter0);
        if (version >= 0x3a2)
            creature.bulletEmitter1 = serializer.thing(creature.bulletEmitter1);
        if (version >= 0x24e) 
            creature.bulletPosIndex = serializer.i32(creature.bulletPosIndex);
        if (version >= 0x24f) {
            creature.maxBulletCount = serializer.i32(creature.maxBulletCount);
            creature.ammoFillFactor = serializer.f32(creature.ammoFillFactor);
        }
        if (version >= 0x252)
            creature.gunDirPrecisionMode = serializer.bool(creature.gunDirPrecisionMode);

        if (version >= 0x320) {
            creature.fireRate = serializer.i32(creature.fireRate);
            creature.gunAccuracy = serializer.f32(creature.gunAccuracy);
            creature.bulletEmitOffset = serializer.v3(creature.bulletEmitOffset);
            creature.bulletEmitRotation = serializer.f32(creature.bulletEmitRotation);
            creature.gunThing = serializer.thing(creature.gunThing);
            creature.gunTrigger = serializer.thing(creature.gunTrigger);
            creature.lastGunTriggerUID = serializer.i32(creature.lastGunTriggerUID);
        }

        if (version >= 0x272)
            creature.airTimeLeft = serializer.i32(creature.airTimeLeft);

        if (version >= 0x2c9 || revision.has(Branch.LEERDAMMER, Revisions.LD_SUBMERGED)) {
            creature.amountBodySubmerged = serializer.f32(creature.amountBodySubmerged);
            creature.amountHeadSubmerged = serializer.f32(creature.amountHeadSubmerged);
        }

        if (version >= 0x289 || revision.isLeerdammer())
            creature.hasScubaGear = serializer.bool(creature.hasScubaGear);
        
        if ((version >= 0x289 && version < 0x2c8) || revision.before(Branch.LEERDAMMER, Revisions.LD_REMOVED_HEAD_PIECE))
            creature.headPiece = serializer.resource(creature.headPiece, ResourceType.PLAN);

        if (version >= 0x289 || revision.isLeerdammer())
            creature.outOfWaterJumpBoost = serializer.bool(creature.outOfWaterJumpBoost);
        
        if (version >= 0x2a9) 
            creature.handPiece = serializer.resource(creature.handPiece, ResourceType.PLAN);

        if (version >= 0x273) {
            creature.head = serializer.thing(creature.head);
            creature.toolTetherJoint = serializer.thing(creature.toolTetherJoint);
            creature.toolTetherWidth = serializer.f32(creature.toolTetherWidth);
            creature.jetpack = serializer.thing(creature.jetpack);
            creature.wallJumpDir = serializer.i32(creature.wallJumpDir);
            creature.wallJumpPos = serializer.v3(creature.wallJumpPos);

            if (!serializer.isWriting()) creature.bootContactForceList = new Vector3f[serializer.getInput().i32()];
            else {
                if (creature.bootContactForceList == null)
                    creature.bootContactForceList = new Vector3f[0];
                serializer.getOutput().i32(creature.bootContactForceList.length);
            }
            for (int i = 0; i < creature.bootContactForceList.length; ++i)
                creature.bootContactForceList[i] = serializer.v3(creature.bootContactForceList[i]);
            
            
            creature.gunType = serializer.s32(creature.gunType);
            creature.wallJumpMat = serializer.bool(creature.wallJumpMat);
        }
        
        if (version >= 0x29e && version < 0x336)
            creature.lastDirectControlPrompt = serializer.thing(creature.lastDirectControlPrompt);

        if (version > 0x2e4)
            creature.directControlPrompt = serializer.thing(creature.directControlPrompt);
        
        if (version >= 0x29e && version < 0x336) {
            creature.smoothedDirectControlStick = serializer.v3(creature.smoothedDirectControlStick);
            creature.directControlAnimFrame = serializer.i16(creature.directControlAnimFrame);
            creature.directControlAnimState = serializer.i8(creature.directControlAnimState);
        }

        if (version >= 0x29f && version < 0x2c1)
            creature.directControlMode = serializer.i8(creature.directControlMode);

        if (version >= 0x2c1 && version < 0x336)
            serializer.u8(0);

        if (version >= 0x2a5) {
            creature.responsiblePlayer = serializer.i32(creature.responsiblePlayer);
            creature.responsibleFramesLeft = serializer.i32(creature.responsibleFramesLeft);
        }

        if (version >= 0x32c)
            creature.canDropPowerup = serializer.bool(creature.canDropPowerup);
        
        if (version >= 0x35a)
            creature.behavior = serializer.i32(creature.behavior);

        if (version >= 0x373)
            creature.effectDestroy = serializer.i32(creature.effectDestroy);

        if (version >= 0x3c0)
            creature.whipSim = serializer.thing(creature.whipSim);

        if (subVersion >= 0xaa)
            creature.alternateFormWorld = serializer.thing(creature.alternateFormWorld);

        if (subVersion >= 0xd7)
            creature.hookHatState = serializer.i32(creature.hookHatState);

        if (subVersion >= 0xdf)
            creature.hookHatBogey = serializer.thing(creature.hookHatBogey);
        
        if (subVersion >= 0x196) {
            creature.flyingState = serializer.i32(creature.flyingState);
            creature.flyingTimer = serializer.i32(creature.flyingTimer);
            creature.flyingFlumpFrame = serializer.i32(creature.flyingFlumpFrame);
            creature.flyingImpulseFrame = serializer.i32(creature.flyingImpulseFrame);
            creature.flyingFlapButtonTimer = serializer.i32(creature.flyingFlapButtonTimer);
            creature.flyingBrakeTimer = serializer.i32(creature.flyingBrakeTimer);
            creature.flyingGrabFallTimer = serializer.i32(creature.flyingGrabFallTimer);
            creature.flyingLegScale = serializer.f32(creature.flyingLegScale);
            creature.flyingVels = serializer.v4(creature.flyingVels);
            creature.flyingFlapLockout = serializer.bool(creature.flyingFlapLockout);
            creature.flyingFallLockout = serializer.bool(creature.flyingFallLockout);
            creature.flyingInWind = serializer.bool(creature.flyingInWind);
            creature.flyingThrustLatched = serializer.bool(creature.flyingThrustLatched);
            creature.glidingTime = serializer.i16(creature.glidingTime);
        }

        if (subVersion >= 0x20c) {
            creature.springState = serializer.i8(creature.springState);
            creature.springHasSprung = serializer.bool(creature.springHasSprung);
            creature.currentSpringData = serializer.reference(creature.currentSpringData, SpringData.class);
            creature.springPower = serializer.i8(creature.springPower);
            creature.springSeparateForces = serializer.bool(creature.springSeparateForces);
            creature.springForce = serializer.i8(creature.springForce);
            creature.springStateTimer = serializer.i8(creature.springStateTimer);
        }

        return creature;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
}
