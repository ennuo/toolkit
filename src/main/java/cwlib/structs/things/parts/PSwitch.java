package cwlib.structs.things.parts;

import org.joml.Vector4f;

import cwlib.enums.Branch;
import cwlib.enums.ResourceType;
import cwlib.enums.SwitchBehavior;
import cwlib.enums.SwitchLogicType;
import cwlib.enums.SwitchType;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
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

public class PSwitch implements Serializable {
    public boolean inverted;
    public float radius, minRadius;
    public int colorIndex;
    public String name;
    public boolean crappyOldLbp1Switch;
    public int behaviorOld;
    public SwitchOutput[] outputs;

    public ResourceDescriptor stickerPlan;
    @Deprecated public GlobalThingDescriptor refSticker;

    public boolean hideInPlayMode;
    public SwitchType type = SwitchType.INVALID;
    public Thing referenceThing;
    public SwitchSignal manualActivation;
    public float platformVisualFactor;
    public float oldActivation;
    public int activationHoldTime;
    public boolean requireAll;
    public Vector4f[] connectorPos;
    public boolean[] connectorGrabbed;
    public Vector4f portPosOffset, looseConnectorPos, looseConnectorBaseOffset;
    public boolean looseConnectorGrabbed;
    public float angleRange;
    public int includeTouching;
    public int bulletsRequired, bulletsDetected, bulletPlayerNumber, bulletRefreshTime;
    public boolean resetWhenFull, hideConnectors;
    public SwitchLogicType logicType = SwitchLogicType.AND;
    public int updateFrame;
    @Deprecated public Thing[] inputList;
    public Thing portThing;
    public boolean includeRigidConnectors;
    public Vector4f customPortOffset, customConnectorOffset;
    public float timerCount;
    public byte timerAutoCount;
    public int teamFilter;
    public SwitchBehavior behavior = SwitchBehavior.OFF_ON;
    public int randomBehavior, randomPattern;
    public int randomOnTimeMin, randomOnTimeMax;
    public int randomOffTimeMin, randomOffTimeMax;
    public int randomPhaseOn, randomPhaseTime;
    public boolean retardedOldJoint;
    public int keySensorMode;
    public int userDefinedColour;
    public boolean wiresVisible;
    public byte bulletTypes;
    public boolean detectUnspawnedPlayers;
    public byte unspawnedBehavior;
    public boolean playSwitchAudio;
    public byte playerMode;
    public DataLabelValue value = new DataLabelValue();
    public boolean relativeToSequencer;
    public byte layerRange;
    public boolean breakSound;
    public int colorTimer;
    public boolean isLbp3Switch, randomNonRepeating;
    public int stickerSwitchMode;

    /* Vita */
    public byte impactSensorMode;
    public int switchTouchType;
    public byte cursorScreenArea, cursorInteractionType, cursorTouchPanels, cursorTouchIndex;
    public byte flags;
    public int outputAndOr;
    public byte includeSameChipTags;
    public int glowFrontCol, glowBackCol, glowActiveCol;
    public byte playerFilter;


    @SuppressWarnings("unchecked")
    @Override public PSwitch serialize(Serializer serializer, Serializable structure) {
        PSwitch sw = (structure == null) ? new PSwitch() : (PSwitch) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x1a5) {
            serializer.reference(null, Thing.class);
            serializer.bool(false);
        }

        sw.inverted = serializer.bool(sw.inverted);
        sw.radius = serializer.f32(sw.radius);

        if (version >= 0x382) sw.minRadius = serializer.f32(sw.minRadius);

        sw.colorIndex = serializer.s32(sw.colorIndex);

        if (version >= 0x2dc)
            sw.name = serializer.wstr(sw.name);

        if (version >= 0x38f)
            sw.crappyOldLbp1Switch = serializer.bool(sw.crappyOldLbp1Switch);
        if (!serializer.isWriting() && version < 0x309)
            sw.crappyOldLbp1Switch = true;
        
        sw.behaviorOld = serializer.s32(sw.behaviorOld);

