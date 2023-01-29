package cwlib.structs.things.parts;

import java.util.ArrayList;

import org.joml.Vector4f;

import cwlib.enums.Branch;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.ex.SerializationException;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.structs.streaming.StreamingManager;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.EggLink;
import cwlib.structs.things.components.KeyLink;
import cwlib.structs.things.components.world.BroadcastMicrochipEntry;
import cwlib.structs.things.components.world.CameraSettings;
import cwlib.structs.things.components.world.CutsceneCameraManager;
import cwlib.structs.things.components.world.EditorSelection;
import cwlib.structs.things.components.world.GlobalAudioSettings;
import cwlib.structs.things.components.world.GlobalSettings;
import cwlib.structs.things.components.world.MoveCursor;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PWorld implements Serializable {
    @GsonRevision(max=0x14a) public ResourceDescriptor[] materials;
    @GsonRevision(lbp3=true,min=0x6d) public float backdropOffsetX, backdropOffsetY, backdropOffsetZ;
    @GsonRevision(lbp3=true,min=0x70) public boolean backdropOffsetZAuto;
    @GsonRevision(lbp3=true,min=0xe2) public String overrideBackdropAmbience;
    @GsonRevision(lbp3=true,min=0x3f)
    public StreamingManager streamingManager;
    public ArrayList<Thing> things = new ArrayList<>();
    public float maxVel = 100.0f, maxAVel = 1.0f;
    public int frame;
    @GsonRevision(min=0x2e2) public int simFrame;
    @GsonRevision(min=0x377) public int frameLevelStarted;
    @GsonRevision(max=0x32c) public int randy;
    public int thingUIDCounter = 2;
    public EditorSelection[] selections;
    public Thing backdrop, backdropNew;
    public float backdropTimer;
    @GsonRevision(min=0x3a3) public int lbp2NightDaySwapped = 1;
    @GsonRevision(min=0x14a) public boolean isPaused;
    @GsonRevision(min=0x152) public float lightingFactor = 1.0f, colorCorrectionFactor;
    @GsonRevision(min=0x196) public float fogFactor, fogTintFactor, darknessFactor;
    @GsonRevision(min=0x16f) public EggLink[] completeRewards, collectRewards, aceRewards;
    @GsonRevision(min=0x208) public boolean areRewardsShareable = true;
    @GsonRevision(min=0x35e) public SlotID scoreboardLevelLinkSlot;
    @GsonRevision(min=0x16f) public KeyLink[] completeUnlocks, collectUnlocks, aceUnlocks;
    @GsonRevision(min=0x1c2, max=0x36d) public int deathCount;
    @GsonRevision(min=0x1c4) public int maxNumPlayers = 1;
    @GsonRevision(min=0x1db) public Thing[] dissolvingThings, oldDissolvingThings;
    @GsonRevision(min=0x1de, max=0x344) public boolean isTutorialLevel;
    @GsonRevision(min=0x22e) public boolean everSpawned;
    @GsonRevision(min=0x22e) public int spawnFailureCount;
    @GsonRevision(min=0x25a) public GlobalSettings targetGlobalSettings = new GlobalSettings(), fromGlobalSettings = new GlobalSettings();
    @Deprecated @GsonRevision(min=0x25a) public float globalSettingsBlendFactor;
    @GsonRevision(min=0x25a) public boolean hasLevelLightingBeenSetup;
    @GsonRevision(min=0x25a) @Deprecated public int globalSettingsThingUID;
    @GsonRevision(min=0x370) public CameraSettings cameraSettings = new CameraSettings();
    @GsonRevision(min=0x26f) public float waterLevel, targetWaterLevel;
    @GsonRevision(min=0x278)
    @GsonRevision(min=0x4, branch=0x4c44)
    public float fromWaterLevel;
    @GsonRevision(min=0x270) public float waterWaveMagnitude, fromWaterWaveMagnitude, targetWaterWaveMagnitude;
    @GsonRevision(min=0x26f) public float gravity = 1.0f, fromGravity = 1.0f, targetGravity = 1.0f;
    @GsonRevision(min=0x26f) public float[] currGlobalSettingsBlendFactors;
    @GsonRevision(min=0x26f) public int[] globalSettingsThingUIDs;
    @GsonRevision(min=0x270) public int[] globalSettingsThingPriority;
    
    @GsonRevision(min=0x289)
    @GsonRevision(branch=0x4c44)
    public float waterTint, targetWaterTint;

    @GsonRevision(min=0x289)
    @GsonRevision(branch=0x4c44)
    public Vector4f fromWaterTintColor = new Vector4f(0.47451f, 0.898039f, 0.909804f, 0.125f);

    @GsonRevision(min=0x289)
    @GsonRevision(branch=0x4c44)
    public float waterMurkiness, fromWaterMurkiness, targetWaterMurkiness;

    @GsonRevision(min=0x2b4)
    @GsonRevision(branch=0x4c44, min=0x7)
    public float waterBits = 1.0f, fromWaterBits = 1.0f, targetWaterBits = 1.0f;

    @GsonRevision(min=0x34e) public boolean waterDrainSoundsEnabled = true, currWaterDrainSoundsEnabled = true;
    @GsonRevision(lbp3=true,min=0xe8) public boolean waterCausticsEnabled = true, currWaterCausticsEnabled = true;
    
    @GsonRevision(lbp3=true,min=0xf8)
    public int waterMainColor = -1090453761, fromWaterMainColor = -1090453761, targetWaterMainColor = -1090453761;
    @GsonRevision(lbp3=true,min=0xf8)
    public int waterHintColorOne = -33554177, fromWaterHintColorOne = -33554177, targetWaterHintColorOne = -33554177;
    @GsonRevision(lbp3=true,min=0xf8)
    public int waterHintColorTwo = 16662783, fromWaterHintColorTwo = 16662783, targetWaterHintColorTwo = 16662783;

    @GsonRevision(lbp3=true,min=0x182) public boolean backdropEnabled = true, currBackdropEnabled = true;

    @GsonRevision(min=0x29c)
    @GsonRevision(min=0x4, branch=0x4c44)
    public float currWavePos = 0.0186706f;


    @GsonRevision(min=0x2a3) public int gameMode = 0;
    @GsonRevision(lbp3=true,min=0x218) public int gameModeRequested = 0;

    @GsonRevision(min=0x2b0) public int nextSackbotPlayerNumber = -2;
    @GsonRevision(min=0x2ee) public CutsceneCameraManager cutsceneCameraManager = new CutsceneCameraManager();
    @GsonRevision(min=0x30c) public GlobalAudioSettings globalAudioSettings = new GlobalAudioSettings();
    @GsonRevision(min=0x321) public ResourceDescriptor backdropPlan, backdropNewPlan;
    @GsonRevision(min=0x352) public boolean subLevel;
    @GsonRevision(min=0x38a) public boolean scoreLocked;
    @GsonRevision(min=0x3ac) public int debugTimeInLevel;
    @GsonRevision(min=0x3ac) public boolean useEvenNewerCheckpointCode;
    @GsonRevision(min=0x3bd) public MoveCursor[] moveCursors;
    
    @GsonRevision(min=0x215) public boolean singlePlayer;
    @GsonRevision(min=0x3d0) public int minPlayers = 1, maxPlayers = 4;
    @GsonRevision(min=0x3d0) public boolean moveRecommended;
    @GsonRevision(min=0x3dd) public boolean fixInvalidInOutMoverContacts;
    @GsonRevision(min=0x3f1) public boolean continueMusic;

    @GsonRevision(lbp3=true,min=0x2f) public BroadcastMicrochipEntry[] broadcastMicroChipEntries;
    @GsonRevision(lbp3=true,min=0x5d) public int manualJumpDown;
    @GsonRevision(lbp3=true,min=0xe5) public Thing[] deferredDestroys;
    @GsonRevision(lbp3=true,min=0xcf) public float globalDofFront, globalDofBack, globalDofSackTrack;
    @GsonRevision(lbp3=true,min=0xda) public boolean enableSackpocket;
    @GsonRevision(lbp3=true,min=0x170) public boolean showQuestLog;
    @GsonRevision(lbp3=true,min=0x11d) public SlotID scoreboardUnlockLevelSlot;
    @GsonRevision(lbp3=true,min=0x154) public String progressBoardLevelLinkStartPoint;
    @GsonRevision(lbp3=true,min=0x15e) public boolean isLBP3World;

    /* Vita Fields */

    @GsonRevision(branch=0x4431,min=0x78) public boolean nonLinearFog;
    @GsonRevision(branch=0x4431,min=0x25) public EggLink[] bronzeRewards, silverRewards, goldRewards;
    @GsonRevision(branch=0x4431,min=0x25) public KeyLink[] bronzeUnlocks, silverUnlocks, goldUnlocks;
    @GsonRevision(branch=0x4431,min=0x25) public byte bronzeTrophyConditionType, silverTrophyConditionType, goldTrophyConditionType;
    @GsonRevision(branch=0x4431,min=0x25) public int scoreRequiredForBronzeTrophy, scoreRequiredForSilverTrophy, scoreRequiredForGoldTrophy;
    @GsonRevision(branch=0x4431,min=0x25) public float timeRequiredForBronzeTrophy, timeRequiredForSilverTrophy, timeRequiredForGoldTrophy;
    @GsonRevision(branch=0x4431,min=0x25) public int livesLostRequiredForBronzeTrophy, livesLostRequiredForSilverTrophy, livesLostRequiredForGoldTrophy;

    @GsonRevision(branch=0x4431,min=0x3d) public boolean enableAcing, enableGoldTrophy, enableSilverTrophy, enableBronzeTrophy;
    @GsonRevision(branch=0x4431,min=0x4c) public boolean enforceMinMaxPlayers;
    
    @GsonRevision(branch=0x4431,min=0x2d) public int waterColor;
    @GsonRevision(branch=0x4431,min=0x2d) public float waterBrightness;

    @GsonRevision(branch=0x4431,min=0x8) public int globalTouchCursor;


    @GsonRevision(branch=0x4431,min=0x18) public boolean portraitMode; 
    @GsonRevision(branch=0x4431,min=0x47) public boolean sharedScreen;
    @GsonRevision(branch=0x4431,min=0x39) public boolean disableHUD;
    @GsonRevision(branch=0x4431,min=0x52) public boolean disableShadows;
    @GsonRevision(branch=0x4431,min=0x3b) public boolean flipBackground;
    @GsonRevision(branch=0x4431,min=0x4f) public boolean mpSeparateScreen;
    
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

        if (subVersion >= 0x72 && subVersion <= 0x73) {
            serializer.f32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.u8(0);
            serializer.u8(0);
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

        if (subVersion >= 0x3f)
            world.streamingManager = serializer.reference(world.streamingManager, StreamingManager.class);
        
        if (!revision.isToolkit() || revision.before(Branch.MIZUKI, Revisions.MZ_SCENE_GRAPH)) {
            world.things = serializer.arraylist(world.things, Thing.class, true);
            serializer.log("END OF WORLD THINGS");

            world.maxVel = serializer.f32(world.maxVel);
            world.maxAVel = serializer.f32(world.maxAVel);
    
            world.frame = serializer.s32(world.frame);
            if (version >= 0x2e2) world.simFrame = serializer.s32(world.simFrame);
            if (version >= 0x377) world.frameLevelStarted = serializer.i32(world.frameLevelStarted);

            world.thingUIDCounter = serializer.i32(world.thingUIDCounter);
        }

        
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

        if (!revision.isToolkit() || revision.before(Branch.MIZUKI, Revisions.MZ_SCENE_GRAPH)) {
            world.selections = serializer.array(world.selections, EditorSelection.class, true);

            world.backdrop = serializer.reference(world.backdrop, Thing.class);
            world.backdropNew = serializer.reference(world.backdropNew, Thing.class);
            world.backdropTimer = serializer.f32(world.backdropTimer);
        }

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
                if (revision.has(Branch.DOUBLE11, 0x78))
                    world.nonLinearFog = serializer.bool(world.nonLinearFog);
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

        if (revision.isVita()) {

            if (revision.has(Branch.DOUBLE11, 0x22) && revision.before(Branch.DOUBLE11, 0x25)) {
                serializer.array(null, EggLink.class); // unlocks
                serializer.array(null, EggLink.class);  // rewards
                serializer.s32(0); // timeRequired
            }

            if (revision.has(Branch.DOUBLE11, 0x25)) {
                world.goldRewards = serializer.array(world.goldRewards, EggLink.class);
                world.goldUnlocks = serializer.array(world.goldUnlocks, KeyLink.class);
                world.silverRewards = serializer.array(world.silverRewards, EggLink.class);
                world.silverUnlocks = serializer.array(world.silverUnlocks, KeyLink.class);
                world.bronzeRewards = serializer.array(world.bronzeRewards, EggLink.class);
                world.bronzeUnlocks = serializer.array(world.bronzeUnlocks, KeyLink.class);

                world.goldTrophyConditionType = serializer.i8(world.goldTrophyConditionType);
                world.silverTrophyConditionType = serializer.i8(world.silverTrophyConditionType);
                world.bronzeTrophyConditionType = serializer.i8(world.bronzeTrophyConditionType);

                world.scoreRequiredForGoldTrophy = serializer.s32(world.scoreRequiredForGoldTrophy);
                world.timeRequiredForGoldTrophy = serializer.f32(world.timeRequiredForGoldTrophy);
                world.livesLostRequiredForGoldTrophy = serializer.s32(world.livesLostRequiredForGoldTrophy);

                world.scoreRequiredForSilverTrophy = serializer.s32(world.scoreRequiredForSilverTrophy);
                world.timeRequiredForSilverTrophy = serializer.f32(world.timeRequiredForSilverTrophy);
                world.livesLostRequiredForSilverTrophy = serializer.s32(world.livesLostRequiredForSilverTrophy);

                world.scoreRequiredForBronzeTrophy = serializer.s32(world.scoreRequiredForBronzeTrophy);
                world.timeRequiredForBronzeTrophy = serializer.f32(world.timeRequiredForBronzeTrophy);
                world.livesLostRequiredForBronzeTrophy = serializer.s32(world.livesLostRequiredForBronzeTrophy);
            }

            if (revision.has(Branch.DOUBLE11, 0x3d)) {
                world.enableAcing = serializer.bool(world.enableAcing);
                world.enableGoldTrophy = serializer.bool(world.enableGoldTrophy);
                world.enableSilverTrophy = serializer.bool(world.enableSilverTrophy);
                world.enableBronzeTrophy = serializer.bool(world.enableBronzeTrophy);
            }

            if (revision.has(Branch.DOUBLE11, 0x4c))
                world.enforceMinMaxPlayers = serializer.bool(world.enforceMinMaxPlayers);
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

        if (revision.has(Branch.DOUBLE11, 0x2d)) {
            world.waterColor = serializer.s32(world.waterColor);
            world.waterBrightness = serializer.f32(world.waterBrightness);
        } else if (version >= 0x289 || revision.isLeerdammer()) {
            world.waterTint = serializer.f32(world.waterTint);
            world.fromWaterTintColor = serializer.v4(world.fromWaterTintColor);
            world.targetWaterTint = serializer.f32(world.targetWaterTint);
        }
        
        if (version >= 0x289 || revision.isLeerdammer()) {
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

        if (subVersion >= 0xf8 && subVersion < 0x189)
            serializer.bool(false);

        if (subVersion >= 0x182) {
            world.backdropEnabled = serializer.bool(world.backdropEnabled);
            world.currBackdropEnabled = serializer.bool(world.currBackdropEnabled);
        }

        if (version >= 0x29c || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_WAVE))
            world.currWavePos = serializer.f32(world.currWavePos);
        

        if ((version > 0x288 && version < 0x29c) || (revision.isLeerdammer() && revision.before(Branch.LEERDAMMER, Revisions.LD_WATER_WAVE))) {
            serializer.f32(0);
            serializer.bool(false);
        }

        // CBreadLoaf
        if (0x281 < version && version < 0x287) {
            if (serializer.i32(0) != 0)
                throw new SerializationException("CBreadLoaf serialization not supported!");

            // serializer.i32(0); // loafAlloc.numHandles
            // serializer.i32(0); // loafAlloc.freehead
            // serializer.i32(0); // loafAlloc.numUsed
            // // handles?

            // // reflect array 2, loafMin floats
            // serializer.i32(0); // loafSize
            // serializer.u16(0); // maxDepth
            // serializer.u16(0); // first

            // // reflect array, x via numHandles float
            // // reflect array, y via numHandles float
            // // reflect 4 LoafHandle's short?
            // // firstcrumb short
            // // depth, bytearray?
        }

        if (version >= 0x2a3) world.gameMode = serializer.i32(world.gameMode);
    
        if (subVersion >= 0x218) 
            world.gameModeRequested = serializer.i32(world.gameModeRequested);

        if (version >= 0x2b0) 
            world.nextSackbotPlayerNumber = serializer.s32(world.nextSackbotPlayerNumber);

        if (0x2d3 < version && version < 0x2f3)
            throw new SerializationException("Unsupported structure in serialization");

        if (version >= 0x2ee)
            world.cutsceneCameraManager = serializer.struct(world.cutsceneCameraManager, CutsceneCameraManager.class);

        if (version >= 0x30c)
            world.globalAudioSettings = serializer.struct(world.globalAudioSettings, GlobalAudioSettings.class);
        
        if ((version >= 0x321 && !revision.isToolkit()) || revision.before(Branch.MIZUKI, Revisions.MZ_SCENE_GRAPH)) {
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
        
        if (version >= 0x3bd && subVersion <= 0x117)
            world.moveCursors = serializer.array(world.moveCursors, MoveCursor.class);

        // version > 0x3c0, rather than 0x3e1 for some reason
        if (revision.has(Branch.DOUBLE11, 0x8))
            world.globalTouchCursor = serializer.i32(world.globalTouchCursor);
        if (revision.has(Branch.DOUBLE11, 0xa) && revision.before(Branch.DOUBLE11, 0x28)) 
            world.sharedScreen = serializer.bool(world.sharedScreen);

        if (subVersion > 0x215)
            world.singlePlayer = serializer.bool(world.singlePlayer);
        
        if (version >= 0x3d0) {
            world.minPlayers = serializer.u8(world.minPlayers);
            world.maxPlayers = serializer.u8(world.maxPlayers);
            world.moveRecommended = serializer.bool(world.moveRecommended);
        }

        if (revision.isVita()) {
            int vita = revision.getBranchRevision();

            // version > 0x3d4, rathern than 0x3e1 for some reason
            if (vita >= 0x18) 
                world.portraitMode = serializer.bool(world.portraitMode);

            if (vita >= 0x28 && vita < 0x47) {
                if (serializer.u8(0) > 1 && !serializer.isWriting())
                    world.sharedScreen = true;
            }

            if (version >= 0x3dd)
                world.fixInvalidInOutMoverContacts = serializer.bool(world.fixInvalidInOutMoverContacts);
            
            if (vita >= 0x47) world.sharedScreen = serializer.bool(world.sharedScreen);
            if (vita >= 0x39) world.disableHUD = serializer.bool(world.disableHUD);
            if (vita >= 0x52) world.disableShadows = serializer.bool(world.disableShadows);
            if (vita >= 0x3b) world.flipBackground = serializer.bool(world.flipBackground); 
            if (vita >= 0x4f) world.mpSeparateScreen = serializer.bool(world.mpSeparateScreen);

            return world;
        }

        if (version >= 0x3dd)
            world.fixInvalidInOutMoverContacts = serializer.bool(world.fixInvalidInOutMoverContacts);

        if (version >= 0x3f1)
            world.continueMusic = serializer.bool(world.continueMusic);

        if (subVersion >= 0x2f)
            world.broadcastMicroChipEntries = serializer.array(world.broadcastMicroChipEntries, BroadcastMicrochipEntry.class);

        if (subVersion >= 0x5d)
            world.manualJumpDown = serializer.i32(world.manualJumpDown);

        if (subVersion >= 0x98 && subVersion < 0xe5)
            serializer.thingarray(null);
        if (subVersion >= 0xc3 && subVersion < 0xe5)
            serializer.thingarray(null);
        if (subVersion >= 0x98 && subVersion < 0xe5)
            serializer.thingarray(null);

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
            world.progressBoardLevelLinkStartPoint = serializer.wstr(world.progressBoardLevelLinkStartPoint);

        if (subVersion >= 0x15e)
            world.isLBP3World = serializer.bool(world.isLBP3World);

        return world;
    }

    @Override public int getAllocatedSize() { return 0; }
    
}
