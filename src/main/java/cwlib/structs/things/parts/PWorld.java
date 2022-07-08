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
import cwlib.structs.things.components.world.EditorSelection;
import cwlib.structs.things.components.world.GlobalSettings;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PWorld implements Serializable {
    public ResourceDescriptor[] materials;
    public float backdropOffsetX, backdropOffsetY, backdropOffsetZ;
    public boolean backdropOffsetZAuto, overrideBackdropAmbience;
    public ArrayList<Thing> things = new ArrayList<>();
    public float maxVel, maxAVel;
    public int frame, simFrame, frameLevelStarted, thingUIDCounter, randy;
    public EditorSelection[] selections;
    public Thing backdrop, backdropNew;
    public float backdropTimer;
    public boolean lbp2NightDaySwapped, isPaused;
    public float lightingFactor, colorCorrectionFactor, fogFactor, fogTintFactor, darknessFactor;
    public EggLink[] completeRewards, collectRewards, aceRewards;
    public boolean areRewardsShareable;
    public KeyLink[] completeUnlocks, collectUnlocks, aceUnlocks;
    public int deathCount, maxNumPlayers;
    public Thing[] dissolvingThings, oldDissolvingThings;
    public boolean isTutorialLevel, everSpawned;
    public int spawnFailureCount;
    public GlobalSettings targetGlobalSettings = new GlobalSettings(), fromGlobalSettings = new GlobalSettings();
    @Deprecated public float globalSettingsBlendFactor;
    public boolean hasLevelLightingBeenSetup;
    @Deprecated public int globalSettingsThingUID;
    // cameraSettings
    public float waterLevel, fromWaterLevel, targetWaterLevel;
    public float waterWaveMagnitude, fromWaterWaveMagnitude, targetWaterWaveMagnitude;
    public float gravity, fromGravity, targetGravity;
    public float[] currGlobalSettingsBlendFactors;
    public int[] globalSettingsThingUIDs, globalSettingsThingPriority;
    public float waterTint;
    public Vector4f fromWaterTintColor;
    public float targetWaterTint;
    public float waterMurkiness, fromWaterMurkiness, targetWaterMurkiness;
    public float waterBits, fromWaterBits, targetWaterBits;
    public boolean waterDrainSoundsEnabled, currWaterDrainSoundsEnabled, waterCausticsEnabled, currWaterCausticsEnabled;
    public int waterMainColor, fromWaterMainColor, targetWaterMainColor;
    public int waterHintColorOne, fromWaterHintColorOne, targetWaterHintColorOne;
    public int waterHintColorTwo, fromWaterHintColorTwo, targetWaterHintColorTwo;
    public boolean backdropEnabled, currBackdropEnabled;
    public float currWavePos;
    public GameMode gameMode, gameModeRequested;
    public int nextSackbotPlayerNumber;
    // cutSceneCameraManager
    // globalAudioSettings
    public ResourceDescriptor backdropPlan, backdropNewPlan;
    public boolean subLevel, scoreLocked;
    public int debugTimeInLevel;
    public boolean useEvenNewerCheckpointCode;
    // move cursors
    public boolean singlePlayer;
    public int minPlayers, maxPlayers;
    public boolean moveRecommended, fixInvalidInOutMoverContacts, continueMusic;
    public Thing[] broadcastMicroChipEntries;
    public int manualJumpDown;
    public Thing[] deferredDestroys;
    public float globalDofFront, globalDofBack, globalDofSackTrack;
    public boolean enableSackpocket, showQuestLog;
    public SlotID scoreboardUnlockLevelSlot;
    public String progressBoardLevelLinkStartPoint;
    public boolean isLBP3World;

    @SuppressWarnings("unchecked")
    @Override public PWorld serialize(Serializer serializer, Serializable structure) {
        PWorld world = (structure == null) ? new PWorld() : (PWorld) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x14b) {
            if (serializer.isWriting()) serializer.getOutput().i32(0);
            else {
                world.materials = new ResourceDescriptor[serializer.getInput().i32()];
                for (int i = 0; i < world.materials.length; ++i)
                    world.materials[i] = serializer.resource(null, ResourceType.MATERIAL);
            }
        }

        world.things = serializer.arraylist(world.things, Thing.class, true);

        world.maxVel = serializer.f32(world.maxVel);
        world.maxAVel = serializer.f32(world.maxAVel);

        world.frame = serializer.i32(world.frame);
        world.thingUIDCounter = serializer.i32(world.thingUIDCounter);
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

        world.selections = serializer.array(world.selections, EditorSelection.class);

        world.backdrop = serializer.reference(world.backdrop, Thing.class);
        world.backdropNew = serializer.reference(world.backdropNew, Thing.class);
        world.backdropTimer = serializer.f32(world.backdropTimer);

        if (version >= 0x3a3)
            world.lbp2NightDaySwapped = serializer.bool(world.lbp2NightDaySwapped);

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

        if (version >= 0x1c2)
            world.deathCount = serializer.i32(world.deathCount);
        if (version >= 0x1c4)
            world.maxNumPlayers = serializer.i32(world.maxNumPlayers);

        if (version >= 0x1db) {
            world.dissolvingThings = serializer.array(world.dissolvingThings, Thing.class, true);
            world.oldDissolvingThings = serializer.array(world.oldDissolvingThings, Thing.class, true);
        }

        if (version >= 0x1de)
            world.isTutorialLevel = serializer.bool(world.isTutorialLevel);

        if (version >= 0x22e) {
            world.everSpawned = serializer.bool(world.everSpawned);
            world.spawnFailureCount = serializer.i32(world.spawnFailureCount);
        }

        if (version >= 0x25a) {
            world.fromGlobalSettings = serializer.struct(world.fromGlobalSettings, GlobalSettings.class);
            world.targetGlobalSettings = serializer.struct(world.targetGlobalSettings, GlobalSettings.class);
            world.globalSettingsBlendFactor = serializer.f32(world.globalSettingsBlendFactor);
            world.hasLevelLightingBeenSetup = serializer.bool(world.hasLevelLightingBeenSetup);
            world.globalSettingsThingUID = serializer.i32(world.globalSettingsThingUID);
        }

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
        
        // serializer.log("PWORLD STRUCTURE NOT FINISHED!");
        // System.exit(0);

        return world;
    }

    @Override public int getAllocatedSize() { return 0; }
    
}
