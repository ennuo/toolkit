package cwlib.structs.things.parts;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;

import org.joml.Vector3f;

public class PPhysicsTweak implements Serializable
{
    @GsonRevision(max = 0x2c3)
    @Deprecated
    public float activation;

    public float tweakGravity;

    @GsonRevision(min = 0x300)
    public float tweakBuoyancy;

    @GsonRevision(max = 0x280)
    @Deprecated
    public float legacyTweakDampening;

    @GsonRevision(min = 0x281)
    public Vector3f tweakDampening;

    public Vector3f input;
    public Vector3f middleVel;

    @GsonRevision(max = 0x280)
    @Deprecated
    public Vector3f legacyVelRange, legacyAcceleration, legacyDeceleration;

    @GsonRevision(max = 0x280)
    @Deprecated
    public int legacyTurnToFace;

    @GsonRevision(min = 0x281)
    public float velRange, accelStrength;

    @GsonRevision(min = 0x286)
    public float decelStrength;

    @GsonRevision(lbp3 = true, min = 0xa4)
    public byte directionModifier, movementModifier;

    public boolean localSpace;

    @GsonRevision(min = 0x279)
    public int configuration;

    @GsonRevision(min = 0x283)
    public boolean hideInPlayMode;

    @GsonRevision(min = 0x28e)
    public int colorIndex;

    @GsonRevision(min = 0x2dc)
    public String name;

    @GsonRevision(lbp3 = true, min = 0x133)
    public int followerPlayerMode;

    @GsonRevision(min = 0x2ae)
    @Deprecated
    public int teamFilter;

    @GsonRevision(min = 0x2c6)
    public int behavior;

    @GsonRevision(min = 0x2dd)
    public boolean allowInOut, allowUpDown;

    @GsonRevision(min = 0x2dd)
    public float minRange, maxRange;

    @GsonRevision(min = 0x2dd)
    public boolean followKey;

    @GsonRevision(min = 0x382)
    public float angleRange;

    @GsonRevision(min = 0x2ff)
    public boolean flee;

    @GsonRevision(lbp3 = true, min = 0x70)
    public boolean stabiliser;

    @GsonRevision(lbp3 = true, min = 0x130)
    public boolean shardDephysicalised, shardPhysicsAudio;

    @GsonRevision(lbp3 = true, min = 0x44)
    public boolean isLBP2PhysicsTweak;

    @GsonRevision(min = 0x36b)
    public float maximumMass;

    @GsonRevision(min = 0x36b)
    public boolean canPush;

    @GsonRevision(min = 0x36b)
    public int zBehavior;

    @GsonRevision(min = 0x36b)
    public float lastKnownActivation;

    @GsonRevision(min = 0x36b)
    public boolean waitingToMove;

    @GsonRevision(lbp3 = true, min = 0xf7)
    public byte zPhase;

    @GsonRevision(lbp3 = true, min = 0x57)
    public short gridSnap, gridStrength;

    @GsonRevision(lbp3 = true, min = 0x9a)
    public float gridGoalW;

    @GsonRevision(lbp3 = true, min = 0x99)
    public Vector3f gridGoal;

    /* Vita */
    @GsonRevision(branch = 0x4c44, min = 0x11)
    public int usePanel;

    @GsonRevision(branch = 0x4c44, min = 0x1f)
    public int followType;

    @GsonRevision(branch = 0x4c44, min = 0x4b)
    public float followerDeceleration;

