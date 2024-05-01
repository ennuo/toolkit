package cwlib.structs.things.parts;

import cwlib.enums.Branch;
import cwlib.enums.Part;
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
import cwlib.structs.things.components.switches.SwitchOutput;
import cwlib.structs.things.components.switches.SwitchTarget;
import cwlib.structs.things.components.world.*;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import org.joml.Vector4f;

import java.util.ArrayList;

public class PWorld implements Serializable
{
    @GsonRevision(max = 0x14a)
    public ResourceDescriptor[] materials;
    @GsonRevision(lbp3 = true, min = 0x6d)
    public float backdropOffsetX, backdropOffsetY, backdropOffsetZ;
    @GsonRevision(lbp3 = true, min = 0x70)
    public boolean backdropOffsetZAuto;
    @GsonRevision(lbp3 = true, min = 0xe2)
    public String overrideBackdropAmbience;
    @GsonRevision(lbp3 = true, min = 0x3f)
    public StreamingManager streamingManager;
    public ArrayList<Thing> things = new ArrayList<>();
    public float maxVel = 100.0f, maxAVel = 1.0f;
    public int frame;
    @GsonRevision(min = 0x2e2)
    public int simFrame;
    @GsonRevision(min = 0x377)
    public int frameLevelStarted;
    @GsonRevision(max = 0x32c)
    public int randy;
    public int thingUIDCounter = 2;
    public EditorSelection[] selections;
    public Thing backdrop, backdropNew;
    public float backdropTimer;
    @GsonRevision(min = 0x3a3)
    public int lbp2NightDaySwapped = 1;
    @GsonRevision(min = 0x14a)
    public boolean isPaused;
    @GsonRevision(min = 0x152)
    public float lightingFactor = 1.0f, colorCorrectionFactor;
    @GsonRevision(min = 0x196)
    public float fogFactor, fogTintFactor, darknessFactor;
    @GsonRevision(min = 0x16f)
    public EggLink[] completeRewards, collectRewards, aceRewards;
    @GsonRevision(min = 0x208)
    public boolean areRewardsShareable = true;
    @GsonRevision(min = 0x35e)
    public SlotID scoreboardLevelLinkSlot;
    @GsonRevision(min = 0x16f)
    public KeyLink[] completeUnlocks, collectUnlocks, aceUnlocks;
    @GsonRevision(min = 0x1c2, max = 0x36d)
    public int deathCount;
    @GsonRevision(min = 0x1c4)
    public int maxNumPlayers = 1;
    @GsonRevision(min = 0x1db)
    public Thing[] dissolvingThings, oldDissolvingThings;
    @GsonRevision(min = 0x1de, max = 0x344)
    public boolean isTutorialLevel;
    @GsonRevision(min = 0x22e)
    public boolean everSpawned;
    @GsonRevision(min = 0x22e)
    public int spawnFailureCount;
    @GsonRevision(min = 0x25a)
    public GlobalSettings targetGlobalSettings = new GlobalSettings(), fromGlobalSettings =
        new GlobalSettings();
    @Deprecated
    @GsonRevision(min = 0x25a)
    public float globalSettingsBlendFactor;
    @GsonRevision(min = 0x25a)
    public boolean hasLevelLightingBeenSetup;
    @GsonRevision(min = 0x25a)
    @Deprecated
    public int globalSettingsThingUID;
    @GsonRevision(min = 0x370)
    public CameraSettings cameraSettings = new CameraSettings();
    @GsonRevision(min = 0x26f)
    public float waterLevel, targetWaterLevel;
    @GsonRevision(min = 0x278)
    @GsonRevision(min = 0x4, branch = 0x4c44)
    public float fromWaterLevel;
    @GsonRevision(min = 0x270)
    public float waterWaveMagnitude, fromWaterWaveMagnitude, targetWaterWaveMagnitude;
    @GsonRevision(min = 0x26f)
    public float gravity = 1.0f, fromGravity = 1.0f, targetGravity = 1.0f;
    @GsonRevision(min = 0x26f)
    public float[] currGlobalSettingsBlendFactors;
    @GsonRevision(min = 0x26f)
    public int[] globalSettingsThingUIDs;
    @GsonRevision(min = 0x270)
    public int[] globalSettingsThingPriority;