        if (version < 0x329) {
            // I would just use SwitchOutput, but ternary
            // doesn't exist in this struct, despite SwitchSignal having
            // it added in 0x310
            if (serializer.isWriting()) {
                MemoryOutputStream stream = serializer.getOutput();
                SwitchOutput output = (sw.outputs != null && sw.outputs.length != 0) ? sw.outputs[0] : new SwitchOutput();
                serializer.f32(output.activation.activation);
                if (version > 0x2a2) stream.i32(output.activation.player);
                serializer.array(output.targetList, SwitchTarget.class);
            } else {
                MemoryInputStream stream = serializer.getInput();
                SwitchOutput output = new SwitchOutput();
                output.activation.activation = stream.f32();
                if (version > 0x2a2)
                    output.activation.player = serializer.i32(output.activation.player);
                output.targetList = serializer.array(null, SwitchTarget.class);
                sw.outputs = new SwitchOutput[] { output };
            }
        } else sw.outputs = serializer.array(sw.outputs, SwitchOutput.class, true);
        
        if (version < 0x398 && 0x140 <= version) {
            if (version < 0x160)
                sw.refSticker = serializer.struct(sw.refSticker, GlobalThingDescriptor.class);
            else
                sw.stickerPlan = serializer.resource(sw.stickerPlan, ResourceType.PLAN, true, false);
        }
        
        if (0x13f < version && version < 0x1a5) serializer.s32(0);
        
        if (version > 0x197) sw.hideInPlayMode = serializer.bool(sw.hideInPlayMode);
        if (version > 0x1a4) {
            sw.type = serializer.enum32(sw.type, true);
            sw.referenceThing = serializer.reference(sw.referenceThing, Thing.class);
            sw.manualActivation = serializer.struct(sw.manualActivation, SwitchSignal.class);
        }

        if (version >= 0x398 && (sw.type == SwitchType.STICKER || (type == SwitchType.POCKET_ITEM && subVersion > 0x10)))
            sw.stickerPlan = serializer.resource(sw.stickerPlan, ResourceType.PLAN, true, false);
        
        if (version > 0x1a4 && version < 0x368) sw.platformVisualFactor = serializer.f32(sw.platformVisualFactor);
        if (version > 0x1a4 && version < 0x2a0) serializer.f32(0);

        if (version > 0x1a4) sw.activationHoldTime = serializer.s32(sw.activationHoldTime);
        if (version > 0x1a4) sw.requireAll = serializer.bool(sw.requireAll);

        if (revision.has(Branch.DOUBLE11, 0x2a))
            sw.impactSensorMode = serializer.i8(sw.impactSensorMode);

        if (version > 0x1fa && version < 0x327) {
            // connectorPos
            if (!serializer.isWriting()) sw.connectorPos = new Vector4f[serializer.getInput().i32()];
            else {
                if (sw.connectorPos == null)
                    sw.connectorPos = new Vector4f[0];
                serializer.getOutput().i32(sw.connectorPos.length);
            }
            for (int i = 0; i < sw.connectorPos.length; ++i)
                sw.connectorPos[i] = serializer.v4(sw.connectorPos[i]);

            // connectorGrabbed
            if (!serializer.isWriting()) sw.connectorGrabbed = new boolean[serializer.getInput().i32()];
            else {
                if (sw.connectorGrabbed == null)
                    sw.connectorGrabbed = new boolean[0];
                serializer.getOutput().i32(sw.connectorGrabbed.length);
            }
            for (int i = 0; i < sw.connectorGrabbed.length; ++i)
                sw.connectorGrabbed[i] = serializer.bool(sw.connectorGrabbed[i]);

            sw.portPosOffset = serializer.v4(sw.portPosOffset);
            sw.looseConnectorPos = serializer.v4(sw.looseConnectorPos);
            sw.looseConnectorBaseOffset = serializer.v4(sw.looseConnectorBaseOffset);
            sw.looseConnectorGrabbed = serializer.bool(sw.looseConnectorGrabbed);
        }

