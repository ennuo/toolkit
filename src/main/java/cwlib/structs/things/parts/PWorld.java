package cwlib.structs.things.parts;

import java.util.ArrayList;

import org.joml.Vector4f;

import cwlib.enums.Branch;
import cwlib.enums.GameMode;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.EggLink;
import cwlib.structs.things.components.KeyLink;
import cwlib.structs.things.components.world.CameraSettings;
import cwlib.structs.things.components.world.CutsceneCameraManager;
import cwlib.structs.things.components.world.EditorSelection;
import cwlib.structs.things.components.world.GlobalAudioSettings;
import cwlib.structs.things.components.world.GlobalSettings;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PWorld implements Serializable {
    public ResourceDescriptor[] materials;
    public float backdropOffsetX, backdropOffsetY, backdropOffsetZ;
    public boolean backdropOffsetZAuto;
    public String overrideBackdropAmbience;
    public ArrayList<Thing> things = new ArrayList<>();
    public float maxVel = 100.0f, maxAVel = 1.0f;
    public int frame, simFrame, frameLevelStarted, thingUIDCounter = 2, randy;
    public EditorSelection[] selections;
    public Thing backdrop, backdropNew;
    public float backdropTimer;
    public int lbp2NightDaySwapped = 1;
    public boolean isPaused;
    public float lightingFactor = 1.0f, colorCorrectionFactor, fogFactor, fogTintFactor, darknessFactor;
    public EggLink[] completeRewards, collectRewards, aceRewards;
    public boolean areRewardsShareable = true;
    public SlotID scoreboardLevelLinkSlot;
    public KeyLink[] completeUnlocks, collectUnlocks, aceUnlocks;
    public int deathCount, maxNumPlayers = 1;
    public Thing[] dissolvingThings, oldDissolvingThings;
    public boolean isTutorialLevel, everSpawned;
    public int spawnFailureCount;
    public GlobalSettings targetGlobalSettings = new GlobalSettings(), fromGlobalSettings = new GlobalSettings();
    @Deprecated public float globalSettingsBlendFactor;
    public boolean hasLevelLightingBeenSetup;
    @Deprecated public int globalSettingsThingUID;
    public CameraSettings cameraSettings = new CameraSettings();
    public float waterLevel, fromWaterLevel, targetWaterLevel;
    public float waterWaveMagnitude, fromWaterWaveMagnitude, targetWaterWaveMagnitude;
    public float gravity = 1.0f, fromGravity = 1.0f, targetGravity = 1.0f;
    public float[] currGlobalSettingsBlendFactors;
    public int[] globalSettingsThingUIDs, globalSettingsThingPriority;
    public float waterTint;
    public Vector4f fromWaterTintColor = new Vector4f(0.47451f, 0.898039f, 0.909804f, 0.125f);
    public float targetWaterTint;
    public float waterMurkiness, fromWaterMurkiness, targetWaterMurkiness;
    public float waterBits = 1.0f, fromWaterBits = 1.0f, targetWaterBits = 1.0f;
    public boolean waterDrainSoundsEnabled = true, currWaterDrainSoundsEnabled = true, waterCausticsEnabled = true, currWaterCausticsEnabled = true;
    public int waterMainColor = -1090453761, fromWaterMainColor = -1090453761, targetWaterMainColor = -1090453761;
    public int waterHintColorOne = -33554177, fromWaterHintColorOne = -33554177, targetWaterHintColorOne = -33554177;
    public int waterHintColorTwo = 16662783, fromWaterHintColorTwo = 16662783, targetWaterHintColorTwo = 16662783;
    public boolean backdropEnabled = true, currBackdropEnabled = true;
    public float currWavePos = 0.0186706f;
    public GameMode gameMode = GameMode.NONE, gameModeRequested = GameMode.NONE;
    public int nextSackbotPlayerNumber = -2;
    public CutsceneCameraManager cutsceneCameraManager = new CutsceneCameraManager();
    public GlobalAudioSettings globalAudioSettings = new GlobalAudioSettings();
    public ResourceDescriptor backdropPlan, backdropNewPlan;
    public boolean subLevel, scoreLocked;
    public int debugTimeInLevel;
    public boolean useEvenNewerCheckpointCode;
    // move cursors
    public boolean singlePlayer;
    public int minPlayers = 1, maxPlayers = 4;
    public boolean moveRecommended, fixInvalidInOutMoverContacts, continueMusic;
    public Thing[] broadcastMicroChipEntries;
    public int manualJumpDown;
    public Thing[] deferredDestroys;
    public float globalDofFront, globalDofBack, globalDofSackTrack;
    public boolean enableSackpocket, showQuestLog;
    public SlotID scoreboardUnlockLevelSlot;
    public String progressBoardLevelLinkStartPoint;
    public boolean isLBP3World;

    public PWorld() {
        this.currGlobalSettingsBlendFactors = new float[12];
        this.globalSettingsThingUIDs = new int[12];
        this.globalSettingsThingPriority = new int[12];
        for (int i = 0; i < 12; ++i)
            this.currGlobalSettingsBlendFactors[i] = 1.0f;
    }

    @SuppressWarnings("unchecked")
    @Override public PWorld serialize(Serializer serializer, Serializable structure) {
        PWorld world = (structure == null) ? new PWorld() : (PWorld) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (subVersion >= 0x6d) {
            world.backdropOffsetX = serializer.f32(world.backdropOffsetX);
            world.backdropOffsetY = serializer.f32(world.backdropOffsetY);
            world.backdropOffsetZ = serializer.f32(world.backdropOffsetZ);
        }

        if (subVersion >= 0x70)
            world.backdropOffsetZAuto = serializer.bool(world.backdropOffsetZAuto);
        if (subVersion >= 0xe2)
            world.overrideBackdropAmbience = serializer.str(world.overrideBackdropAmbience);
        
        if (version < 0x14b) {
            if (serializer.isWriting()) serializer.getOutput().i32(0);
            else {
                world.materials = new ResourceDescriptor[serializer.getInput().i32()];
                for (int i = 0; i < world.materials.length; ++i)
                    world.materials[i] = serializer.resource(null, ResourceType.MATERIAL);
            }
        }

        if (subVersion >= 0x3f) {
            if (serializer.i32(0) != 0) throw new SerializationException("Streaming manager not supported!");
        }

        world.things = serializer.arraylist(world.things, Thing.class, true);

        world.maxVel = serializer.f32(world.maxVel);
        world.maxAVel = serializer.f32(world.maxAVel);

        world.frame = serializer.s32(world.frame);
        if (version >= 0x2e2) world.simFrame = serializer.s32(world.simFrame);
        if (version >= 0x377) world.frameLevelStarted = serializer.i32(world.frameLevelStarted);
        
        world.thingUIDCounter = serializer.i32(world.thingUIDCounter);
        if (version < 0x32d)
            world.randy = serializer.i32(world.randy);

        if (version < 0x1a4) {
            serializer.f32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.i32(0);
            if (version < 0x14b)
                serializer.array(null, Thing.class);
        }

        world.selections = serializer.array(world.selections, EditorSelection.class, true);

        world.backdrop = serializer.reference(world.backdrop, Thing.class);
        world.backdropNew = serializer.reference(world.backdropNew, Thing.class);
        world.backdropTimer = serializer.f32(world.backdropTimer);

        if (version >= 0x3a3)
            world.lbp2NightDaySwapped = serializer.s32(world.lbp2NightDaySwapped);

        if (version >= 0x14a)
            world.isPaused = serializer.bool(world.isPaused);

        if (version >= 0x152) {
            world.lightingFactor = serializer.f32(world.lightingFactor);
            world.colorCorrectionFactor = serializer.f32(world.colorCorrectionFactor);
            if (version >= 0x196) {
                world.fogFactor = serializer.f32(world.fogFactor);
                world.fogTintFactor = serializer.f32(world.fogTintFactor);
                world.darknessFactor = serializer.f32(world.darknessFactor);
            }
        }

        if (version >= 0x16f) {
            world.completeRewards = serializer.array(world.completeRewards, EggLink.class);
            world.collectRewards = serializer.array(world.collectRewards, EggLink.class);
            world.aceRewards = serializer.array(world.aceRewards, EggLink.class);
            if (version >= 0x208) 
                world.areRewardsShareable = serializer.bool(world.areRewardsShareable);
            if (version >= 0x35e)
                world.scoreboardLevelLinkSlot = serializer.struct(world.scoreboardLevelLinkSlot, SlotID.class);
            world.completeUnlocks = serializer.array(world.completeUnlocks, KeyLink.class);
            world.collectUnlocks = serializer.array(world.collectUnlocks, KeyLink.class);
            world.aceUnlocks = serializer.array(world.aceUnlocks, KeyLink.class);
        }

        if (0x16e < version && version < 0x1bf) {
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
        }

        if (0x1a3 < version && version < 0x1d1)
            throw new SerializationException("CGameCamera serialization unsupported!");

        if (0x1bd < version && version < 0x213)
            serializer.i32(0);

        if (version >= 0x1c2 && version < 0x36e)
            world.deathCount = serializer.i32(world.deathCount);
        if (version >= 0x1c4)
            world.maxNumPlayers = serializer.i32(world.maxNumPlayers);

        if (version >= 0x1db) {
            world.dissolvingThings = serializer.array(world.dissolvingThings, Thing.class, true);
            world.oldDissolvingThings = serializer.array(world.oldDissolvingThings, Thing.class, true);
        }

        if (version >= 0x1de && version < 0x345)
            world.isTutorialLevel = serializer.bool(world.isTutorialLevel);

        if (version >= 0x22e) {
            world.everSpawned = serializer.bool(world.everSpawned);
            world.spawnFailureCount = serializer.i32(world.spawnFailureCount);
        }

        if (version >= 0x25a) {
            world.targetGlobalSettings = serializer.struct(world.targetGlobalSettings, GlobalSettings.class);
            world.fromGlobalSettings = serializer.struct(world.fromGlobalSettings, GlobalSettings.class);
            world.globalSettingsBlendFactor = serializer.f32(world.globalSettingsBlendFactor);
            world.hasLevelLightingBeenSetup = serializer.bool(world.hasLevelLightingBeenSetup);
            world.globalSettingsThingUID = serializer.i32(world.globalSettingsThingUID);
        }

        if (version >= 0x370)
            world.cameraSettings = serializer.struct(world.cameraSettings, CameraSettings.class);

        if (version >= 0x26f) {
            world.waterLevel = serializer.f32(world.waterLevel);
            if (version >= 0x278 || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_WAVE))
                world.fromWaterLevel = serializer.f32(world.fromWaterLevel);
            world.targetWaterLevel = serializer.f32(world.targetWaterLevel);
        }

        if (version >= 0x270) {
            world.waterWaveMagnitude = serializer.f32(world.waterWaveMagnitude);
            world.fromWaterWaveMagnitude = serializer.f32(world.fromWaterWaveMagnitude);
            world.targetWaterWaveMagnitude = serializer.f32(world.targetWaterWaveMagnitude);
        }

        if (version >= 0x26f) {
            world.gravity = serializer.f32(world.gravity);
            world.fromGravity = serializer.f32(world.fromGravity);
            world.targetGravity = serializer.f32(world.targetGravity);

            world.currGlobalSettingsBlendFactors = serializer.floatarray(world.currGlobalSettingsBlendFactors);
            world.globalSettingsThingUIDs = serializer.intarray(world.globalSettingsThingUIDs);
            if (version >= 0x270)
                world.globalSettingsThingPriority = serializer.intarray(world.globalSettingsThingPriority);
        }

        if (version >= 0x289 || revision.isLeerdammer()) {
            world.waterTint = serializer.f32(world.waterTint);
            world.fromWaterTintColor = serializer.v4(world.fromWaterTintColor);
            world.targetWaterTint = serializer.f32(world.targetWaterTint);
            world.waterMurkiness = serializer.f32(world.waterMurkiness);
            world.fromWaterMurkiness = serializer.f32(world.fromWaterMurkiness);
            world.targetWaterMurkiness = serializer.f32(world.targetWaterMurkiness);
        }

        if (version >= 0x2b4 || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_BITS)) {
            world.waterBits = serializer.f32(world.waterBits);
            world.fromWaterBits = serializer.f32(world.fromWaterBits);
            world.targetWaterBits = serializer.f32(world.targetWaterBits);
        }

        if (version >= 0x34e) {
            world.waterDrainSoundsEnabled = serializer.bool(world.waterDrainSoundsEnabled);
            world.currWaterDrainSoundsEnabled = serializer.bool(world.currWaterDrainSoundsEnabled);
        }

        if (subVersion >= 0xe8) {
            world.waterCausticsEnabled = serializer.bool(world.waterCausticsEnabled);
            world.currWaterCausticsEnabled = serializer.bool(world.waterCausticsEnabled);
        }

        if (subVersion >= 0xf8) {
            world.waterMainColor = serializer.i32(world.waterMainColor);
            world.fromWaterMainColor = serializer.i32(world.fromWaterMainColor);
            world.targetWaterMainColor = serializer.i32(world.targetWaterMainColor);

            world.waterHintColorOne = serializer.i32(world.waterHintColorOne);
            world.fromWaterHintColorOne = serializer.i32(world.fromWaterHintColorOne);
            world.targetWaterHintColorOne = serializer.i32(world.targetWaterHintColorOne);

            world.waterHintColorTwo = serializer.i32(world.waterHintColorTwo);
            world.fromWaterHintColorTwo = serializer.i32(world.fromWaterHintColorTwo);
            world.targetWaterHintColorTwo = serializer.i32(world.targetWaterHintColorTwo);
        }

        if (subVersion >= 0x182) {
            world.backdropEnabled = serializer.bool(world.backdropEnabled);
            world.currBackdropEnabled = serializer.bool(world.currBackdropEnabled);
        }

        if (version >= 0x29c || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_WAVE))
            world.currWavePos = serializer.f32(world.currWavePos);

        if (0x281 < version && version < 0x287)
            throw new SerializationException("Unsupported structure in serialization");

        if (version >= 0x2a3) world.gameMode = serializer.enum32(world.gameMode);
    
        if (subVersion >= 0x218) 
            world.gameModeRequested = serializer.enum32(world.gameModeRequested);

        if (version >= 0x2b0) 
            world.nextSackbotPlayerNumber = serializer.s32(world.nextSackbotPlayerNumber);

        if (0x2d3 < version && version < 0x2f3)
            throw new SerializationException("Unsupported structure in serialization");

        if (version >= 0x2ee)
            world.cutsceneCameraManager = serializer.struct(world.cutsceneCameraManager, CutsceneCameraManager.class);

        if (version >= 0x30c)
            world.globalAudioSettings = serializer.struct(world.globalAudioSettings, GlobalAudioSettings.class);
        
        if (version >= 0x321) {
            world.backdropPlan = serializer.resource(world.backdropPlan, ResourceType.PLAN, true);
            world.backdropNewPlan = serializer.resource(world.backdropNewPlan, ResourceType.PLAN, true);
        }

        if (version >= 0x352)
            world.subLevel = serializer.bool(world.subLevel);

        if (version >= 0x38a)
            world.scoreLocked = serializer.bool(world.scoreLocked);

        if (version >= 0x38b)
            world.debugTimeInLevel = serializer.s32(world.debugTimeInLevel);

        if (version >= 0x3ac)
            world.useEvenNewerCheckpointCode = serializer.bool(world.useEvenNewerCheckpointCode);

        if (version >= 0x3bd) {
            int size = serializer.i32(0);
            if (size != 0)
                throw new SerializationException("move cursors not supported!");
        }

        if (version >= 0x3d0) {
            world.minPlayers = serializer.u8(world.minPlayers);
            world.maxPlayers = serializer.u8(world.maxPlayers);
            world.moveRecommended = serializer.bool(world.moveRecommended);
        }

        if (version >= 0x3dd)
            world.fixInvalidInOutMoverContacts = serializer.bool(world.fixInvalidInOutMoverContacts);

        if (version >= 0x3f1)
            world.continueMusic = serializer.bool(world.continueMusic);

        if (subVersion >= 0x2f)
            world.broadcastMicroChipEntries = serializer.array(world.broadcastMicroChipEntries, Thing.class, true);

        if (subVersion >= 5d)
            world.manualJumpDown = serializer.i32(world.manualJumpDown);

        if (subVersion >= 0xe5)
            world.deferredDestroys = serializer.array(world.deferredDestroys, Thing.class, true);

        if (subVersion >= 0xcf) {
            world.globalDofFront = serializer.f32(world.globalDofFront);
            world.globalDofBack = serializer.f32(world.globalDofBack);
            world.globalDofSackTrack = serializer.f32(world.globalDofSackTrack);
        }

        if (subVersion >= 0xda)
            world.enableSackpocket = serializer.bool(world.enableSackpocket);

        if (subVersion >= 0x170)
            world.showQuestLog = serializer.bool(world.showQuestLog);

        if (subVersion >= 0x11d)
            world.scoreboardUnlockLevelSlot = serializer.struct(world.scoreboardUnlockLevelSlot, SlotID.class);

        if (subVersion >= 0x154)
            world.progressBoardLevelLinkStartPoint = serializer.str(world.progressBoardLevelLinkStartPoint);

        if (subVersion >= 0x15e)
            world.isLBP3World = serializer.bool(world.isLBP3World);

        // serializer.log("PWORLD STRUCTURE NOT FINISHED!");
        // System.exit(0);

        return world;
    }

    @Override public int getAllocatedSize() { return 0; }
    
}