    @GsonRevision(min = 0x289)
    @GsonRevision(branch = 0x4c44)
    public float waterTint, targetWaterTint;

    @GsonRevision(min = 0x289)
    @GsonRevision(branch = 0x4c44)
    public Vector4f fromWaterTintColor = new Vector4f(0.47451f, 0.898039f, 0.909804f, 0.125f);

    @GsonRevision(min = 0x289)
    @GsonRevision(branch = 0x4c44)
    public float waterMurkiness, fromWaterMurkiness, targetWaterMurkiness;

    @GsonRevision(min = 0x2b4)
    @GsonRevision(branch = 0x4c44, min = 0x7)
    public float waterBits = 1.0f, fromWaterBits = 1.0f, targetWaterBits = 1.0f;

    @GsonRevision(min = 0x34e)
    public boolean waterDrainSoundsEnabled = true, currWaterDrainSoundsEnabled = true;
    @GsonRevision(lbp3 = true, min = 0xe8)
    public boolean waterCausticsEnabled = true, currWaterCausticsEnabled = true;

    @GsonRevision(lbp3 = true, min = 0xf8)
    public int waterMainColor = -1090453761, fromWaterMainColor = -1090453761,
        targetWaterMainColor = -1090453761;
    @GsonRevision(lbp3 = true, min = 0xf8)
    public int waterHintColorOne = -33554177, fromWaterHintColorOne = -33554177,
        targetWaterHintColorOne = -33554177;
    @GsonRevision(lbp3 = true, min = 0xf8)
    public int waterHintColorTwo = 16662783, fromWaterHintColorTwo = 16662783,
        targetWaterHintColorTwo = 16662783;

    @GsonRevision(lbp3 = true, min = 0x182)
    public boolean backdropEnabled = true, currBackdropEnabled = true;

    @GsonRevision(min = 0x29c)
    @GsonRevision(min = 0x4, branch = 0x4c44)
    public float currWavePos = 0.0186706f;


    @GsonRevision(min = 0x2a3)
    public int gameMode = 0;
    @GsonRevision(lbp3 = true, min = 0x218)
    public int gameModeRequested = 0;

    @GsonRevision(min = 0x2b0)
    public int nextSackbotPlayerNumber = -2;
    @GsonRevision(min = 0x2ee)
    public CutsceneCameraManager cutsceneCameraManager = new CutsceneCameraManager();
    @GsonRevision(min = 0x30c)
    public GlobalAudioSettings globalAudioSettings = new GlobalAudioSettings();
    @GsonRevision(min = 0x321)
    public ResourceDescriptor backdropPlan, backdropNewPlan;
    @GsonRevision(min = 0x352)
    public boolean subLevel;
    @GsonRevision(min = 0x38a)
    public boolean scoreLocked;
    @GsonRevision(min = 0x3ac)
    public int debugTimeInLevel;
    @GsonRevision(min = 0x3ac)
    public boolean useEvenNewerCheckpointCode;
    @GsonRevision(min = 0x3bd)
    public MoveCursor[] moveCursors;

    @GsonRevision(min = 0x215)
    public boolean singlePlayer;
    @GsonRevision(min = 0x3d0)
    public int minPlayers = 1, maxPlayers = 4;
    @GsonRevision(min = 0x3d0)
    public boolean moveRecommended;
    @GsonRevision(min = 0x3dd)
    public boolean fixInvalidInOutMoverContacts;
    @GsonRevision(min = 0x3f1)
    public boolean continueMusic;

    @GsonRevision(lbp3 = true, min = 0x2f)
    public BroadcastMicrochipEntry[] broadcastMicroChipEntries;
    @GsonRevision(lbp3 = true, min = 0x5d)
    public int manualJumpDown;
    @GsonRevision(lbp3 = true, min = 0xe5)
    public Thing[] deferredDestroys;
    @GsonRevision(lbp3 = true, min = 0xcf)
    public float globalDofFront, globalDofBack, globalDofSackTrack;
    @GsonRevision(lbp3 = true, min = 0xda)
    public boolean enableSackpocket;
    @GsonRevision(lbp3 = true, min = 0x170)
    public boolean showQuestLog;
    @GsonRevision(lbp3 = true, min = 0x11d)
    public SlotID scoreboardUnlockLevelSlot;
    @GsonRevision(lbp3 = true, min = 0x154)
    public String progressBoardLevelLinkStartPoint;
    @GsonRevision(lbp3 = true, min = 0x15e)
    public boolean isLBP3World;

