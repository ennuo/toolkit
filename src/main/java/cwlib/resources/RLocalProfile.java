package cwlib.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import cwlib.enums.Branch;
import cwlib.enums.GameProgressionStatus;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.enums.TutorialLevel;
import cwlib.enums.TutorialState;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.profile.*;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.GUID;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.OpenPSID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;

public class RLocalProfile implements Compressable, Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    /**
     * List of items this user has.
     */
    public ArrayList<InventoryItem> inventory;

    /**
     * Stores categories and locations referenced by items.
     */
    public StringLookupTable stringTable = new StringLookupTable();

    /**
     * Whether or not this profile was from a production build.
     */
    @GsonRevision(min=0x3b6)
    public boolean fromProductionBuild = true;

    @GsonRevision(min=0x1e4)
    public HashMap<TutorialLevel, TutorialState> lbp1TutorialStates = new HashMap<>();

    @GsonRevision(min=0x266)
    public int[] dlcPackViewed, dlcPackShown;

    @GsonRevision(min=0x1e5)
    public int lbp1MainMenuButtonUnlocks;

    @GsonRevision(min=0x16e)
    public ArrayList<PlayedLevelData> playedLevelData;
    
    @GsonRevision(min=0x37a)
    public ArrayList<ViewedLevelData> viewedLevelData;
    @GsonRevision(min=0x37a)
    public ArrayList<ViewedPlayerData> viewedPlayerData;

    @GsonRevision(min=0x16e)
    public HashMap<SlotID, Slot> oldMyMoonSlots = new HashMap<>();

    @GsonRevision(min=0x20e)
    public boolean copiedFromAnotherUser;

    @GsonRevision(min=0x268)
    public boolean fixedUpByDeveloper;

    @GsonRevision(min=0x297)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_SAVEGAME_INFO)
    public boolean showSaveGameInfo;
    
    @GsonRevision(min=0x297)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_SAVEGAME_INFO)
    public int totalLevelCount;

    @GsonRevision(min=0x394)
    public int lastMmPicksCount;

    @GsonRevision(min=0x2cc)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_NEWS_TIMESTAMP)
    public long lastNewsItemTimestamp;

    @GsonRevision(min=0x324)
    public long lastStreamEventTimestamp;

    @GsonRevision(min=0x2cd)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_QUEUE)
    public long lolcatFtwTimestamp;

    @GsonRevision(min=0x328)
    public String[] oldTextSearches;

    public SlotLink[] oldDiscoveredLinks;
    public SlotLink[] pendingLinks;
    public SlotID lastPlayed = new SlotID();

    @GsonRevision(min=0x17e)
    public int[] lbp1VideosPlayed;
    
    @GsonRevision(min=0x36c)
    public int[] lbp2VideosPlayed;

    @GsonRevision(min=0x170)
    public PlayerMetrics currentPlayerMetrics;
    
    @GsonRevision(min=0x1ac)
    public PlayerMetrics lastPostedPlayerMetrics;

    @GsonRevision(min=0x1b9)
    public GameProgressionStatus lbp1GameProgressionStatus = GameProgressionStatus.NEW_GAME;
    
    @GsonRevision(min=0x1d0)
    public int lbp1GameProgressionEventHappenedBits;

    @GsonRevision(min=0x1d0)
    public int lbp1GameProgressionEventsExplainedBits;

    @GsonRevision(min=0x26d)
    public int demoProgressionStatus;

    @GsonRevision(min=0x36c)
    public int lbp2GameProgressionFlags;

    @GsonRevision(min=0x387)
    public InventoryItem[] pendingInventoryItems;

    public ResourceDescriptor podLevel;

    @GsonRevision(min=0x150)
    public NetworkPlayerID playerId;


    @GsonRevision(min=0x297)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_SAVEGAME_INFO)
    public NetworkPlayerID ownerPlayerId;

    @GsonRevision(min=0x297)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_SAVEGAME_INFO)
    public OpenPSID ownerPsid;

    @GsonRevision(min=0x2ec)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_OWNER_ID)
    public int ownerUserId;

    @GsonRevision(min=0x176)
    public SHA1 eulaAgreed;

    @GsonRevision(min=0x3d5)
    public NetworkPlayerID acceptingPlayer;

    public ResourceDescriptor syncedProfile;

    @GsonRevision(min=0x1a2)
    public NetworkPlayerID[] voipMutedPlayers;

    @GsonRevision(min=0x1a2)
    public boolean voipChatPaused;

    @GsonRevision(min=0x1a2)
    public boolean enableMusicInPlayMode;

    @GsonRevision(min=0x1b5)
    public boolean enableMusicInPod;

    @GsonRevision(min=0x1b6)
    public LegacyInventoryCollection[] legacyInventoryCollections;
    
    @GsonRevision(min=0x1b6)
    public InventoryView[] legacyInventoryViews;

    @GsonRevision(min=0x317)
    public InventoryCollection[] inventoryCollections;

    @GsonRevision(min=0x1e1)
    public ResourceDescriptor avatarIcon;

    @GsonRevision(min=0x23d)
    public ResourceDescriptor saveIcon;

    @GsonRevision(min=0x1e6)
    public int lbp1CreateModeVOProgress;

    @GsonRevision(min=0x200)
    public float gamma;

    @GsonRevision(min=0x200)
    public float screenSize;

    @GsonRevision(min=0x35f)
    public boolean hasSeenCalibrationScreen;

    @GsonRevision(min=0x206)
    public int[] lbp1VOPlayed;
    
    @GsonRevision(min=0x36c)
    public int[] lbp2VOPlayed;

    @GsonRevision(min=0x23b)
    public int subtitleMode;
    
    @GsonRevision(min=0x32f)
    public PinsAwarded pinsAwarded;

    @GsonRevision(min=0x261)
    public boolean userSettingPhotoBooth;

    @GsonRevision(min=0x261)
    public boolean userSettingCollection;

    @GsonRevision(lbp3=true, min=0x17f)
    public boolean userSettingAdvancedEditMode;

    @GsonRevision(min=0x2d3)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_THERMOMETER)
    public boolean showThermometer;

    @GsonRevision(min=0x384)
    public boolean saveOnlinePlaySettings;

    @GsonRevision(min=0x30e)
    public int onlinePlayMode;

    @GsonRevision(min=0x30e)
    public int friendJoinRequestMode;

    @GsonRevision(min=0x30e)
    public int nonFriendJoinRequestMode;

    @GsonRevision(min=0x353)
    public int[] lbp2TutorialsPlayed;

    @GsonRevision(min=0x353)
    public int[] sectionHeadingToggled;

    @GsonRevision(lbp3=true, min=0x20e)
    public MysteryPodEventSeen[] mysteryPodEventsSeen;

    @GsonRevision(min=0x367)
    public SHA1[] lastLegacyImportedProfileHashLBP1;

    @GsonRevision(min=0x3f6)
    public SHA1[] lastLegacyImportedProfileHashLBP2;

    @GsonRevision(min=0x36c)
    public boolean playedLBP1;

    @GsonRevision(lbp3=true, min=0x12d)
    public boolean hasPerformedFirstRun;

    @GsonRevision(min=0x3af)
    public boolean lbp2TutorialUnlockButtonUnleashed;

    @GsonRevision(min=0x28c)
    public int[] hiddenCategories;

    @GsonRevision(min=0x2d2)
    public CollectedBubble[] collectedBubbles;

    @GsonRevision(min=0x2f6)
    @GsonRevision(branch=0x4c44, min=Revisions.LD_WATER_LEVELS)
    public int numWaterLevelsPlayed;

    @GsonRevision(min=0x3d0)
    public boolean autoFilterOn;

    @GsonRevision(min=0x3d3)
    public boolean hasUsed6Axis, hasUsedMove;

    @GsonRevision(min=0x3dc)
    public PaintProperties paintProperties;

    @GsonRevision(min=0x3ef)
    @GsonRevision(branch=0x4431, min=Revisions.D1_DATALABELS)
    public PlayerDataLabels playerDataLabels;

    /* Vita stuff */

    @GsonRevision(branch=0x4431, min=Revisions.D1_3G_CONNECTIVITY)
    public int total3GUpStream, total3GDownStream;

    @GsonRevision(branch=0x4431, min=Revisions.D1_TOUCH_CREATE)
    public boolean createRearTouchPan, createRearTouchPinchZoom, createRearTapZoom;
    
    @GsonRevision(branch=0x4431, min=Revisions.D1_CREATE_WARNING)
    public boolean createFrameRateWarningMessages;

    @GsonRevision(branch=0x4431, min=Revisions.D1_CREATE_BETA)
    public long onlineBetaNetworkTimestamp;

    @GsonRevision(branch=0x4431, min=Revisions.D1_CREATE_BETA)
    public int onlineBetaPlayTimeSinceOnline, onlineBetaBootsSinceOnline;

    @GsonRevision(min=0x3e3)
    public float distanceMovedWhilstAttracted;

    @GsonRevision(min=0x3e5)
    public int beakersUsedPart1, beakersUsedPart2;

    @GsonRevision(min=0x3ec)
    public int profileFlags;

    @GsonRevision(min=0x3f2)
    public byte goldMedalsAwarded, goldMedalStoryArcPins;

    @GsonRevision(min=0x3f3)
    public boolean twitterEnabled, facebookEnabled;
    @GsonRevision(min=0x3f3)
    public String twitterAccessToken, twitterAccessTokenSecret;
    
    @GsonRevision(min=0x3f4)
    public boolean playedLBP2;

    @GsonRevision(min=0x3f5)
    public boolean createChallengeTutorialSeen, playChallengeTutorialSeen;
    
    @GsonRevision(min=0x3f8)
    public int ownedLBP;

    @GsonRevision(lbp3=true, min=0xf0)
    public int dceUuidState;

    @GsonRevision(lbp3=true, min=0x15c)
    public boolean lastPlayedPPP;

    @GsonRevision(lbp3=true, min=0x20f)
    public SlotID lastPlayedPlanet;

    @GsonRevision(lbp3=true, min=0x20f)
    public boolean lastPlayedEarthAdv;

    @GsonRevision(lbp3=true, min=0x20f)
    public boolean hasSeenDiveInBetaMessage;

    @GsonRevision(lbp3=true, min=0x184)
    public int touchCreateCursorMode;

    @GsonRevision(lbp3=true, min=0x188)
    public boolean showAdvancedEditModeMessageCounter;

    @GsonRevision(lbp3=true, min=0x195)
    public boolean showAdventureSaveWarning;

    @GsonRevision(lbp3=true, min=0x1a0)
    public int totalFramesInEditMode;

    @GsonRevision(lbp3=true, min=0x1a2)
    public SHA1[] onlineTutorialsPlayed;

    @GsonRevision(lbp3=true, min=0x1a2)
    public SlotID[] popitPuzzlesCompleted;

    @GsonRevision(lbp3=true, min=0x1ab)
    public boolean timesaverNoticeViewed;

    @GsonRevision(lbp3=true, min=0x1ae)
    public int questProgressPin;

    @GsonRevision(lbp3=true, min=0x20a)
    public int[] activityFilterToggled;

    @GsonRevision(lbp3=true, min=0x213)
    public int lastLandingPageFocusItems;

    @GsonRevision(lbp3=true, min=0x216)
    public GoPlayCache[] goPlayCache;


    @SuppressWarnings("unchecked")
    @Override public RLocalProfile serialize(Serializer serializer, Serializable structure) {
        RLocalProfile profile = (structure == null) ? new RLocalProfile() : (RLocalProfile) structure;

        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        profile.inventory = serializer.arraylist(profile.inventory, InventoryItem.class);
        profile.stringTable = serializer.struct(profile.stringTable, StringLookupTable.class);

        if (version >= 0x3b6)
            profile.fromProductionBuild = serializer.bool(profile.fromProductionBuild);

        if (version >= 0x133 && version < 0x1df) {
            // set of slot ids
            // ???
        }

        if (version < 0x269 && version > 0x128) {
            // lockStates
        }

        if (version > 0x1e3) {
            if (serializer.isWriting()) {
                MemoryOutputStream stream = serializer.getOutput();
                ArrayList<TutorialLevel> keys = new ArrayList<>(profile.lbp1TutorialStates.keySet());
                keys.sort((l, r) -> l.getValue() - r.getValue());
                stream.i32(keys.size());
                for (TutorialLevel key : keys) {
                    stream.enum32(key);
                    stream.enum32(profile.lbp1TutorialStates.get(key));
                }
            } else {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                profile.lbp1TutorialStates = new HashMap<TutorialLevel, TutorialState>(count);
                for (int i = 0; i < count; ++i)
                    profile.lbp1TutorialStates.put(stream.enum32(TutorialLevel.class), stream.enum32(TutorialState.class));
            }
        }

        // These two are actually signed integer arrays
        // TODO: Fix that at some point
        if (version > 0x265) {
            profile.dlcPackViewed = serializer.intarray(profile.dlcPackViewed);
            profile.dlcPackShown = serializer.intarray(profile.dlcPackShown);
        }

        if (version > 0x1e4)
            profile.lbp1MainMenuButtonUnlocks = serializer.i32(profile.lbp1MainMenuButtonUnlocks);

        if (version >= 0x16e)
            profile.playedLevelData = serializer.arraylist(profile.playedLevelData, PlayedLevelData.class);

        if (version > 0x379) {
            profile.viewedLevelData = serializer.arraylist(profile.viewedLevelData, ViewedLevelData.class);
            profile.viewedPlayerData = serializer.arraylist(profile.viewedPlayerData, ViewedPlayerData.class);
        }

        if (version > 0x16d) {
            if (serializer.isWriting()) {
                Set<SlotID> keys = profile.oldMyMoonSlots.keySet();
                serializer.getOutput().i32(keys.size());
                for (SlotID key : keys) {
                    serializer.struct(key, SlotID.class);
                    serializer.struct(profile.oldMyMoonSlots.get(key), Slot.class);
                }
            } else {
                int count = serializer.getInput().i32();
                profile.oldMyMoonSlots = new HashMap<SlotID, Slot>(count);
                for (int i = 0; i < count; ++i)
                    profile.oldMyMoonSlots.put(
                            serializer.struct(null, SlotID.class), 
                            serializer.struct(null, Slot.class));
            }
        }

        if (version > 0x20d) 
            profile.copiedFromAnotherUser = serializer.bool(profile.copiedFromAnotherUser);
        if (version > 0x267) 
            profile.fixedUpByDeveloper = serializer.bool(profile.fixedUpByDeveloper);
        if (version > 0x296 || revision.has(Branch.LEERDAMMER, Revisions.LD_SAVEGAME_INFO)) {
            profile.showSaveGameInfo = serializer.bool(profile.showSaveGameInfo);
            profile.totalLevelCount = serializer.i32(profile.totalLevelCount);
        }

        if (version > 0x393) 
            profile.lastMmPicksCount = serializer.i32(profile.lastMmPicksCount);

        if (version > 0x2cb || revision.has(Branch.LEERDAMMER, Revisions.LD_NEWS_TIMESTAMP))
            profile.lastNewsItemTimestamp = serializer.i64(profile.lastNewsItemTimestamp);

        if (version > 0x323)
            profile.lastStreamEventTimestamp = serializer.i64(profile.lastStreamEventTimestamp);

        if (version > 0x2cc || revision.has(Branch.LEERDAMMER, Revisions.LD_QUEUE))
            profile.lolcatFtwTimestamp = serializer.i64(profile.lolcatFtwTimestamp);

        if (version >= 0x328) {
            if (!serializer.isWriting()) profile.oldTextSearches = new String[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.oldTextSearches.length);
            for (int i = 0; i < profile.oldTextSearches.length; ++i)
                profile.oldTextSearches[i] = serializer.wstr(profile.oldTextSearches[i]);
        }
        
        profile.oldDiscoveredLinks = serializer.array(profile.oldDiscoveredLinks, SlotLink.class);
        profile.pendingLinks = serializer.array(profile.pendingLinks, SlotLink.class);

        profile.lastPlayed = serializer.struct(profile.lastPlayed, SlotID.class);

        if (version > 0x17d) 
            profile.lbp1VideosPlayed = serializer.intarray(profile.lbp1VideosPlayed);
        if (version > 0x36b)
            profile.lbp2VideosPlayed = serializer.intarray(profile.lbp2VideosPlayed);

        if (version > 0x16f) 
            profile.currentPlayerMetrics = serializer.struct(profile.currentPlayerMetrics, PlayerMetrics.class);

        if (version > 0x1ab) 
            profile.lastPostedPlayerMetrics = serializer.struct(profile.lastPostedPlayerMetrics, PlayerMetrics.class);

        if (version > 0x1b8)
            profile.lbp1GameProgressionStatus = serializer.enum32(profile.lbp1GameProgressionStatus);

        if (version > 0x1cf) {
            profile.lbp1GameProgressionEventHappenedBits = serializer.i32(profile.lbp1GameProgressionEventHappenedBits);
            profile.lbp1GameProgressionEventsExplainedBits = serializer.i32(profile.lbp1GameProgressionEventsExplainedBits);
        }

        if (version > 0x26c)
            profile.demoProgressionStatus = serializer.i32(profile.demoProgressionStatus);

        if (version > 0x36b)
            profile.lbp2GameProgressionFlags = serializer.i32(profile.lbp2GameProgressionFlags);

        if (version >= 0x387) 
            profile.pendingInventoryItems = serializer.array(profile.pendingInventoryItems, InventoryItem.class);

        profile.podLevel = serializer.resource(profile.podLevel, ResourceType.LEVEL, true);

        if (version > 0x14f)
            profile.playerId = serializer.struct(profile.playerId, NetworkPlayerID.class);

        if (version > 0x296 || revision.has(Branch.LEERDAMMER, Revisions.LD_SAVEGAME_INFO)) {
            profile.ownerPlayerId = serializer.struct(profile.ownerPlayerId, NetworkPlayerID.class);
            profile.ownerPsid = serializer.struct(profile.ownerPsid, OpenPSID.class);
        }

        if (version > 0x2eb || revision.has(Branch.LEERDAMMER, Revisions.LD_OWNER_ID))
            profile.ownerUserId = serializer.i32(profile.ownerUserId);

        if (version > 0x175) 
            profile.eulaAgreed = serializer.sha1(profile.eulaAgreed);

        if (version >= 0x3d5)
            profile.acceptingPlayer = serializer.struct(profile.acceptingPlayer, NetworkPlayerID.class);

        profile.syncedProfile = serializer.resource(profile.syncedProfile, ResourceType.SYNCED_PROFILE, true);

        if (version > 0x1a1) {
            profile.voipMutedPlayers = serializer.array(profile.voipMutedPlayers, NetworkPlayerID.class);
            profile.voipChatPaused = serializer.bool(profile.voipChatPaused);
            profile.enableMusicInPlayMode = serializer.bool(profile.enableMusicInPlayMode);
        }

        if (version > 0x1b4)
            profile.enableMusicInPod = serializer.bool(profile.enableMusicInPod);

        if (version > 0x1b5) {
            profile.legacyInventoryCollections = serializer.array(profile.legacyInventoryCollections, LegacyInventoryCollection.class, true);
            profile.legacyInventoryViews = serializer.array(profile.legacyInventoryViews, InventoryView.class, true);
        }

        if (version > 0x316) 
            profile.inventoryCollections = serializer.array(profile.inventoryCollections, InventoryCollection.class, true);

        if (version > 0x1e0)
            profile.avatarIcon = serializer.resource(profile.avatarIcon, ResourceType.TEXTURE, true);

        if (version > 0x23c)
            profile.saveIcon = serializer.resource(profile.saveIcon, ResourceType.TEXTURE, true);

        if (version > 0x1e5) 
            profile.lbp1CreateModeVOProgress = serializer.i32(profile.lbp1CreateModeVOProgress);

        if (version > 0x1ff) {
            profile.gamma = serializer.f32(profile.gamma);
            profile.screenSize = serializer.f32(profile.screenSize);
        }

        if (version > 0x35e)
            profile.hasSeenCalibrationScreen = serializer.bool(profile.hasSeenCalibrationScreen);


        if (version > 0x205)
            profile.lbp1VOPlayed = serializer.intvector(profile.lbp1VOPlayed);
        if (version > 0x36b) 
            profile.lbp2VOPlayed = serializer.intvector(profile.lbp2VOPlayed);

        if (version > 0x23a)
            profile.subtitleMode = serializer.s32(profile.subtitleMode);

        if (version > 0x255 && version < 0x351) {
            serializer.i32(0);
        }

        if (version > 0x32e) {
            profile.pinsAwarded = serializer.struct(profile.pinsAwarded, PinsAwarded.class);
        }
        
        if (version > 0x260) {
            profile.userSettingPhotoBooth = serializer.bool(profile.userSettingPhotoBooth);

            if (subVersion > 0x17e) 
                profile.userSettingAdvancedEditMode = serializer.bool(profile.userSettingAdvancedEditMode);

            profile.userSettingCollection = serializer.bool(profile.userSettingCollection);
            
            if (version == 0x261)
                serializer.bool(false);
        }

        if (version > 0x2d2 || revision.has(Branch.LEERDAMMER, Revisions.LD_THERMOMETER))
            profile.showThermometer = serializer.bool(profile.showThermometer);

        if (version > 0x383)
            profile.saveOnlinePlaySettings = serializer.bool(profile.saveOnlinePlaySettings);

        if (version > 0x30d) {
            profile.onlinePlayMode = serializer.i32(profile.onlinePlayMode);
            profile.friendJoinRequestMode = serializer.i32(profile.friendJoinRequestMode);
            profile.nonFriendJoinRequestMode = serializer.i32(profile.nonFriendJoinRequestMode);
        }

        if (version > 0x352) {
            profile.lbp2TutorialsPlayed = serializer.intarray(profile.lbp2TutorialsPlayed);
            profile.sectionHeadingToggled = serializer.intvector(profile.sectionHeadingToggled);
        }

        if (subVersion > 0x20d)
            profile.mysteryPodEventsSeen = serializer.array(profile.mysteryPodEventsSeen, MysteryPodEventSeen.class);

        if (version > 0x366) {
            if (!serializer.isWriting()) profile.lastLegacyImportedProfileHashLBP1 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.lastLegacyImportedProfileHashLBP1.length);
            for (int i = 0; i < profile.lastLegacyImportedProfileHashLBP1.length; ++i)
                profile.lastLegacyImportedProfileHashLBP1[i] = serializer.sha1(profile.lastLegacyImportedProfileHashLBP1[i]);
        }

        if (subVersion > 0x37) {
            if (!serializer.isWriting()) profile.lastLegacyImportedProfileHashLBP2 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.lastLegacyImportedProfileHashLBP2.length);
            for (int i = 0; i < profile.lastLegacyImportedProfileHashLBP2.length; ++i)
                profile.lastLegacyImportedProfileHashLBP2[i] = serializer.sha1(profile.lastLegacyImportedProfileHashLBP2[i]);
        }

        if (version > 0x36b)
            profile.playedLBP1 = serializer.bool(profile.playedLBP1);

        if (subVersion > 0x37)
            profile.playedLBP2 = serializer.bool(profile.playedLBP2);

        if (subVersion > 0x12c) 
            profile.hasPerformedFirstRun = serializer.bool(profile.hasPerformedFirstRun);

        if (version > 0x3ae)
            profile.lbp2TutorialUnlockButtonUnleashed = serializer.bool(profile.lbp2TutorialUnlockButtonUnleashed);

        if (version >= 0x28c)
            profile.hiddenCategories = serializer.intvector(profile.hiddenCategories);

        if (version >= 0x2d2)
            profile.collectedBubbles = serializer.array(profile.collectedBubbles, CollectedBubble.class);

        if (version > 0x2f5 || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_LEVELS))
            profile.numWaterLevelsPlayed = serializer.i32(profile.numWaterLevelsPlayed);

        if (version > 0x3cf) 
            profile.autoFilterOn = serializer.bool(profile.autoFilterOn);

        if (version > 0x3d2) {
            profile.hasUsed6Axis = serializer.bool(profile.hasUsed6Axis);
            profile.hasUsedMove = serializer.bool(profile.hasUsedMove);
        }

        if (version > 0x3db)
            profile.paintProperties = serializer.struct(profile.paintProperties, PaintProperties.class);


        if (revision.has(Branch.DOUBLE11, Revisions.D1_DATALABELS)) {
            profile.playerDataLabels = serializer.struct(profile.playerDataLabels, PlayerDataLabels.class);
            if (revision.has(Branch.DOUBLE11, Revisions.D1_3G_CONNECTIVITY)) {
                profile.total3GUpStream = serializer.i32(profile.total3GUpStream);
                profile.total3GDownStream = serializer.i32(profile.total3GDownStream);
                if (revision.has(Branch.DOUBLE11, Revisions.D1_TOUCH_CREATE)) {
                    profile.createRearTouchPan = serializer.bool(profile.createRearTouchPan);
                    profile.createRearTouchPinchZoom = serializer.bool(profile.createRearTouchPinchZoom);
                    profile.createRearTapZoom = serializer.bool(profile.createRearTapZoom);
                    if (revision.has(Branch.DOUBLE11, Revisions.D1_CREATE_WARNING)) {
                        profile.createFrameRateWarningMessages = serializer.bool(profile.createFrameRateWarningMessages);
                        if (revision.has(Branch.DOUBLE11, Revisions.D1_CREATE_BETA)) {
                            profile.onlineBetaNetworkTimestamp = serializer.i64(profile.onlineBetaNetworkTimestamp);
                            profile.onlineBetaPlayTimeSinceOnline = serializer.i32(profile.onlineBetaPlayTimeSinceOnline);
                            profile.onlineBetaBootsSinceOnline = serializer.i32(profile.onlineBetaBootsSinceOnline);
                        }
                    }
                }
            }
        }

        if (version > 0x3e2) 
            profile.distanceMovedWhilstAttracted = serializer.f32(profile.distanceMovedWhilstAttracted);

        if (version > 0x3e4) {
            profile.beakersUsedPart1 = serializer.i32(profile.beakersUsedPart1);
            profile.beakersUsedPart2 = serializer.i32(profile.beakersUsedPart2);
        }

        if (version > 0x3eb)
            profile.profileFlags = serializer.i32(profile.profileFlags);

        if (version > 0x3ee)
            profile.playerDataLabels = serializer.struct(profile.playerDataLabels, PlayerDataLabels.class);

        if (version > 0x3f1) {
            profile.goldMedalsAwarded = serializer.i8(profile.goldMedalsAwarded);
            profile.goldMedalStoryArcPins = serializer.i8(profile.goldMedalStoryArcPins);
        }

        if (version > 0x3f2) {
            profile.twitterEnabled = serializer.bool(profile.twitterEnabled);
            profile.facebookEnabled = serializer.bool(profile.facebookEnabled);
            profile.twitterAccessToken = serializer.str(profile.twitterAccessToken);
            profile.twitterAccessTokenSecret = serializer.str(profile.twitterAccessTokenSecret);
        }

        if (version > 0x3f3)
            profile.playedLBP2 = serializer.bool(profile.playedLBP2);

        if (version > 0x3f4) {
            profile.createChallengeTutorialSeen = serializer.bool(profile.createChallengeTutorialSeen);
            profile.playChallengeTutorialSeen = serializer.bool(profile.playChallengeTutorialSeen);
        }

        if (version > 0x3f5 && version < 0x3f9) {
            if (!serializer.isWriting()) profile.lastLegacyImportedProfileHashLBP2 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.lastLegacyImportedProfileHashLBP2.length);
            for (int i = 0; i < profile.lastLegacyImportedProfileHashLBP2.length; ++i)
                profile.lastLegacyImportedProfileHashLBP2[i] = serializer.sha1(profile.lastLegacyImportedProfileHashLBP2[i]);
        }

        if (version > 0x3f7)
            profile.ownedLBP = serializer.i32(profile.ownedLBP);

        if (subVersion > 0xef)
            profile.dceUuidState = serializer.i32(profile.dceUuidState);

        if (subVersion > 0x15b)
            profile.lastPlayedPPP = serializer.bool(profile.lastPlayedPPP);

        if (subVersion > 0x20e) {
            profile.lastPlayedPlanet = serializer.struct(profile.lastPlayedPlanet, SlotID.class);
            profile.lastPlayedEarthAdv = serializer.bool(profile.lastPlayedEarthAdv);
            profile.hasSeenDiveInBetaMessage = serializer.bool(profile.hasSeenDiveInBetaMessage);
        } 

        if (subVersion > 0x183)
            profile.touchCreateCursorMode = serializer.i32(profile.touchCreateCursorMode);
        
        if (subVersion > 0x187)
            profile.showAdvancedEditModeMessageCounter = serializer.bool(profile.showAdvancedEditModeMessageCounter);

        if (subVersion > 0x194)
            profile.showAdventureSaveWarning = serializer.bool(profile.showAdventureSaveWarning);

        if (subVersion > 0x19f)
            profile.totalFramesInEditMode = serializer.i32(profile.totalFramesInEditMode);

        if (subVersion > 0x1a1) {
            if (!serializer.isWriting()) profile.onlineTutorialsPlayed = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.onlineTutorialsPlayed.length);
            for (int i = 0; i < profile.onlineTutorialsPlayed.length; ++i)
                profile.onlineTutorialsPlayed[i] = serializer.sha1(profile.onlineTutorialsPlayed[i]);

            profile.popitPuzzlesCompleted = serializer.array(profile.popitPuzzlesCompleted, SlotID.class);
        }

        if (subVersion > 0x1aa)
            profile.timesaverNoticeViewed = serializer.bool(profile.timesaverNoticeViewed);
        if (subVersion > 0x1ad)
            profile.questProgressPin = serializer.s32(profile.questProgressPin);
        if (subVersion > 0x209) 
            profile.activityFilterToggled = serializer.intvector(profile.activityFilterToggled);
        if (subVersion > 0x212)
            profile.lastLandingPageFocusItems = serializer.i32(profile.lastLandingPageFocusItems);
        if (subVersion > 0x215) 
            profile.goPlayCache = serializer.array(profile.goPlayCache, GoPlayCache.class);
        
        System.out.println(serializer.getOffset());

        return profile;
    }

    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        size += this.stringTable.getAllocatedSize();
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(0xFFFF00 + (this.inventory.size() * 0xC0), revision, compressionFlags);
        serializer.struct(this, RLocalProfile.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags, 
            ResourceType.LOCAL_PROFILE,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    /**
     * Adds an item to your inventory from a RPlan.
     * @param plan Plan to add
     * @param descriptor GUID/SHA1 reference to the plan to add
     * @param table Translation table for resolving categories/locations
     * @return Added inventory item
     */
    public InventoryItem addItem(RPlan plan, ResourceDescriptor descriptor, RTranslationTable table) {
        InventoryItem existing = this.getItem(descriptor);
        if (existing != null) return existing;

        InventoryItem item = new InventoryItem();
        item.plan = descriptor;
        if (descriptor.isGUID())
            item.guid = descriptor.getGUID();
        InventoryItemDetails details = plan.inventoryData;
        details.dateAdded = (new Date().getTime() / 1000);
        
        if (details.icon == null)
            details.icon = new ResourceDescriptor(2551, ResourceType.TEXTURE);

        if (table != null) {
            String category = "";
            if (plan.inventoryData.category != 0)
                category = table.translate(plan.inventoryData.category);
            String location = "";
            if (plan.inventoryData.location != 0)
                location = table.translate(plan.inventoryData.location);
            if (location == null)
                location = "";
            if (category == null) 
                category = "";

            details.categoryIndex = (short) this.stringTable.add(category, (int) (plan.inventoryData.category & 0xffffffff));
            details.locationIndex = (short) this.stringTable.add(location, (int) (plan.inventoryData.location & 0xffffffff));
        }

        item.details = plan.inventoryData;
        item.flags = 4;
        item.UID = this.getLastInventoryUID() + 1;

        this.inventory.add(item);

        return item;
    }

    public int getLastInventoryUID() {
        int UID = 1;
        for (InventoryItem item : this.inventory)
            if (item.UID > UID)
                UID = item.UID;
        return UID;
    }

    public boolean hasItem(ResourceDescriptor descriptor) {
        if (descriptor == null) return false;
        if (descriptor.isGUID()) return this.hasItem(descriptor.getGUID());
        else if (descriptor.isHash()) return this.hasItem(descriptor.getSHA1());
        return false;
    }

    public boolean hasItem(SHA1 hash) {
        for (InventoryItem item : this.inventory) {
            ResourceDescriptor plan = item.plan;
            if (plan == null) continue;
            if (plan.isHash() && plan.getSHA1().equals(hash))
                return true;
        }
        return false;
    }

    public boolean hasItem(GUID guid) {
        for (InventoryItem item : this.inventory) {
            ResourceDescriptor plan = item.plan;
            if (plan == null) continue;
            if (plan.isGUID() && plan.getGUID().equals(guid))
                return true;
        }
        return false;
    }

    public InventoryItem getItem(ResourceDescriptor descriptor) {
        if (descriptor == null) return null;
        for (InventoryItem item : this.inventory)
            if (descriptor.equals(item.plan))
                return item;
        return null;
    }  
}
