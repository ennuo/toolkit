package cwlib.structs.things.parts;

import cwlib.enums.*;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.profile.DataLabelValue;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.GlobalThingDescriptor;
import cwlib.structs.things.components.switches.SwitchOutput;
import cwlib.structs.things.components.switches.SwitchSignal;
import cwlib.structs.things.components.switches.SwitchTarget;
import cwlib.types.data.NetworkOnlineID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import org.joml.Vector4f;

public class PSwitch implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x200;

    public boolean inverted;
    public float radius;
    @GsonRevision(min = 0x382)
    public float minRadius;
    public int colorIndex;
    @GsonRevision(min = 0x2dc)
    public String name;
    @GsonRevision(min = 0x38f)
    public boolean crappyOldLbp1Switch;
    public int behaviorOld;
    public SwitchOutput[] outputs;

    @GsonRevision(min = 0x160)
    public ResourceDescriptor stickerPlan;
    @GsonRevision(min = 0x140, max = 0x15f)
    @Deprecated
    public GlobalThingDescriptor refSticker;

    @GsonRevision(min = 0x198)
    public boolean hideInPlayMode;
    @GsonRevision(min = 0x1a5)
    public SwitchType type = SwitchType.INVALID;
    @GsonRevision(min = 0x1a5)
    public Thing referenceThing;
    @GsonRevision(min = 0x1a5)
    public SwitchSignal manualActivation;
    @GsonRevision(min = 0x1a5, max = 0x367)
    public float platformVisualFactor;
    @GsonRevision(min = 0x1a5, max = 0x2bf)
    public float oldActivation;
    @GsonRevision(min = 0x1a5)
    public int activationHoldTime;
    @GsonRevision(min = 0x1a5)
    public boolean requireAll;
    @GsonRevision(min = 0x1fb, max = 0x326)
    public Vector4f[] connectorPos;
    @GsonRevision(min = 0x1fb, max = 0x326)
    public boolean[] connectorGrabbed;
    @GsonRevision(min = 0x1fb, max = 0x326)
    public Vector4f portPosOffset, looseConnectorPos, looseConnectorBaseOffset;
    @GsonRevision(min = 0x1fb, max = 0x326)
    public boolean looseConnectorGrabbed;
    @GsonRevision(min = 0x23e)
    public float angleRange;
    @GsonRevision(min = 0x23e)
    public int includeTouching;
    @GsonRevision(min = 0x244)
    public int bulletsRequired;
    @GsonRevision(min = 0x245)
    public int bulletsDetected;
    @GsonRevision(min = 0x246, max = 0x397)
    public int bulletPlayerNumber;
    @GsonRevision(min = 0x249)
    public int bulletRefreshTime;
    @GsonRevision(min = 0x2f5)
    public boolean resetWhenFull;
    @GsonRevision(min = 0x24b, max = 0x326)
    public boolean hideConnectors;
    @GsonRevision(min = 0x273, max = 0x397)
    public SwitchLogicType logicType = SwitchLogicType.AND;
    @GsonRevision(min = 0x273, max = 0x368)
    public int updateFrame;
    @GsonRevision(min = 0x273)
    @Deprecated
    public Thing[] inputList;
    @GsonRevision(min = 0x277, max = 0x326)
    public Thing portThing;
    @GsonRevision(min = 0x284)
    public boolean includeRigidConnectors;
    @GsonRevision(min = 0x285, max = 0x326)
    public Vector4f customPortOffset, customConnectorOffset;
    @GsonRevision(min = 0x28d)
    public float timerCount;
    @GsonRevision(min = 0x28d, max = 0x2c3)
    public byte timerAutoCount;

    // @GsonRevision(lbp3=true,max=0x9f)
    @GsonRevision(min = 0x2ae)
    public int teamFilter;

    @GsonRevision(min = 0x2c4)
    public SwitchBehavior behavior = SwitchBehavior.OFF_ON;
    @GsonRevision(min = 0x2c4)
    public int randomBehavior, randomPattern;
    @GsonRevision(min = 0x2c4)
    public int randomOnTimeMin, randomOnTimeMax;
    @GsonRevision(min = 0x2c4)
    public int randomOffTimeMin, randomOffTimeMax;
    @GsonRevision(min = 0x2c4, max = 0x3ac)
    public int randomPhaseOn, randomPhaseTime;
    @GsonRevision(min = 0x2c4)
    public boolean retardedOldJoint;
    @GsonRevision(min = 0x310)
    public int keySensorMode;
    @GsonRevision(min = 0x34d)
    public int userDefinedColour;
    @GsonRevision(min = 0x34d)
    public boolean wiresVisible;
    @GsonRevision(min = 0x350)
    public byte bulletTypes;
    @GsonRevision(min = 0x391)
    public boolean detectUnspawnedPlayers;
    @GsonRevision(lbp3 = true, min = 0x217)
    public byte unspawnedBehavior;
    @GsonRevision(min = 0x3a5)
    public boolean playSwitchAudio;
    @GsonRevision(min = 0x3ed)
    public byte playerMode;
    @GsonRevision(min = 0x3ef)
    @GsonRevision(branch = 0x4431, min = 0x45)
    public DataLabelValue value = new DataLabelValue();
    @GsonRevision(lbp3 = true, min = 0x22)
    public boolean relativeToSequencer;
    @GsonRevision(lbp3 = true, min = 0x30)
    public byte layerRange;
    @GsonRevision(lbp3 = true, min = 0x7b)
    public boolean breakSound;
    @GsonRevision(lbp3 = true, min = 0x7b)
    public int colorTimer;
    @GsonRevision(lbp3 = true, min = 0x68)
    public boolean isLbp3Switch;
    @GsonRevision(lbp3 = true, min = 0x69)
    public boolean randomNonRepeating;
    @GsonRevision(lbp3 = true, min = 0x103)
    public int stickerSwitchMode;

    /* Vita */
    @GsonRevision(branch = 0x4431, min = 0x2a)
    public byte impactSensorMode;
    @GsonRevision(branch = 0x4431, min = 0x1)
    public int switchTouchType;
    @GsonRevision(branch = 0x4431, min = 0x9)
    public byte cursorScreenArea;
    @GsonRevision(branch = 0x4431, min = 0xb)
    public byte cursorInteractionType;
    @GsonRevision(branch = 0x4431, min = 0xc)
    public byte cursorTouchPanels;
    @GsonRevision(branch = 0x4431, min = 0x23)
    public byte cursorTouchIndex;
    @GsonRevision(branch = 0x4431, min = 0x36)
    public byte flags;
    @GsonRevision(branch = 0x4431, min = 0x2b)
    public int outputAndOr;
    @GsonRevision(branch = 0x4431, min = 0x41)
    public byte includeSameChipTags;
    @GsonRevision(branch = 0x4431, min = 0x43)
    public int glowFrontCol, glowBackCol, glowActiveCol;
    @GsonRevision(branch = 0x4431, min = 0x54)
    public byte playerFilter;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x1a5)
        {
            serializer.reference(null, Thing.class);
            serializer.bool(false);
        }

        inverted = serializer.bool(inverted);
        radius = serializer.f32(radius);

        if (version >= 0x382) minRadius = serializer.f32(minRadius);

        colorIndex = serializer.s32(colorIndex);

        if (version >= 0x2dc)
            name = serializer.wstr(name);

        if (version >= 0x38f)
            crappyOldLbp1Switch = serializer.bool(crappyOldLbp1Switch);
        if (!serializer.isWriting() && version < 0x309)
            crappyOldLbp1Switch = true;

        behaviorOld = serializer.s32(behaviorOld);

        if (version < 0x329)
        {
            // I would just use SwitchOutput, but ternary
            // doesn't exist in this struct, despite SwitchSignal having
            // it added in 0x310
            if (serializer.isWriting())
            {
                MemoryOutputStream stream = serializer.getOutput();
                SwitchOutput output = (outputs != null && outputs.length != 0) ?
                    outputs[0] : new SwitchOutput();
                serializer.f32(output.activation.activation);
                if (version > 0x2a2) stream.i32(output.activation.player);
                serializer.array(output.targetList, SwitchTarget.class);
            }
            else
            {
                MemoryInputStream stream = serializer.getInput();
                SwitchOutput output = new SwitchOutput();
                output.activation.activation = stream.f32();
                if (version > 0x2a2)
                    output.activation.player = serializer.i32(output.activation.player);
                output.targetList = serializer.array(null, SwitchTarget.class);
                outputs = new SwitchOutput[] { output };
            }
        }
        else outputs = serializer.array(outputs, SwitchOutput.class, true);

        if (version < 0x398 && 0x140 <= version)
        {
            if (version < 0x160)
                refSticker = serializer.struct(refSticker, GlobalThingDescriptor.class);
            else
                stickerPlan = serializer.resource(stickerPlan, ResourceType.PLAN, true,
                    false, false);
        }

        if (0x13f < version && version < 0x1a5) serializer.s32(0);

        if (version > 0x197) hideInPlayMode = serializer.bool(hideInPlayMode);
        if (version > 0x1a4)
        {
            type = serializer.enum32(type, true);
            referenceThing = serializer.reference(referenceThing, Thing.class);
            manualActivation = serializer.struct(manualActivation, SwitchSignal.class);
        }

        if (version >= 0x398 && (type == SwitchType.STICKER || (type == SwitchType.POCKET_ITEM && subVersion > 0x10)))
            stickerPlan = serializer.resource(stickerPlan, ResourceType.PLAN, true, false,
                false);

        if (version > 0x1a4 && version < 0x368)
            platformVisualFactor = serializer.f32(platformVisualFactor);
        if (version > 0x1a4 && version < 0x2a0) oldActivation = serializer.f32(oldActivation);

        if (version > 0x1a4) activationHoldTime = serializer.s32(activationHoldTime);
        if (version > 0x1a4) requireAll = serializer.bool(requireAll);

        if (revision.has(Branch.DOUBLE11, 0x2a))
            impactSensorMode = serializer.i8(impactSensorMode);

        if (version > 0x1fa && version < 0x327)
        {
            connectorPos = serializer.vectorarray(connectorPos);

            // connectorGrabbed
            if (!serializer.isWriting())
                connectorGrabbed = new boolean[serializer.getInput().i32()];
            else
            {
                if (connectorGrabbed == null)
                    connectorGrabbed = new boolean[0];
                serializer.getOutput().i32(connectorGrabbed.length);
            }
            for (int i = 0; i < connectorGrabbed.length; ++i)
                connectorGrabbed[i] = serializer.bool(connectorGrabbed[i]);

            portPosOffset = serializer.v4(portPosOffset);
            looseConnectorPos = serializer.v4(looseConnectorPos);
            looseConnectorBaseOffset = serializer.v4(looseConnectorBaseOffset);
            looseConnectorGrabbed = serializer.bool(looseConnectorGrabbed);
        }

        if (version > 0x23d)
        {
            angleRange = serializer.f32(angleRange);
            includeTouching = serializer.s32(includeTouching);
            if (version >= 0x398 && type == SwitchType.MICROCHIP && includeTouching == 1)
                stickerPlan = serializer.resource(stickerPlan, ResourceType.PLAN, true,
                    false, false);
        }

        if (subVersion > 0x165 && type == SwitchType.GAME_LIVE_STREAMING_CHOICE)
        {
            throw new SerializationException("UNSUPPORTED!");
            // wstr[4], wstr[5] if subVersion >= 0x187?
        }

        if (version > 0x243) bulletsRequired = serializer.s32(bulletsRequired);
        if (version == 0x244) serializer.i32(0); // ???
        if (version > 0x244) bulletsDetected = serializer.s32(bulletsDetected);
        if (version > 0x245 && version < 0x398)
            bulletPlayerNumber = serializer.i32(bulletPlayerNumber);
        if (version > 0x248)
            bulletRefreshTime = serializer.i32(bulletRefreshTime);

        if (version >= 0x2f5) resetWhenFull = serializer.bool(resetWhenFull);

        if (version > 0x24a && version < 0x327)
            hideConnectors = serializer.bool(hideConnectors);

        if (version > 0x272 && version < 0x398)
            logicType = serializer.enum32(logicType);
        if (version > 0x272 && version < 0x369)
            updateFrame = serializer.i32(updateFrame);
        if (version > 0x272)
            inputList = serializer.thingarray(inputList);

        if (version > 0x276 && version < 0x327)
            portThing = serializer.thing(portThing);

        if (version > 0x283)
            includeRigidConnectors = serializer.bool(includeRigidConnectors);

        if (version > 0x284 && version < 0x327)
        {
            customPortOffset = serializer.v4(customPortOffset);
            customConnectorOffset = serializer.v4(customConnectorOffset);
        }

        if (version > 0x28c)
        {
            timerCount = serializer.f32(timerCount);
            if (version < 0x2c4)
                timerAutoCount = serializer.i8(timerAutoCount);
        }

        if (version > 0x2ad && subVersion < 0x100)
            teamFilter = serializer.i32(teamFilter);

        if (version > 0x2c3) behavior = serializer.enum32(behavior);

        if (version < 0x329 && version > 0x2c3) serializer.s32(0);

        if (version > 0x2c3)
        {
            randomBehavior = serializer.i32(randomBehavior);
            randomPattern = serializer.i32(randomPattern);
            randomOnTimeMin = serializer.i32(randomOnTimeMin);
            randomOnTimeMax = serializer.i32(randomOnTimeMax);
            randomOffTimeMin = serializer.i32(randomOffTimeMin);
            randomOffTimeMax = serializer.i32(randomOffTimeMax);
            if (version < 0x3ad)
            {
                randomPhaseOn = serializer.u8(randomPhaseOn);
                randomPhaseTime = serializer.s32(randomPhaseTime);
            }
            retardedOldJoint = serializer.bool(retardedOldJoint);
        }

        if (version > 0x30f) keySensorMode = serializer.i32(keySensorMode);
        if (version > 0x34c)
        {
            userDefinedColour = serializer.i32(userDefinedColour);
            wiresVisible = serializer.bool(wiresVisible);
        }
        if (version > 0x34f) bulletTypes = serializer.i8(bulletTypes);
        if (version > 0x390) detectUnspawnedPlayers = serializer.bool(detectUnspawnedPlayers);
        if (subVersion > 0x216)
            unspawnedBehavior = serializer.i8(unspawnedBehavior);
        if (version > 0x3a4) playSwitchAudio = serializer.bool(playSwitchAudio);

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();

            if (vita >= 0x1) // > 0x3c0
                switchTouchType = serializer.i32(switchTouchType);

            if (vita >= 0x6 && vita < 0x36) // > 0x3c0
                serializer.u8(0);

            if (vita >= 0x9 && vita < 0x2c) // > 0x3c0
                cursorScreenArea = (byte) serializer.u32(cursorScreenArea);
            else if (vita >= 0x2c)
                cursorScreenArea = serializer.i8(cursorScreenArea);

            if (vita >= 0xb) // > 0x3c0
                cursorInteractionType = serializer.i8(cursorInteractionType);
            if (vita >= 0xc) // > 0x3c0
                cursorTouchPanels = serializer.i8(cursorTouchPanels);
            if (vita >= 0x23) // > 0x3c0
                cursorTouchIndex = serializer.i8(cursorTouchIndex);

            if (vita < 0x36)
            { // > 0x3c0
                if (vita >= 0x23) serializer.u8(0);
                if (vita >= 0x13) serializer.u8(0);
                if (vita >= 0x7) serializer.u8(0);
                if (vita >= 0x15) serializer.u8(0);
                if (vita >= 0x24) serializer.u8(0);
            } // Most of these should correspond to a value in sw.flags

            if (vita >= 0x36)
                flags = serializer.i8(flags);
            if (vita >= 0x2b)
                outputAndOr = serializer.s32(outputAndOr);

            if (vita >= 0x2b && vita < 0x36)
                serializer.u8(0);

            // data sampler, although 0x2f shouldn't be the switch type?
            if (revision.isVita() && type.getValue() == 0x2f)
            {
                if (value == null) value = new DataLabelValue();

                if (vita >= 0x45)
                    value.labelIndex = serializer.i32(value.labelIndex);
                if (vita >= 0x46)
                {
                    value.creatorID = serializer.struct(value.creatorID, NetworkOnlineID.class);
                    value.labelName = serializer.wstr(value.labelName);
                }

                if (revision.has(Branch.DOUBLE11, Revisions.D1_LABEL_ANALOGUE_ARRAY))
                    value.analogue = serializer.floatarray(value.analogue);
                else if (revision.has(Branch.DOUBLE11, Revisions.D1_DATALABELS))
                {
                    if (serializer.isWriting())
                    {
                        float analogue = value.analogue != null && value.analogue.length != 0 ?
                            value.analogue[0] : 0.0f;
                        serializer.getOutput().f32(analogue);
                    }
                    else value.analogue = new float[] { serializer.getInput().f32() };
                }

                if (revision.has(Branch.DOUBLE11, Revisions.D1_LABEL_TERNARY))
                    value.ternary = serializer.bytearray(value.ternary);
            }

            if (vita >= 0x38 && vita < 0x41)
                serializer.u8(0); // if equal to 0, includeSameChipTags is 1

            if (vita >= 0x41)
                includeSameChipTags = serializer.i8(includeSameChipTags);
            if (vita >= 0x43)
            {
                glowFrontCol = serializer.i32(glowFrontCol);
                glowBackCol = serializer.i32(glowBackCol);
                glowActiveCol = serializer.i32(glowActiveCol);
            }

            if (vita >= 0x54)
                playerFilter = serializer.i8(playerFilter);
        }

        if (version > 0x3ec) playerMode = serializer.i8(playerMode);

        if (version > 0x3ee && type == SwitchType.DATA_SAMPLER)
        {
            if (value == null) value = new DataLabelValue();

            value.labelIndex = serializer.i32(value.labelIndex);
            value.creatorID = serializer.struct(value.creatorID, NetworkOnlineID.class);
            value.labelName = serializer.wstr(value.labelName);
            value.analogue = serializer.floatarray(value.analogue);
            value.ternary = serializer.bytearray(value.ternary);
        }

        if (subVersion > 0x21)
            relativeToSequencer = serializer.bool(relativeToSequencer);
        if (subVersion > 0x2f)
            layerRange = serializer.i8(layerRange);
        if (subVersion > 0x7a)
        {
            breakSound = serializer.bool(breakSound);
            colorTimer = serializer.s32(colorTimer);
        }

        if (subVersion > 0x67)
            isLbp3Switch = serializer.bool(isLbp3Switch);
        if (subVersion > 0x68)
            randomNonRepeating = serializer.bool(randomNonRepeating);
        if (subVersion > 0x102)
            stickerSwitchMode = serializer.i32(stickerSwitchMode);
    }

    // TODO: Actually implement
    @Override
    public int getAllocatedSize()
    {
        return 0;
    }
}