    /* Vita Fields */

    @GsonRevision(branch = 0x4431, min = 0x78)
    public boolean nonLinearFog;
    @GsonRevision(branch = 0x4431, min = 0x25)
    public EggLink[] bronzeRewards, silverRewards, goldRewards;
    @GsonRevision(branch = 0x4431, min = 0x25)
    public KeyLink[] bronzeUnlocks, silverUnlocks, goldUnlocks;
    @GsonRevision(branch = 0x4431, min = 0x25)
    public byte bronzeTrophyConditionType, silverTrophyConditionType, goldTrophyConditionType;
    @GsonRevision(branch = 0x4431, min = 0x25)
    public int scoreRequiredForBronzeTrophy, scoreRequiredForSilverTrophy,
        scoreRequiredForGoldTrophy;
    @GsonRevision(branch = 0x4431, min = 0x25)
    public float timeRequiredForBronzeTrophy, timeRequiredForSilverTrophy,
        timeRequiredForGoldTrophy;
    @GsonRevision(branch = 0x4431, min = 0x25)
    public int livesLostRequiredForBronzeTrophy, livesLostRequiredForSilverTrophy,
        livesLostRequiredForGoldTrophy;

    @GsonRevision(branch = 0x4431, min = 0x3d)
    public boolean enableAcing, enableGoldTrophy, enableSilverTrophy, enableBronzeTrophy;
    @GsonRevision(branch = 0x4431, min = 0x4c)
    public boolean enforceMinMaxPlayers;

    @GsonRevision(branch = 0x4431, min = 0x2d)
    public int waterColor;
    @GsonRevision(branch = 0x4431, min = 0x2d)
    public float waterBrightness;

    @GsonRevision(branch = 0x4431, min = 0x8)
    public int globalTouchCursor;


    @GsonRevision(branch = 0x4431, min = 0x18)
    public boolean portraitMode;
    @GsonRevision(branch = 0x4431, min = 0x47)
    public boolean sharedScreen;
    @GsonRevision(branch = 0x4431, min = 0x39)
    public boolean disableHUD;
    @GsonRevision(branch = 0x4431, min = 0x52)
    public boolean disableShadows;
    @GsonRevision(branch = 0x4431, min = 0x3b)
    public boolean flipBackground;
    @GsonRevision(branch = 0x4431, min = 0x4f)
    public boolean mpSeparateScreen;

    public PWorld()
    {
        this.currGlobalSettingsBlendFactors = new float[12];
        this.globalSettingsThingUIDs = new int[12];
        this.globalSettingsThingPriority = new int[12];
        for (int i = 0; i < 12; ++i)
            this.currGlobalSettingsBlendFactors[i] = 1.0f;
    }

    public void fixup(Thing worldThing, Revision revision)
    {
        int version = revision.getVersion();
        
        // Fixup all things in the world
        for (Thing thing : things)
        {
            if (thing == null || thing == worldThing) continue;
            thing.fixup(revision);
        }
        
        // Fix up any emitters if we're past the revision they got overhauled
        if (version > 0x2c3)
        {
            for (Thing thing : things)
            {
                if (thing == null || !thing.hasPart(Part.EMITTER)) continue;
                PEmitter emitter = thing.getPart(Part.EMITTER);
                emitter.modScaleActive = hasSwitchInput(thing);
                if (emitter.lastUpdateFrame == 0)
                    emitter.lastUpdateFrame = frame;
            }
        }
    }

    public boolean hasSwitchInput(Thing target)
    {
        return getSwitchInput(target) != null;
    }