        if (version > 0x23d) {
            sw.angleRange = serializer.f32(sw.angleRange);
            sw.includeTouching = serializer.s32(sw.includeTouching);
            if (version >= 0x398 && sw.type == SwitchType.MICROCHIP && sw.includeTouching == 1)
                sw.stickerPlan = serializer.resource(sw.stickerPlan, ResourceType.PLAN, true, false);
        }

        if (subVersion > 0x165 && sw.type == SwitchType.GAME_LIVE_STREAMING_CHOICE) {
            throw new SerializationException("UNSUPPORTED!");
            // wstr[4], wstr[5] if subVersion >= 0x187?
        }

        if (version > 0x243) sw.bulletsRequired = serializer.s32(sw.bulletsRequired);
        if (version == 0x244) serializer.i32(0); // ???
        if (version > 0x244) sw.bulletsDetected = serializer.s32(sw.bulletsDetected);
        if (version > 0x245 && version < 0x398) 
            sw.bulletPlayerNumber = serializer.i32(sw.bulletPlayerNumber);
        if (version > 0x248)
            sw.bulletRefreshTime = serializer.i32(sw.bulletRefreshTime);

        if (version >= 0x2f5) sw.resetWhenFull = serializer.bool(sw.resetWhenFull);

        if (version > 0x24a && version < 0x327) sw.hideConnectors = serializer.bool(sw.hideConnectors);

        if (version > 0x272 && version < 0x398)
            sw.logicType = serializer.enum32(sw.logicType);
        if (version > 0x272 && version < 0x369)
            sw.updateFrame = serializer.i32(sw.updateFrame);
        if (version > 0x272)
            sw.inputList = serializer.thingarray(sw.inputList);

        if (version > 0x276 && version < 0x327)
            sw.portThing = serializer.thing(sw.portThing);

        if (version > 0x283)
            sw.includeRigidConnectors = serializer.bool(sw.includeRigidConnectors);
        
        if (version > 0x284 && version < 0x327) {
            sw.customPortOffset = serializer.v4(sw.customPortOffset);
            sw.customConnectorOffset = serializer.v4(sw.customConnectorOffset);
        }

        if (version > 0x28c) {
            sw.timerCount = serializer.f32(sw.timerCount);
            if (version < 0x2c4)
                sw.timerAutoCount = serializer.i8(sw.timerAutoCount);
        }

        if (version > 0x2ad && subVersion < 0x100)
            sw.teamFilter = serializer.i32(sw.teamFilter);

        if (version > 0x2c3) sw.behavior = serializer.enum32(sw.behavior);

        if (version < 0x329 && version > 0x2c3) serializer.s32(0);

        if (version > 0x2c3) {
            sw.randomBehavior = serializer.i32(sw.randomBehavior);
            sw.randomPattern = serializer.i32(sw.randomPattern);
            sw.randomOnTimeMin = serializer.i32(sw.randomOnTimeMin);
            sw.randomOnTimeMax = serializer.i32(sw.randomOnTimeMax);
            sw.randomOffTimeMin = serializer.i32(sw.randomOffTimeMin);
            sw.randomOffTimeMax = serializer.i32(sw.randomOffTimeMax);
            if (version < 0x3ad) {
                sw.randomPhaseOn = serializer.u8(sw.randomPhaseOn);
                sw.randomPhaseTime = serializer.s32(sw.randomPhaseTime);
            }
            sw.retardedOldJoint = serializer.bool(sw.retardedOldJoint);
        }

        if (version > 0x30f) sw.keySensorMode = serializer.i32(sw.keySensorMode);
        if (version > 0x34c) {
            sw.userDefinedColour = serializer.i32(sw.userDefinedColour);
            sw.wiresVisible = serializer.bool(sw.wiresVisible);
        }
        if (version > 0x34f) sw.bulletTypes = serializer.i8(sw.bulletTypes);
        if (version > 0x390) sw.detectUnspawnedPlayers = serializer.bool(sw.detectUnspawnedPlayers);
        if (subVersion > 0x216)
            sw.unspawnedBehavior = serializer.i8(sw.unspawnedBehavior);
        if (version > 0x3a4) sw.playSwitchAudio = serializer.bool(sw.playSwitchAudio);