    @GsonRevision(branch = 0x4c44, min = 0x55)
    public byte playerFilter;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x2c4)
            activation = serializer.f32(activation);

        tweakGravity = serializer.f32(tweakGravity);
        if (version > 0x2ff)
            tweakBuoyancy = serializer.f32(tweakBuoyancy);

        if (version < 0x281)
            legacyTweakDampening = serializer.f32(legacyTweakDampening);

        if (version > 0x280)
            tweakDampening = serializer.v3(tweakDampening);
        input = serializer.v3(input);
        middleVel = serializer.v3(middleVel);

        if (version < 0x281)
            legacyDeceleration = serializer.v3(legacyDeceleration);
        if (version > 0x280)
            velRange = serializer.f32(velRange);
        if (version < 0x281)
            legacyAcceleration = serializer.v3(legacyAcceleration);

        if (version > 0x280)
        {
            accelStrength = serializer.f32(accelStrength);
            if (version > 0x285)
                decelStrength = serializer.f32(decelStrength);
        }

        if (version < 0x281)
        {
            legacyDeceleration = serializer.v3(legacyDeceleration);
            legacyTurnToFace = serializer.i32(legacyTurnToFace);
        }

        if (subVersion > 0xa3)
        {
            directionModifier = serializer.i8(directionModifier);
            movementModifier = serializer.i8(movementModifier);
        }

        localSpace = serializer.bool(localSpace);

        if (!serializer.isWriting() && version < 0x38a && velRange != 0.0f)
        {
            accelStrength = accelStrength / velRange;
            decelStrength = decelStrength / velRange;
        }

        if (version > 0x278)
            configuration = serializer.i32(configuration);

        if (version >= 0x27a && version < 0x327)
            serializer.thing(null);

        if (version > 0x282)
            hideInPlayMode = serializer.bool(hideInPlayMode);

        if (version > 0x28d)
            colorIndex = serializer.i32(colorIndex);
        if (version > 0x2db)
            name = serializer.wstr(name);

        if (version < 0x3e8)
        {
            if (version > 0x2ad)
                teamFilter = serializer.u8(teamFilter);
        }
        else
        {
            if (subVersion < 0x132)
            {
                if (version >= 0x2ae)
                    teamFilter = serializer.u8(teamFilter);
            }
            else
            {
                if (subVersion > 0x131)
                    followerPlayerMode = serializer.i32(followerPlayerMode);
            }
        }


        if (version > 0x2c5)
            behavior = serializer.i32(behavior);
        if (version > 0x2dc)
        {
            allowInOut = serializer.bool(allowInOut);
            allowUpDown = serializer.bool(allowUpDown);
            minRange = serializer.f32(minRange);
            maxRange = serializer.f32(maxRange);
            followKey = serializer.bool(followKey);
        }

        if (version > 0x381)
            angleRange = serializer.f32(angleRange);

        if (version > 0x2fe)
            flee = serializer.bool(flee);

        if (subVersion > 0x6f)
            stabiliser = serializer.bool(stabiliser);
        if (subVersion > 0x12f)
        {
            shardDephysicalised = serializer.bool(shardDephysicalised);
            shardPhysicsAudio = serializer.bool(shardPhysicsAudio);
        }
        if (subVersion > 0x43)
            isLBP2PhysicsTweak = serializer.bool(isLBP2PhysicsTweak);

        if (version > 0x36a)
        {
            maximumMass = serializer.f32(maximumMass);
            canPush = serializer.bool(canPush);
            zBehavior = serializer.s32(zBehavior);
            lastKnownActivation = serializer.f32(lastKnownActivation);
            waitingToMove = serializer.bool(waitingToMove);
        }

        if (subVersion > 0xf6)
            zPhase = serializer.i8(zPhase);

        // move recording, again will figure out the fields later
        // RecordingPlayer
        if (version > 0x3b8 && configuration == 0xd)
        {
            serializer.resource(null, ResourceType.THING_RECORDING); // recording
            serializer.f32(0); // playHead
            if (version < 0x3c4) serializer.u8(0);
            serializer.u8(0); // type
            serializer.u8(0); // dir
            serializer.v3(null); // prevDesiredPos
            serializer.u8(0); // prevDesiredPosSet
            serializer.m44(null); // startOrientation
            serializer.f32(0); // speed
            if (version > 0x3c4)
                serializer.u8(0); // pathIsAbsolute
        }

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();
            if (vita >= 0x11) // 0x3c0
                usePanel = serializer.i32(usePanel);
            if (vita >= 0x11 && vita < 0x4b) serializer.u8(0); // 0x3c0
            if (vita >= 0x1f) // 0x3d4
                followType = serializer.i32(followType);
            if (vita >= 0x4b)
                followerDeceleration = serializer.f32(followerDeceleration);
            if (vita >= 0x55)
                playerFilter = serializer.i8(playerFilter);
        }

        if (subVersion >= 0x1 && subVersion < 0x17)
        {
            serializer.s32(0);
            serializer.s32(0);
            serializer.s32(0);
            serializer.s32(0);
            serializer.s32(0);
            serializer.s32(0);
            serializer.s32(0);
            serializer.s32(0);
            serializer.s32(0);
        }

        if (subVersion >= 0x5 && subVersion < 0x17)
        {
            serializer.f32(0);
            serializer.u8(0);
            serializer.u8(0);
        }

        // attract-o-gel, ill figure out the fields later
        // attractorData
        if (configuration == 0xe && version > 0x3e3)
        {
            serializer.f32(0); // attractDistance
            serializer.i32(0);
            serializer.f32(0); // visualEffectBrightness_On
            serializer.f32(0); // visualEffectSpeed_On
            serializer.i32(0);
            serializer.f32(0); // visualEffectBrightnessOff
            serializer.f32(0); // visualEffectSpeed_Off
            serializer.u8(0); // attractorManualDetach
            serializer.u8(0); // attractorSoundEffects
            serializer.u8(0); // attractorVisualEffect
            serializer.u8(0); // attractorAffectConnected
        }

        if (subVersion > 0x56)
        {
            gridSnap = serializer.i16(gridSnap);
            gridStrength = serializer.i16(gridStrength);
        }

        if (subVersion > 0x99)
            gridGoalW = serializer.f32(gridGoalW);

        if (subVersion > 0x98)
            gridGoal = serializer.v3(gridGoal);
    }

    // TODO: Actually implement
    @Override
    public int getAllocatedSize()
    {
        return 0;
    }
}