    public SwitchOutput getSwitchInput(Thing target)
    {
        for (Thing thing : things)
        {
            if (thing == null || !thing.hasPart(Part.SWITCH)) continue;
            PSwitch switchBase = thing.getPart(Part.SWITCH);
            if (switchBase.outputs == null) continue;
            for (SwitchOutput output : switchBase.outputs)
            {
                if (output.targetList == null) continue;
                for (SwitchTarget switchTarget : output.targetList)
                {
                    if (switchTarget.thing == target) 
                        return output;
                }
            }
        }

        return null;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (subVersion >= 0x6d)
        {
            backdropOffsetX = serializer.f32(backdropOffsetX);
            backdropOffsetY = serializer.f32(backdropOffsetY);
            backdropOffsetZ = serializer.f32(backdropOffsetZ);
        }

        if (subVersion >= 0x72 && subVersion <= 0x73)
        {
            serializer.f32(0);
            serializer.f32(0);
            serializer.f32(0);
            serializer.u8(0);
            serializer.u8(0);
        }

        if (subVersion >= 0x70)
            backdropOffsetZAuto = serializer.bool(backdropOffsetZAuto);
        if (subVersion >= 0xe2)
            overrideBackdropAmbience = serializer.str(overrideBackdropAmbience);

        if (version < 0x14b)
        {
            if (serializer.isWriting()) serializer.getOutput().i32(0);
            else
            {
                materials = new ResourceDescriptor[serializer.getInput().i32()];
                for (int i = 0; i < materials.length; ++i)
                    materials[i] = serializer.resource(null, ResourceType.MATERIAL);
            }
        }

        if (subVersion >= 0x3f)
            streamingManager = serializer.reference(streamingManager,
                StreamingManager.class);

        if (!revision.isToolkit() || revision.before(Branch.MIZUKI, Revisions.MZ_SCENE_GRAPH))
        {
            things = serializer.arraylist(things, Thing.class, true);
            serializer.log("END OF WORLD THINGS");

            maxVel = serializer.f32(maxVel);
            maxAVel = serializer.f32(maxAVel);

            frame = serializer.s32(frame);
            if (version >= 0x2e2) simFrame = serializer.s32(simFrame);
            if (version >= 0x377) frameLevelStarted = serializer.i32(frameLevelStarted);

            thingUIDCounter = serializer.i32(thingUIDCounter);
        }


        if (version < 0x32d)
            randy = serializer.i32(randy);

        if (version < 0x1a4)
        {
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

        if (!revision.isToolkit() || revision.before(Branch.MIZUKI, Revisions.MZ_SCENE_GRAPH))
        {
            selections = serializer.array(selections, EditorSelection.class, true);

            backdrop = serializer.reference(backdrop, Thing.class);
            backdropNew = serializer.reference(backdropNew, Thing.class);
            backdropTimer = serializer.f32(backdropTimer);
        }

        if (version >= 0x3a3)
            lbp2NightDaySwapped = serializer.s32(lbp2NightDaySwapped);

        if (version >= 0x14a)
            isPaused = serializer.bool(isPaused);

        if (version >= 0x152)
        {
            lightingFactor = serializer.f32(lightingFactor);
            colorCorrectionFactor = serializer.f32(colorCorrectionFactor);
            if (version >= 0x196)
            {
                fogFactor = serializer.f32(fogFactor);
                fogTintFactor = serializer.f32(fogTintFactor);
                darknessFactor = serializer.f32(darknessFactor);
                if (revision.has(Branch.DOUBLE11, 0x78))
                    nonLinearFog = serializer.bool(nonLinearFog);
            }
        }

        if (version >= 0x16f)
        {
            completeRewards = serializer.array(completeRewards, EggLink.class);
            collectRewards = serializer.array(collectRewards, EggLink.class);
            aceRewards = serializer.array(aceRewards, EggLink.class);
            if (version >= 0x208)
                areRewardsShareable = serializer.bool(areRewardsShareable);
            if (version >= 0x35e)
                scoreboardLevelLinkSlot = serializer.struct(scoreboardLevelLinkSlot,
                    SlotID.class);
            completeUnlocks = serializer.array(completeUnlocks, KeyLink.class);
            collectUnlocks = serializer.array(collectUnlocks, KeyLink.class);
            aceUnlocks = serializer.array(aceUnlocks, KeyLink.class);
        }

        if (revision.isVita())
        {

            if (revision.has(Branch.DOUBLE11, 0x22) && revision.before(Branch.DOUBLE11, 0x25))
            {
                serializer.array(null, EggLink.class); // unlocks
                serializer.array(null, EggLink.class);  // rewards
                serializer.s32(0); // timeRequired
            }

            if (revision.has(Branch.DOUBLE11, 0x25))
            {
                goldRewards = serializer.array(goldRewards, EggLink.class);
                goldUnlocks = serializer.array(goldUnlocks, KeyLink.class);
                silverRewards = serializer.array(silverRewards, EggLink.class);
                silverUnlocks = serializer.array(silverUnlocks, KeyLink.class);
                bronzeRewards = serializer.array(bronzeRewards, EggLink.class);
                bronzeUnlocks = serializer.array(bronzeUnlocks, KeyLink.class);

                goldTrophyConditionType = serializer.i8(goldTrophyConditionType);
                silverTrophyConditionType = serializer.i8(silverTrophyConditionType);
                bronzeTrophyConditionType = serializer.i8(bronzeTrophyConditionType);

                scoreRequiredForGoldTrophy = serializer.s32(scoreRequiredForGoldTrophy);
                timeRequiredForGoldTrophy = serializer.f32(timeRequiredForGoldTrophy);
                livesLostRequiredForGoldTrophy =
                    serializer.s32(livesLostRequiredForGoldTrophy);

                scoreRequiredForSilverTrophy =
                    serializer.s32(scoreRequiredForSilverTrophy);
                timeRequiredForSilverTrophy =
                    serializer.f32(timeRequiredForSilverTrophy);
                livesLostRequiredForSilverTrophy =
                    serializer.s32(livesLostRequiredForSilverTrophy);

                scoreRequiredForBronzeTrophy =
                    serializer.s32(scoreRequiredForBronzeTrophy);
                timeRequiredForBronzeTrophy =
                    serializer.f32(timeRequiredForBronzeTrophy);
                livesLostRequiredForBronzeTrophy =
                    serializer.s32(livesLostRequiredForBronzeTrophy);
            }

            if (revision.has(Branch.DOUBLE11, 0x3d))
            {
                enableAcing = serializer.bool(enableAcing);
                enableGoldTrophy = serializer.bool(enableGoldTrophy);
                enableSilverTrophy = serializer.bool(enableSilverTrophy);
                enableBronzeTrophy = serializer.bool(enableBronzeTrophy);
            }

            if (revision.has(Branch.DOUBLE11, 0x4c))
                enforceMinMaxPlayers = serializer.bool(enforceMinMaxPlayers);
        }

        if (0x16e < version && version < 0x1bf)
        {
            serializer.i32(0);
            serializer.i32(0);
            serializer.i32(0);
        }

        if (0x1a3 < version && version < 0x1d1)
            throw new SerializationException("CGameCamera serialization unsupported!");

        if (0x1bd < version && version < 0x213)
            serializer.i32(0);

        if (version >= 0x1c2 && version < 0x36e)
            deathCount = serializer.i32(deathCount);
        if (version >= 0x1c4)
            maxNumPlayers = serializer.s32(maxNumPlayers);

        if (version >= 0x1db)
        {
            dissolvingThings = serializer.array(dissolvingThings, Thing.class, true);
            oldDissolvingThings = serializer.array(oldDissolvingThings, Thing.class,
                true);
        }

        if (version >= 0x1de && version < 0x345)
            isTutorialLevel = serializer.bool(isTutorialLevel);

        if (version >= 0x22e)
        {
            everSpawned = serializer.bool(everSpawned);
            spawnFailureCount = serializer.s32(spawnFailureCount);
        }

        if (version >= 0x25a)
        {
            targetGlobalSettings = serializer.struct(targetGlobalSettings,
                GlobalSettings.class);
            fromGlobalSettings = serializer.struct(fromGlobalSettings,
                GlobalSettings.class);
            globalSettingsBlendFactor = serializer.f32(globalSettingsBlendFactor);
            hasLevelLightingBeenSetup = serializer.bool(hasLevelLightingBeenSetup);
            globalSettingsThingUID = serializer.s32(globalSettingsThingUID);
        }

        if (version >= 0x370)
            cameraSettings = serializer.struct(cameraSettings, CameraSettings.class);

        if (version >= 0x26f)
        {
            waterLevel = serializer.f32(waterLevel);
            if (version >= 0x278 || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_WAVE))
                fromWaterLevel = serializer.f32(fromWaterLevel);
            targetWaterLevel = serializer.f32(targetWaterLevel);
        }

        if (version >= 0x270)
        {
            waterWaveMagnitude = serializer.f32(waterWaveMagnitude);
            fromWaterWaveMagnitude = serializer.f32(fromWaterWaveMagnitude);
            targetWaterWaveMagnitude = serializer.f32(targetWaterWaveMagnitude);
        }

        if (version >= 0x26f)
        {
            gravity = serializer.f32(gravity);
            fromGravity = serializer.f32(fromGravity);
            targetGravity = serializer.f32(targetGravity);

            currGlobalSettingsBlendFactors =
                serializer.floatarray(currGlobalSettingsBlendFactors);
            globalSettingsThingUIDs = serializer.intarray(globalSettingsThingUIDs);
            if (version >= 0x270)
                globalSettingsThingPriority =
                    serializer.intarray(globalSettingsThingPriority);
        }

        if (revision.has(Branch.DOUBLE11, 0x2d))
        {
            waterColor = serializer.s32(waterColor);
            waterBrightness = serializer.f32(waterBrightness);
        }
        else if (version >= 0x289 || revision.isLeerdammer())
        {
            waterTint = serializer.f32(waterTint);
            fromWaterTintColor = serializer.v4(fromWaterTintColor);
            targetWaterTint = serializer.f32(targetWaterTint);
        }

        if (version >= 0x289 || revision.isLeerdammer())
        {
            waterMurkiness = serializer.f32(waterMurkiness);
            fromWaterMurkiness = serializer.f32(fromWaterMurkiness);
            targetWaterMurkiness = serializer.f32(targetWaterMurkiness);
        }

        if (version >= 0x2b4 || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_BITS))
        {
            waterBits = serializer.f32(waterBits);
            fromWaterBits = serializer.f32(fromWaterBits);
            targetWaterBits = serializer.f32(targetWaterBits);
        }