        if (revision.isVita()) {
            int vita = revision.getBranchRevision();

            if (vita >= 0x1) // > 0x3c0
                sw.switchTouchType = serializer.i32(sw.switchTouchType);
            
            if (vita >= 0x6 && vita < 0x36) // > 0x3c0
                serializer.u8(0);
            
            if (vita >= 0x9 && vita < 0x2c) // > 0x3c0
                sw.cursorScreenArea = (byte) serializer.u32(sw.cursorScreenArea);
            else if (vita >= 0x2c)
                sw.cursorScreenArea = serializer.i8(sw.cursorScreenArea);

            if (vita >= 0xb) // > 0x3c0
                sw.cursorInteractionType = serializer.i8(sw.cursorInteractionType);
            if (vita >= 0xc) // > 0x3c0
                sw.cursorTouchPanels = serializer.i8(sw.cursorTouchPanels);
            if (vita >= 0x23) // > 0x3c0
                sw.cursorTouchIndex = serializer.i8(sw.cursorTouchIndex);

            if (vita < 0x36) { // > 0x3c0
                if (vita >= 0x23) serializer.u8(0);
                if (vita >= 0x13) serializer.u8(0);
                if (vita >= 0x7) serializer.u8(0);
                if (vita >= 0x15) serializer.u8(0);
                if (vita >= 0x24) serializer.u8(0);
            } // Most of these should correspond to a value in sw.flags

            if (vita >= 0x36)
                sw.flags = serializer.i8(sw.flags);
            if (vita >= 0x2b)
                sw.outputAndOr = serializer.s32(sw.outputAndOr);
            
            if (vita >= 0x2b && vita < 0x36) 
                serializer.u8(0);

            // data sampler, although 0x2f shouldn't be the switch type?
            if (vita >= 0x45 && sw.type.getValue() == 0x2f)
                throw new SerializationException("UNSUPPORTED!");

            if (vita >= 0x38 && vita < 0x41) 
                serializer.u8(0); // if equal to 0, includeSameChipTags is 1
            
            if (vita >= 0x41)
                sw.includeSameChipTags = serializer.i8(sw.includeSameChipTags);
            if (vita >= 0x43) {
                sw.glowFrontCol = serializer.i32(sw.glowFrontCol);
                sw.glowBackCol = serializer.i32(sw.glowBackCol);
                sw.glowActiveCol = serializer.i32(sw.glowActiveCol);
            }

            if (vita >= 0x54)
                sw.playerFilter = serializer.i8(sw.playerFilter);
        }
        
        if (version > 0x3ec) sw.playerMode = serializer.i8(sw.playerMode);

        if (version > 0x3ee && sw.type == SwitchType.DATA_SAMPLER) {
            if (sw.value == null) sw.value = new DataLabelValue();

            sw.value.labelIndex = serializer.i32(sw.value.labelIndex);
            sw.value.creatorID = serializer.struct(sw.value.creatorID, NetworkOnlineID.class);
            sw.value.labelName = serializer.wstr(sw.value.labelName);
            sw.value.analogue = serializer.floatarray(sw.value.analogue);
            sw.value.ternary = serializer.bytearray(sw.value.ternary);
        }

        if (subVersion > 0x21)
            sw.relativeToSequencer = serializer.bool(sw.relativeToSequencer);
        if (subVersion > 0x2f)
            sw.layerRange = serializer.i8(sw.layerRange);
        if (subVersion > 0x7a) {
            sw.breakSound = serializer.bool(sw.breakSound);
            sw.colorTimer = serializer.i32(sw.colorTimer);
        }

        if (subVersion > 0x67)
            sw.isLbp3Switch = serializer.bool(sw.isLbp3Switch);
        if (subVersion > 0x68)
            sw.randomNonRepeating = serializer.bool(sw.randomNonRepeating);
        if (subVersion > 0x102)
            sw.stickerSwitchMode = serializer.i32(sw.stickerSwitchMode);
        
        return sw;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
}
