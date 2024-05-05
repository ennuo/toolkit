package cwlib.structs.things.parts;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import cwlib.enums.Branch;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonResourceType;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.structs.things.Thing;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PCreature implements Serializable
{
    public static class WhipSim implements Serializable
    {
        public static final int BASE_ALLOCATION_SIZE = 0xB0;

        public Thing creatureThing;
        @Deprecated
        public Matrix4f baseHandleMatrix;
        public Vector3f prevDir, currDir;
        public int stateTimer;
        public byte state;
        public Thing attachedThing;
        public Vector3f attachedLocalPos, attachedLocalNormal;
        public float attachedLocalAngle, attachedScale;
        public boolean playedFailToFireSound;
        public float attachedZOffset;

        @Override
        public void serialize(Serializer serializer)
        {
            creatureThing = serializer.thing(creatureThing);
            baseHandleMatrix = serializer.m44(baseHandleMatrix);
            prevDir = serializer.v3(prevDir);
            currDir = serializer.v3(currDir);
            stateTimer = serializer.i32(stateTimer);
            state = serializer.i8(state);
            attachedThing = serializer.thing(attachedThing);
            attachedLocalPos = serializer.v3(attachedLocalPos);
            attachedLocalNormal = serializer.v3(attachedLocalNormal);
            attachedLocalAngle = serializer.f32(attachedLocalAngle);
            attachedScale = serializer.f32(attachedScale);
            playedFailToFireSound = serializer.bool(playedFailToFireSound);
            attachedZOffset = serializer.f32(attachedZOffset);
        }

        @Override
        public int getAllocatedSize()
        {
            return WhipSim.BASE_ALLOCATION_SIZE;
        }
    }

    public static class SpringData implements Serializable
    {
        public static final int BASE_ALLOCATION_SIZE = 0x30;

        public Thing springThing;
        public int springTimer;
        public Vector3f springDirection, springThingPosition;

        @Override
        public void serialize(Serializer serializer)
        {
            springThing = serializer.thing(springThing);
            springTimer = serializer.i32(springTimer);
            springDirection = serializer.v3(springDirection);
            springThingPosition = serializer.v3(springThingPosition);
        }

        @Override
        public int getAllocatedSize()
        {
            return SpringData.BASE_ALLOCATION_SIZE;
        }
    }

    // ECreatureGunType
    // PAINT 0
    // RAY 1
    // CUSTOM 2

    // EDirectControlMode
    // NONE 0
    // FRONT  1
    // SIDE 2
    // MOTION_CONTROLLER 3

    // EEnemyMoveDirection
    // LEFT_FIRST 0
    // RIGHT_FIRST 1
    // LEFT_ONLY 2
    // RIGHT_ONLY 3
    // NONE 4

    // EEnemyPlayerAwareness
    // NONE 0
    // ATTRACT 1
    // REPULSE 2

    // EZEvent
    // NOTHING 0
    // WALK 1
    // AIR 2
    // JUMP_DOWN 3
    // WALK_TO_GRAB 4
    // WALK_BLOCKED 5
    // WALK_SCARED 6
    // AIR_BLOCKED 7
    // JUMP_DOWN_BLOCKED 8

    // EZMode
    // AUTO 0
    // MANUAL 1
    // HYBRID 2


    @GsonResourceType(ResourceType.SETTINGS_CHARACTER)
    public ResourceDescriptor config;

    public int jumpFrame;
    public float groundDistance;
    public Vector3f groundNormal;
    public Thing grabJoint, jumpingOff;
    public int state;
    @GsonRevision(lbp3 = true, min = 0x132)
    public int subState;
    public int stateTimer;
    public float speedModifier, jumpModifier, strengthModifier;
    public int zMode, playerAwareness, moveDirection;
    public Vector3f forceThatSmashedCreature;
    public int crushFrames;
    public float awarenessRadius;

    @GsonRevision(min = 0x1df)
    public int airTime;

    @GsonRevision(min = 0x354)
    public int[] bouncepadThingUIDs, grabbedThingUIDs;

    @GsonRevision(min = 0x221)
    public boolean haveNotTouchedGroundSinceUsingJetpack;

    @GsonRevision(min = 0x15d)
    public Thing[] legList, lifeSourceList;
    @GsonRevision(min = 0x15d)
    public Thing lifeCreature, aiCreature;

    @GsonRevision(min = 0x163)
    public int jumpInterval, jumpIntervalPhase;

    @GsonRevision(min = 0x169)
    public boolean meshDirty;

    @GsonRevision(min = 0x166)
    public Thing[] eyeList, brainAiList, brainLifeList;

    @GsonRevision(min = 0x19c)
    public boolean reactToLethal;

    @GsonRevision(min = 0x1a9)
    public Matrix4f oldAnimMatrix;
    @GsonRevision(min = 0x1a9)
    public float animOffset;

    @GsonRevision(min = 0x1fc)
    public Vector3f groundNormalRaw;
    @GsonRevision(min = 0x212)
    public Vector3f groundNormalSmooth;

    @GsonRevision(min = 0x212)
    public float bodyAdjustApplied;

    @GsonRevision(min = 0x240, max = 0x2c3)
    @Deprecated
    public float switchScale = 1.0f;

    @GsonRevision(min = 0x243)
    public Vector3f gunDirAndDashVec;
    @GsonRevision(lbp3 = true, min = 0x19e)
    public float gunDirAndDashVecW;

    @GsonRevision(min = 0x246)
    public Thing resourceThing;

    @GsonRevision(min = 0x247)
    public int gunFireFrame;

    @GsonRevision(min = 0x248)
    public int bulletCount;

    @GsonRevision(min = 0x24a)
    public int bulletImmuneTimer;

    @GsonRevision(min = 0x24d)
    public Thing bulletEmitter0;

    @GsonRevision(min = 0x3a2)
    public Thing bulletEmitter1;

    @GsonRevision(min = 0x24e)
    public int bulletPosIndex;

    @GsonRevision(min = 0x24f)
    public int maxBulletCount;

    @GsonRevision(min = 0x24f)
    public float ammoFillFactor;

    @GsonRevision(min = 0x252)
    public boolean gunDirPrecisionMode;


    @GsonRevision(min = 0x320)
    public int fireRate;
    @GsonRevision(min = 0x320)
    public float gunAccuracy;
    @GsonRevision(min = 0x320)
    public Vector3f bulletEmitOffset;
    @GsonRevision(min = 0x320)
    public float bulletEmitRotation;
    @GsonRevision(min = 0x320)
    public Thing gunThing, gunTrigger;
    @GsonRevision(min = 0x320)
    public int lastGunTriggerUID;

    @GsonRevision(min = 0x272)
    public int airTimeLeft;

    @GsonRevision(min = 0x2c9)
    @GsonRevision(branch = 0x4c44, min = 15)
    public float amountBodySubmerged, amountHeadSubmerged;

    @GsonRevision(min = 0x289)
    @GsonRevision(branch = 0x4c44, min = 0)
    public boolean hasScubaGear;

    @GsonRevision(min = 0x289, max = 0x2c7)
    @GsonRevision(branch = 0x4c44, max = 11)
    @Deprecated
    public ResourceDescriptor headPiece;

    @GsonRevision(min = 0x289)
    @GsonRevision(branch = 0x4c44, min = 0)
    public boolean outOfWaterJumpBoost;

    @GsonRevision(min = 0x2a9)
    public ResourceDescriptor handPiece;

    @GsonRevision(min = 0x273)
    public Thing head, toolTetherJoint;
    @GsonRevision(min = 0x273)
    public float toolTetherWidth;
    @GsonRevision(min = 0x273)
    public Thing jetpack;
    @GsonRevision(min = 0x273)
    public int wallJumpDir;
    @GsonRevision(min = 0x273)
    public Vector3f wallJumpPos;
    @GsonRevision(min = 0x273)
    public Vector3f[] bootContactForceList;
    @GsonRevision(min = 0x273)
    public int gunType;
    @GsonRevision(min = 0x273)
    public boolean wallJumpMat;

    @GsonRevision(min = 0x29e, max = 0x335)
    @Deprecated
    public Thing lastDirectControlPrompt;
    @GsonRevision(min = 0x2e5)
    public Thing directControlPrompt;

    @GsonRevision(min = 0x29e, max = 0x335)
    @Deprecated
    public Vector3f smoothedDirectControlStick;
    @GsonRevision(min = 0x29e, max = 0x335)
    @Deprecated
    public short directControlAnimFrame;
    @GsonRevision(min = 0x29e, max = 0x335)
    @Deprecated
    public byte directControlAnimState;
    @GsonRevision(min = 0x29f, max = 0x2c0)
    @Deprecated
    public byte directControlMode;

    @GsonRevision(min = 0x2a5)
    public int responsiblePlayer, responsibleFramesLeft;
    @GsonRevision(min = 0x32c)
    public boolean canDropPowerup;

    @GsonRevision(min = 0x3f0)
    public byte capeExtraMaxVelocityCap;

    @GsonRevision(min = 0x35a)
    public int behavior;

    @GsonRevision(min = 0x373)
    public int effectDestroy = 6;

    @GsonRevision(min = 0x3c0)
    public WhipSim whipSim;

    @GsonRevision(branch = 0x4431, min = 0x53)
    public int shootAtTouch; // vita

    @GsonRevision(lbp3 = true, min = 0xaa)
    public Thing alternateFormWorld;

    @GsonRevision(lbp3 = true, min = 0xd6)
    public int hookHatState;
    @GsonRevision(lbp3 = true, min = 0xdf)
    public Thing hookHatBogey;

    @GsonRevision(lbp3 = true, min = 0x196)
    public int flyingState, flyingTimer, flyingFlumpFrame,
        flyingImpulseFrame, flyingFlapButtonTimer, flyingBrakeTimer,
        flyingGrabFallTimer;
    @GsonRevision(lbp3 = true, min = 0x196)
    public float flyingLegScale;
    @GsonRevision(lbp3 = true, min = 0x196)
    public Vector4f flyingVels;
    @GsonRevision(lbp3 = true, min = 0x196)
    public boolean flyingFlapLockout, flyingFallLockout, flyingInWind, flyingThrustLatched;
    @GsonRevision(lbp3 = true, min = 0x196)
    public short glidingTime;

    @GsonRevision(lbp3 = true, min = 0x20c)
    public byte springState;
    @GsonRevision(lbp3 = true, min = 0x20c)
    public boolean springHasSprung;
    @GsonRevision(lbp3 = true, min = 0x20c)
    public SpringData currentSpringData;
    @GsonRevision(lbp3 = true, min = 0x20c)
    public byte springPower;
    @GsonRevision(lbp3 = true, min = 0x20c)
    public boolean springSeparateForces;
    @GsonRevision(lbp3 = true, min = 0x20c)
    public byte springForce, springStateTimer;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        config = serializer.resource(config, ResourceType.SETTINGS_CHARACTER);
        if (version < 0x155)
        {
            if (serializer.isWriting()) serializer.getOutput().i32(0);
            else
            {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                for (int i = 0; i < count; ++i)
                    stream.v3();
            }
        }

        jumpFrame = serializer.s32(jumpFrame);

        groundDistance = serializer.f32(groundDistance);
        groundNormal = serializer.v3(groundNormal);

        grabJoint = serializer.thing(grabJoint);
        if (version < 0x13c) serializer.thing(null);
        jumpingOff = serializer.thing(jumpingOff);

        state = serializer.i32(state);
        if (subVersion >= 0x132)
            subState = serializer.i32(subState);
        stateTimer = serializer.i32(stateTimer);

        speedModifier = serializer.f32(speedModifier);
        jumpModifier = serializer.f32(jumpModifier);
        strengthModifier = serializer.f32(strengthModifier);

        if (version < 0x142) serializer.v4(null);

        zMode = serializer.i32(zMode);

        if (version < 0x146) serializer.i32(0);
        if (version > 0x145 && version < 0x1f0)
        {
            serializer.i32(0);
            serializer.i32(0);
            serializer.bool(false);
            serializer.bool(false);
            serializer.i32(0);
        }

        playerAwareness = serializer.s32(playerAwareness);
        moveDirection = serializer.s32(moveDirection);

        if (version < 0x15d) serializer.u8(0);
        if (version < 0x1f0)
        {
            serializer.i32(0); // Some array of actual thing pointers, should be 0 length
            serializer.f32(0);
        }

        forceThatSmashedCreature = serializer.v3(forceThatSmashedCreature);
        crushFrames = serializer.i32(crushFrames);

        awarenessRadius = serializer.f32(awarenessRadius);

        if (version >= 0x1df)
            airTime = serializer.i32(airTime);
        if (version >= 0x354)
        {
            bouncepadThingUIDs = serializer.intvector(bouncepadThingUIDs);
            grabbedThingUIDs = serializer.intvector(grabbedThingUIDs);
        }

        if (version >= 0x221)
            haveNotTouchedGroundSinceUsingJetpack =
                serializer.bool(haveNotTouchedGroundSinceUsingJetpack);

        if (version >= 0x15d)
        {
            legList = serializer.thingarray(legList);
            if (version < 0x166)
            {
                serializer.thingarray(null);
                serializer.thingarray(null);
            }
            lifeSourceList = serializer.thingarray(lifeSourceList);
            lifeCreature = serializer.thing(lifeCreature);
            aiCreature = serializer.thing(aiCreature);
        }

        if (version >= 0x163)
        {
            jumpInterval = serializer.i32(jumpInterval);
            jumpIntervalPhase = serializer.i32(jumpIntervalPhase);
        }

        if (0x162 < version && version < 0x16d) serializer.bool(false);

        if (version >= 0x169) meshDirty = serializer.bool(meshDirty);

        if (version >= 0x166)
        {
            eyeList = serializer.thingarray(eyeList);
            brainAiList = serializer.thingarray(brainAiList);
            brainLifeList = serializer.thingarray(brainLifeList);
        }

        if (0x177 < version && version < 0x1e3) serializer.f32(0);

        if (version >= 0x19c)
            reactToLethal = serializer.bool(reactToLethal);

        if (version >= 0x1a9)
        {
            oldAnimMatrix = serializer.m44(oldAnimMatrix);
            animOffset = serializer.f32(animOffset);
        }

        if (0x1ed < version && version < 0x225)
            serializer.s32(0);

        if (version >= 0x1fc) groundNormalRaw = serializer.v3(groundNormalRaw);
        if (version >= 0x212)
        {
            groundNormalSmooth = serializer.v3(groundNormalSmooth);
            bodyAdjustApplied = serializer.f32(bodyAdjustApplied);
        }

        if (version >= 0x240 && version < 0x2c4)
            switchScale = serializer.f32(switchScale);

        if (version > 0x242 && version < 0x24d)
            serializer.resource(null, ResourceType.PLAN);

        if (version >= 0x243)
            gunDirAndDashVec = serializer.v3(gunDirAndDashVec);
        if (subVersion >= 0x19e)
            gunDirAndDashVecW = serializer.f32(gunDirAndDashVecW);

        if (version >= 0x246)
            resourceThing = serializer.thing(resourceThing);

        if (version >= 0x247)
            gunFireFrame = serializer.i32(gunFireFrame);
        if (version >= 0x248)
            bulletCount = serializer.i32(bulletCount);
        if (version >= 0x24a)
            bulletImmuneTimer = serializer.i32(bulletImmuneTimer);
        if (version >= 0x24d)
            bulletEmitter0 = serializer.thing(bulletEmitter0);
        if (version >= 0x3a2)
            bulletEmitter1 = serializer.thing(bulletEmitter1);
        if (version >= 0x24e)
            bulletPosIndex = serializer.i32(bulletPosIndex); // game
        // .bulletposindex_dashboots_hoverboard_unionval
        if (version >= 0x24f)
        {
            maxBulletCount = serializer.i32(maxBulletCount);
            ammoFillFactor = serializer.f32(ammoFillFactor);
        }
        if (version >= 0x252)
            gunDirPrecisionMode = serializer.bool(gunDirPrecisionMode);

        if (version >= 0x320)
        {
            fireRate = serializer.i32(fireRate);
            gunAccuracy = serializer.f32(gunAccuracy);
            bulletEmitOffset = serializer.v3(bulletEmitOffset);
            bulletEmitRotation = serializer.f32(bulletEmitRotation);
            gunThing = serializer.thing(gunThing);
            gunTrigger = serializer.thing(gunTrigger);
            lastGunTriggerUID = serializer.i32(lastGunTriggerUID);
        }

        if (version >= 0x272)
            airTimeLeft = serializer.i32(airTimeLeft);

        if (version >= 0x2c9 || revision.has(Branch.LEERDAMMER, Revisions.LD_SUBMERGED))
        {
            amountBodySubmerged = serializer.f32(amountBodySubmerged);
            amountHeadSubmerged = serializer.f32(amountHeadSubmerged);
        }

        if (version >= 0x289 || revision.isLeerdammer())
            hasScubaGear = serializer.bool(hasScubaGear);

        if ((version >= 0x289 && version < 0x2c8) || revision.before(Branch.LEERDAMMER,
            Revisions.LD_REMOVED_HEAD_PIECE))
            headPiece = serializer.resource(headPiece, ResourceType.PLAN);

        if (version >= 0x289 || revision.isLeerdammer())
            outOfWaterJumpBoost = serializer.bool(outOfWaterJumpBoost);

        if (version >= 0x2a9)
            handPiece = serializer.resource(handPiece, ResourceType.PLAN);

        if (version >= 0x273)
        {
            head = serializer.thing(head);
            toolTetherJoint = serializer.thing(toolTetherJoint);
            toolTetherWidth = serializer.f32(toolTetherWidth);
            jetpack = serializer.thing(jetpack);
            wallJumpDir = serializer.s32(wallJumpDir);
            wallJumpPos = serializer.v3(wallJumpPos);

            if (!serializer.isWriting())
                bootContactForceList = new Vector3f[serializer.getInput().i32()];
            else
            {
                if (bootContactForceList == null)
                    bootContactForceList = new Vector3f[0];
                serializer.getOutput().i32(bootContactForceList.length);
            }
            for (int i = 0; i < bootContactForceList.length; ++i)
                bootContactForceList[i] = serializer.v3(bootContactForceList[i]);


            gunType = serializer.s32(gunType);
            wallJumpMat = serializer.bool(wallJumpMat);
        }

        if (version >= 0x29e && version < 0x336)
            lastDirectControlPrompt = serializer.thing(lastDirectControlPrompt);

        if (version > 0x2e4)
            directControlPrompt = serializer.thing(directControlPrompt);

        if (version >= 0x29e && version < 0x336)
        {
            smoothedDirectControlStick =
                serializer.v3(smoothedDirectControlStick);
            directControlAnimFrame = serializer.i16(directControlAnimFrame);
            directControlAnimState = serializer.i8(directControlAnimState);
        }

        if (version >= 0x29f && version < 0x2c1)
            directControlMode = serializer.i8(directControlMode);

        if (version >= 0x2c1 && version < 0x336)
            serializer.u8(0);

        if (version >= 0x2a5)
        {
            responsiblePlayer = serializer.i32(responsiblePlayer);
            responsibleFramesLeft = serializer.i32(responsibleFramesLeft);
        }

        if (version >= 0x32c)
            canDropPowerup = serializer.bool(canDropPowerup);

        if (version >= 0x3f0)
            capeExtraMaxVelocityCap = serializer.i8(capeExtraMaxVelocityCap);

        if (version >= 0x35a)
            behavior = serializer.i32(behavior);

        if (version >= 0x373)
            effectDestroy = serializer.i32(effectDestroy);

        if (version >= 0x3c0)
            whipSim = serializer.reference(whipSim, WhipSim.class);

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();
            if (vita < 0x53) serializer.u8(0);
            if (vita >= 0x53)
                shootAtTouch = serializer.i32(shootAtTouch);
        }

        if (subVersion >= 0x88 && subVersion <= 0xa4)
            serializer.resource(null, ResourceType.PLAN);

        if (subVersion >= 0xaa)
            alternateFormWorld = serializer.thing(alternateFormWorld);

        if (subVersion >= 0xd7)
            hookHatState = serializer.i32(hookHatState);

        if (subVersion >= 0xd7 && subVersion < 0xea)
        {
            serializer.thing(null);
            serializer.thing(null);
        }

        if (subVersion >= 0xdf)
            hookHatBogey = serializer.thing(hookHatBogey);

        if (subVersion >= 0x196)
        {
            flyingState = serializer.i32(flyingState);
            flyingTimer = serializer.i32(flyingTimer);
            flyingFlumpFrame = serializer.i32(flyingFlumpFrame);
            flyingImpulseFrame = serializer.i32(flyingImpulseFrame);
            flyingFlapButtonTimer = serializer.i32(flyingFlapButtonTimer);
            flyingBrakeTimer = serializer.i32(flyingBrakeTimer);
            flyingGrabFallTimer = serializer.i32(flyingGrabFallTimer);
            flyingLegScale = serializer.f32(flyingLegScale);
            flyingVels = serializer.v4(flyingVels);
            flyingFlapLockout = serializer.bool(flyingFlapLockout);
            flyingFallLockout = serializer.bool(flyingFallLockout);
            flyingInWind = serializer.bool(flyingInWind);
            flyingThrustLatched = serializer.bool(flyingThrustLatched);
            glidingTime = serializer.i16(glidingTime);
        }

        if (subVersion >= 0x20c)
        {
            springState = serializer.i8(springState);
            springHasSprung = serializer.bool(springHasSprung);
            currentSpringData = serializer.reference(currentSpringData,
                SpringData.class);
            springPower = serializer.i8(springPower);
            springSeparateForces = serializer.bool(springSeparateForces);
            springForce = serializer.i8(springForce);
            springStateTimer = serializer.i8(springStateTimer);
        }
    }

    // TODO: Actually implement
    @Override
    public int getAllocatedSize()
    {
        return 0;
    }
}