        if (version >= 0x34e)
        {
            waterDrainSoundsEnabled = serializer.bool(waterDrainSoundsEnabled);
            currWaterDrainSoundsEnabled = serializer.bool(currWaterDrainSoundsEnabled);
        }

        if (subVersion >= 0xe8)
        {
            waterCausticsEnabled = serializer.bool(waterCausticsEnabled);
            currWaterCausticsEnabled = serializer.bool(waterCausticsEnabled);
        }

        if (subVersion >= 0xf8)
        {
            waterMainColor = serializer.s32(waterMainColor);
            fromWaterMainColor = serializer.s32(fromWaterMainColor);
            targetWaterMainColor = serializer.s32(targetWaterMainColor);

            waterHintColorOne = serializer.s32(waterHintColorOne);
            fromWaterHintColorOne = serializer.s32(fromWaterHintColorOne);
            targetWaterHintColorOne = serializer.s32(targetWaterHintColorOne);

            waterHintColorTwo = serializer.s32(waterHintColorTwo);
            fromWaterHintColorTwo = serializer.s32(fromWaterHintColorTwo);
            targetWaterHintColorTwo = serializer.s32(targetWaterHintColorTwo);
        }

        if (subVersion >= 0xf8 && subVersion < 0x189)
            serializer.bool(false);

        if (subVersion >= 0x182)
        {
            backdropEnabled = serializer.bool(backdropEnabled);
            currBackdropEnabled = serializer.bool(currBackdropEnabled);
        }

        if (version >= 0x29c || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_WAVE))
            currWavePos = serializer.f32(currWavePos);


        if ((version > 0x288 && version < 0x29c) || (revision.isLeerdammer() && revision.before(Branch.LEERDAMMER, Revisions.LD_WATER_WAVE)))
        {
            serializer.f32(0);
            serializer.bool(false);
        }

        // CBreadLoaf
        if (0x281 < version && version < 0x287)
        {
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

        if (version >= 0x2a3) gameMode = serializer.i32(gameMode);

        if (subVersion >= 0x218)
            gameModeRequested = serializer.i32(gameModeRequested);

        if (version >= 0x2b0)
            nextSackbotPlayerNumber = serializer.s32(nextSackbotPlayerNumber);

        if (0x2d3 < version && version < 0x2f3)
            throw new SerializationException("Unsupported structure in serialization");

        if (version >= 0x2ee)
            cutsceneCameraManager = serializer.struct(cutsceneCameraManager,
                CutsceneCameraManager.class);

        if (version >= 0x30c)
            globalAudioSettings = serializer.struct(globalAudioSettings,
                GlobalAudioSettings.class);

        if ((version >= 0x321 && !revision.isToolkit()) || revision.before(Branch.MIZUKI,
            Revisions.MZ_SCENE_GRAPH))
        {
            backdropPlan = serializer.resource(backdropPlan, ResourceType.PLAN, true);
            backdropNewPlan = serializer.resource(backdropNewPlan, ResourceType.PLAN,
                true);
        }

        if (version >= 0x352)
            subLevel = serializer.bool(subLevel);

        if (version >= 0x38a)
            scoreLocked = serializer.bool(scoreLocked);

        if (version >= 0x38b)
            debugTimeInLevel = serializer.s32(debugTimeInLevel);

        if (version >= 0x3ac)
            useEvenNewerCheckpointCode = serializer.bool(useEvenNewerCheckpointCode);

        if (version >= 0x3bd && subVersion <= 0x117)
            moveCursors = serializer.array(moveCursors, MoveCursor.class);

        // version > 0x3c0, rather than 0x3e1 for some reason
        if (revision.has(Branch.DOUBLE11, 0x8))
            globalTouchCursor = serializer.i32(globalTouchCursor);
        if (revision.has(Branch.DOUBLE11, 0xa) && revision.before(Branch.DOUBLE11, 0x28))
            sharedScreen = serializer.bool(sharedScreen);

        if (subVersion > 0x215)
            singlePlayer = serializer.bool(singlePlayer);

        if (version >= 0x3d0)
        {
            minPlayers = serializer.u8(minPlayers);
            maxPlayers = serializer.u8(maxPlayers);
            moveRecommended = serializer.bool(moveRecommended);
        }

        if (revision.isVita())
        {
            int vita = revision.getBranchRevision();

            // version > 0x3d4, rathern than 0x3e1 for some reason
            if (vita >= 0x18)
                portraitMode = serializer.bool(portraitMode);

            if (version >= 0x3dd)
                fixInvalidInOutMoverContacts =
                    serializer.bool(fixInvalidInOutMoverContacts);

            if (vita >= 0x28 && vita < 0x47)
            {
                if (serializer.s32(0) > 1 && !serializer.isWriting())
                    sharedScreen = true;
            }


            if (vita >= 0x47) sharedScreen = serializer.bool(sharedScreen);
            if (vita >= 0x39) disableHUD = serializer.bool(disableHUD);
            if (vita >= 0x52) disableShadows = serializer.bool(disableShadows);
            if (vita >= 0x3b) flipBackground = serializer.bool(flipBackground);
            if (vita >= 0x4f) mpSeparateScreen = serializer.bool(mpSeparateScreen);

            return;
        }

        if (version >= 0x3dd)
            fixInvalidInOutMoverContacts =
                serializer.bool(fixInvalidInOutMoverContacts);

        if (version >= 0x3f1)
            continueMusic = serializer.bool(continueMusic);

        if (subVersion >= 0x2f)
            broadcastMicroChipEntries = serializer.array(broadcastMicroChipEntries,
                BroadcastMicrochipEntry.class);

        if (subVersion >= 0x5d)
            manualJumpDown = serializer.i32(manualJumpDown);

        if (subVersion >= 0x98 && subVersion < 0xe5)
            serializer.thingarray(null);
        if (subVersion >= 0xc3 && subVersion < 0xe5)
            serializer.thingarray(null);
        if (subVersion >= 0x98 && subVersion < 0xe5)
            serializer.thingarray(null);

        if (subVersion >= 0xe5)
            deferredDestroys = serializer.array(deferredDestroys, Thing.class, true);

        if (subVersion >= 0xcf)
        {
            globalDofFront = serializer.f32(globalDofFront);
            globalDofBack = serializer.f32(globalDofBack);
            globalDofSackTrack = serializer.f32(globalDofSackTrack);
        }

        if (subVersion >= 0xda)
            enableSackpocket = serializer.bool(enableSackpocket);

        if (subVersion >= 0x170)
            showQuestLog = serializer.bool(showQuestLog);

        if (subVersion >= 0x11d)
            scoreboardUnlockLevelSlot = serializer.struct(scoreboardUnlockLevelSlot,
                SlotID.class);

        if (subVersion >= 0x154)
            progressBoardLevelLinkStartPoint =
                serializer.wstr(progressBoardLevelLinkStartPoint);

        if (subVersion >= 0x15e)
            isLBP3World = serializer.bool(isLBP3World);
    }

    @Override
    public int getAllocatedSize()
    {
        return 0;
    }

}
