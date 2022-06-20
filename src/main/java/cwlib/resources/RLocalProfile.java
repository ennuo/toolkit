package cwlib.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import cwlib.enums.GameProgressionStatus;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.profile.*;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.types.Resource;
import cwlib.types.archives.Fart;
import cwlib.types.data.GUID;
import cwlib.types.data.NetworkPlayerID;
import cwlib.types.data.OpenPSID;
import cwlib.types.data.ResourceReference;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileEntry;

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
    public boolean fromProductionBuild = true;

    public HashMap<Integer, Integer> lbp1TutorialStates = new HashMap<>();

    public int[] dlcPackViewed, dlcPackShown;

    public int lbp1MainMenuButtonUnlocks;

    public ArrayList<PlayedLevelData> playedLevelData;
    public ArrayList<ViewedLevelData> viewedLevelData;
    public ArrayList<ViewedPlayerData> viewedPlayerData;

    public HashMap<SlotID, Slot> oldMyMoonSlots = new HashMap<>();

    public boolean copiedFromAnotherUser;
    public boolean fixedUpByDeveloper;
    public boolean showSaveGameInfo;
    public int totalLevelCount;
    public int lastMmPicksCount;
    public long lastNewsItemTimestamp;
    public long lastStreamEventTimestamp;
    public long lolcatFtwTimestamp;

    public String[] oldTextSearches;
    public SlotLink[] oldDiscoveredLinks;
    public SlotLink[] pendingLinks;
    public SlotID lastPlayed = new SlotID();

    public int[] lbp1VideosPlayed, lbp2VideosPlayed;

    public PlayerMetrics currentPlayerMetrics, lastPostedPlayerMetrics;

    public GameProgressionStatus lbp1GameProgressionStatus = GameProgressionStatus.NEW_GAME;
    public int lbp1GameProgressionEventHappenedBits;
    public int lbp1GameProgressionEventsExplainedBits;
    public int demoProgressionStatus;
    public int lbp2GameProgressionFlags;
    public InventoryItem[] pendingInventoryItems;
    public ResourceReference podLevel;

    public NetworkPlayerID playerId;
    public NetworkPlayerID ownerPlayerId;
    public OpenPSID ownerPsid;
    public int ownerUserId;
    public SHA1 eulaAgreed;
    public NetworkPlayerID acceptingPlayer;
    public ResourceReference syncedProfile;

    public NetworkPlayerID[] voipMutedPlayers;
    public boolean voipChatPaused;
    public boolean enableMusicInPlayMode;
    public boolean enableMusicInPod;

    public LegacyInventoryCollection[] legacyInventoryCollections;
    public InventoryView[] legacyInventoryViews;

    public InventoryCollection[] inventoryCollections;

    public ResourceReference avatarIcon;
    public ResourceReference saveIcon;

    public int lbp1CreateModeVOProgress;
    public float gamma;
    public float screenSize;
    public boolean hasSeenCalibrationScreen;
    public long[] lbp1VOPlayed, lbp2VOPlayed;
    public int subtitleMode;
    
    public PinsAwarded pinsAwarded;

    public boolean userSettingPhotoBooth;
    public boolean userSettingCollection;
    public boolean userSettingAdvancedEditMode;
    public boolean showThermometer;
    public boolean saveOnlinePlaySettings;
    public int onlinePlayMode;
    public int friendJoinRequestMode;
    public int nonFriendJoinRequestMode;

    public int[] lbp2TutorialsPlayed;
    public int[] sectionHeadingToggled;

    public int[] mysteryPodEventsSeen;
    public SHA1[] lastLegacyImportedProfileHashLBP1;
    public SHA1[] lastLegacyImportedProfileHashLBP2;
    public boolean playedLBP1;
    public boolean hasPerformedFirstRun;
    public boolean lbp2TutorialUnlockButtonUnleashed;

    public int[] hiddenCategories;
    public CollectedBubble[] collectedBubbles;
    public int numWaterLevelsPlayed;
    public boolean autoFilterOn;
    public boolean hasUsed6Axis, hasUsedMove;
    public PaintProperties paintProperties;

    public PlayerDataLabels playerDataLabels;

    /* Vita stuff */

    public int total3GUpStream, total3GDownStream;
    public boolean createRearTouchPan, createRearTouchPinchZoom, createRearTapZoom;
    public boolean createFrameRateWarningMessages;
    public long onlineBetaNetworkTimestamp;
    public int onlineBetaPlayTimeSinceOnline, onlineBetaBootsSinceOnline;

    public float distanceMovedWhilstAttracted;
    public int beakersUsedPart1, beakersUsedPart2;
    public int profileFlags;

    public byte goldMedalsAwarded, goldMedalStoryArcPins;
    public boolean twitterEnabled, facebookEnabled;
    public String twitterAccessToken, twitterAccessTokenSecret;
    public boolean playedLBP2, createChallengeTutorialSeen, playChallengeTutorialSeen;
    public int ownedLBP;
    public int dceUuidState;
    public boolean lastPlayedPPP;
    public SlotID lastPlayedPlanet;
    public boolean lastPlayedEarthAdv;
    public boolean hasSeenDiveInBetaMessage;
    public int touchCreateCursorMode;
    public boolean showAdvancedEditModeMessageCounter;
    public boolean showAdventureSaveWarning;
    public int totalFramesInEditMode;
    public SHA1[] onlineTutorialsPlayed;
    public SlotID[] popitPuzzlesCompleted;
    public boolean timesaverNoticeViewed;
    public int questProgressPin;
    public int[] activityFilterToggled;
    public int lastLandingPageFocusItems;
    public GoPlayCache[] goPlayCache;


    @SuppressWarnings("unchecked")
    @Override public RLocalProfile serialize(Serializer serializer, Serializable structure) {
        RLocalProfile profile = (structure == null) ? new RLocalProfile() : (RLocalProfile) structure;

        Revision revision = serializer.getRevision();
        int head = revision.getVersion();

        profile.inventory = serializer.arraylist(profile.inventory, InventoryItem.class);
        profile.stringTable = serializer.struct(profile.stringTable, StringLookupTable.class);
        if (head > 0x3b5)
            profile.fromProductionBuild = serializer.bool(profile.fromProductionBuild);

        if (head < 0x269 && head > 0x128) {
            // lockStates
        }

        if (head > 0x1e3) {
            if (serializer.isWriting()) {
                MemoryOutputStream stream = serializer.getOutput();
                ArrayList<Integer> keys = new ArrayList<>(profile.lbp1TutorialStates.keySet());
                keys.sort((l, r) -> l - r);
                stream.i32(keys.size());
                for (Integer key : keys) {
                    stream.i32(key);
                    stream.i32(profile.lbp1TutorialStates.get(key));
                }
            } else {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                profile.lbp1TutorialStates = new HashMap<Integer, Integer>(count);
                for (int i = 0; i < count; ++i)
                    profile.lbp1TutorialStates.put(stream.i32(), stream.i32());
            }
        }

        if (head > 0x265) {
            profile.dlcPackViewed = serializer.intarray(profile.dlcPackViewed);
            profile.dlcPackShown = serializer.intarray(profile.dlcPackShown);
        }

        if (head > 0x1e4)
            profile.lbp1MainMenuButtonUnlocks = serializer.i32(profile.lbp1MainMenuButtonUnlocks);

        if (head >= 0x16e)
            profile.playedLevelData = serializer.arraylist(profile.playedLevelData, PlayedLevelData.class);

        if (head > 0x379) {
            profile.viewedLevelData = serializer.arraylist(profile.viewedLevelData, ViewedLevelData.class);
            profile.viewedPlayerData = serializer.arraylist(profile.viewedPlayerData, ViewedPlayerData.class);
        }

        if (head > 0x16d) {
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

        if (head > 0x20d) 
            profile.copiedFromAnotherUser = serializer.bool(profile.copiedFromAnotherUser);
        if (head > 0x267) 
            profile.fixedUpByDeveloper = serializer.bool(profile.fixedUpByDeveloper);
        if (head > 0x296 || revision.isAfterLeerdamerRevision(0x2)) {
            profile.showSaveGameInfo = serializer.bool(profile.showSaveGameInfo);
            profile.totalLevelCount = serializer.i32(profile.totalLevelCount);
        }

        if (head > 0x393) 
            profile.lastMmPicksCount = serializer.i32(profile.lastMmPicksCount);

        if (head > 0x2cb || revision.isAfterLeerdamerRevision(0xC))
            profile.lastNewsItemTimestamp = serializer.i64(profile.lastNewsItemTimestamp);

        if (head > 0x323)
            profile.lastStreamEventTimestamp = serializer.i64(profile.lastStreamEventTimestamp);

        if (head > 0x2cc || revision.isAfterLeerdamerRevision(0x11))
            profile.lolcatFtwTimestamp = serializer.i64(profile.lolcatFtwTimestamp);

        if (head >= 0x328) {
            if (!serializer.isWriting()) profile.oldTextSearches = new String[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.oldTextSearches.length);
            for (int i = 0; i < profile.oldTextSearches.length; ++i)
                profile.oldTextSearches[i] = serializer.wstr(profile.oldTextSearches[i]);
        }
        
        profile.oldDiscoveredLinks = serializer.array(profile.oldDiscoveredLinks, SlotLink.class);
        profile.pendingLinks = serializer.array(profile.pendingLinks, SlotLink.class);

        profile.lastPlayed = serializer.struct(profile.lastPlayed, SlotID.class);

        if (head > 0x17d) 
            profile.lbp1VideosPlayed = serializer.intarray(profile.lbp1VideosPlayed);
        if (head > 0x36b)
            profile.lbp2VideosPlayed = serializer.intarray(profile.lbp2VideosPlayed);

        if (head > 0x16f) 
            profile.currentPlayerMetrics = serializer.struct(profile.currentPlayerMetrics, PlayerMetrics.class);

        if (head > 0x1ab) 
            profile.lastPostedPlayerMetrics = serializer.struct(profile.lastPostedPlayerMetrics, PlayerMetrics.class);

        if (head > 0x1b8)
            profile.lbp1GameProgressionStatus = serializer.enum32(profile.lbp1GameProgressionStatus);

        if (head > 0x1cf) {
            profile.lbp1GameProgressionEventHappenedBits = serializer.i32(profile.lbp1GameProgressionEventHappenedBits);
            profile.lbp1GameProgressionEventsExplainedBits = serializer.i32(profile.lbp1GameProgressionEventsExplainedBits);
        }

        if (head > 0x26c)
            profile.demoProgressionStatus = serializer.i32(profile.demoProgressionStatus);

        if (head > 0x36b)
            profile.lbp2GameProgressionFlags = serializer.i32(profile.lbp2GameProgressionFlags);

        if (head >= 0x387) 
            profile.pendingInventoryItems = serializer.array(profile.pendingInventoryItems, InventoryItem.class);

        profile.podLevel = serializer.resource(profile.podLevel, ResourceType.LEVEL, true);

        if (head > 0x14f)
            profile.playerId = serializer.struct(profile.playerId, NetworkPlayerID.class);

        if (head > 0x296 || revision.isAfterLeerdamerRevision(0x2)) {
            profile.ownerPlayerId = serializer.struct(profile.ownerPlayerId, NetworkPlayerID.class);
            profile.ownerPsid = serializer.struct(profile.ownerPsid, OpenPSID.class);
        }

        if (head > 0x2eb || revision.isAfterLeerdamerRevision(0x15))
            profile.ownerUserId = serializer.i32(profile.ownerUserId);

        if (head > 0x175) 
            profile.eulaAgreed = serializer.sha1(profile.eulaAgreed);

        if (head >= 0x3d5)
            profile.acceptingPlayer = serializer.struct(profile.acceptingPlayer, NetworkPlayerID.class);

        profile.syncedProfile = serializer.resource(profile.syncedProfile, ResourceType.SYNCED_PROFILE, true);

        if (head > 0x1a1) {
            profile.voipMutedPlayers = serializer.array(profile.voipMutedPlayers, NetworkPlayerID.class);
            profile.voipChatPaused = serializer.bool(profile.voipChatPaused);
            profile.enableMusicInPlayMode = serializer.bool(profile.enableMusicInPlayMode);
        }

        if (head > 0x1b4)
            profile.enableMusicInPod = serializer.bool(profile.enableMusicInPod);

        if (head > 0x1b5) {
            profile.legacyInventoryCollections = serializer.array(profile.legacyInventoryCollections, LegacyInventoryCollection.class, true);
            profile.legacyInventoryViews = serializer.array(profile.legacyInventoryViews, InventoryView.class, true);
        }

        if (head > 0x316) 
            profile.inventoryCollections = serializer.array(profile.inventoryCollections, InventoryCollection.class, true);

        if (head > 0x1e0)
            profile.avatarIcon = serializer.resource(profile.avatarIcon, ResourceType.TEXTURE, true);

        if (head > 0x23c)
            profile.saveIcon = serializer.resource(profile.saveIcon, ResourceType.TEXTURE, true);

        if (head > 0x1e5) 
            profile.lbp1CreateModeVOProgress = serializer.i32(profile.lbp1CreateModeVOProgress);

        if (head > 0x1ff) {
            profile.gamma = serializer.f32(profile.gamma);
            profile.screenSize = serializer.f32(profile.screenSize);
        }

        if (head > 0x35e)
            profile.hasSeenCalibrationScreen = serializer.bool(profile.hasSeenCalibrationScreen);

        if (head > 0x205)
            profile.lbp1VOPlayed = serializer.longvector(profile.lbp1VOPlayed);

        if (head > 0x36b) 
            profile.lbp2VOPlayed = serializer.longvector(profile.lbp2VOPlayed);

        if (head > 0x23a)
            profile.subtitleMode = serializer.i32d(profile.subtitleMode);

        if (head > 0x255 && head < 0x351) {
            serializer.i32(0);
        }

        if (head > 0x32e) {
            profile.pinsAwarded = serializer.struct(profile.pinsAwarded, PinsAwarded.class);
        }
        
        if (head > 0x260) {
            profile.userSettingPhotoBooth = serializer.bool(profile.userSettingPhotoBooth);

            if (revision.isAfterLBP3Revision(0x17e)) 
                profile.userSettingAdvancedEditMode = serializer.bool(profile.userSettingAdvancedEditMode);

            profile.userSettingCollection = serializer.bool(profile.userSettingCollection);
        }

        if (head > 0x2d2 || revision.isAfterLeerdamerRevision(0x13))
            profile.showThermometer = serializer.bool(profile.showThermometer);

        if (head > 0x383)
            profile.saveOnlinePlaySettings = serializer.bool(profile.saveOnlinePlaySettings);

        if (head > 0x30d) {
            profile.onlinePlayMode = serializer.i32(profile.onlinePlayMode);
            profile.friendJoinRequestMode = serializer.i32(profile.friendJoinRequestMode);
            profile.nonFriendJoinRequestMode = serializer.i32(profile.nonFriendJoinRequestMode);
        }

        if (head > 0x352) {
            profile.lbp2TutorialsPlayed = serializer.intarray(profile.lbp2TutorialsPlayed);
            profile.sectionHeadingToggled = serializer.intvector(profile.sectionHeadingToggled);
        }

        if (revision.isAfterLBP3Revision(0x20d))
            profile.mysteryPodEventsSeen = serializer.intarray(profile.mysteryPodEventsSeen);

        if (head > 0x366) {
            if (!serializer.isWriting()) profile.lastLegacyImportedProfileHashLBP1 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.lastLegacyImportedProfileHashLBP1.length);
            for (int i = 0; i < profile.lastLegacyImportedProfileHashLBP1.length; ++i)
                profile.lastLegacyImportedProfileHashLBP1[i] = serializer.sha1(profile.lastLegacyImportedProfileHashLBP1[i]);
        }

        if (revision.isAfterLBP3Revision(0x37)) {
            if (!serializer.isWriting()) profile.lastLegacyImportedProfileHashLBP2 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.lastLegacyImportedProfileHashLBP2.length);
            for (int i = 0; i < profile.lastLegacyImportedProfileHashLBP2.length; ++i)
                profile.lastLegacyImportedProfileHashLBP2[i] = serializer.sha1(profile.lastLegacyImportedProfileHashLBP2[i]);
        }

        if (head > 0x36b)
            profile.playedLBP1 = serializer.bool(profile.playedLBP1);

        if (revision.isAfterLBP3Revision(0x37))
            profile.playedLBP2 = serializer.bool(profile.playedLBP2);

        if (revision.isAfterLBP3Revision(0x12c)) 
            profile.hasPerformedFirstRun = serializer.bool(profile.hasPerformedFirstRun);

        if (head > 0x3ae)
            profile.lbp2TutorialUnlockButtonUnleashed = serializer.bool(profile.lbp2TutorialUnlockButtonUnleashed);

        if (head >= 0x28c)
            profile.hiddenCategories = serializer.intvector(profile.hiddenCategories);

        if (head >= 0x2d2)
            profile.collectedBubbles = serializer.array(profile.collectedBubbles, CollectedBubble.class);

        if (head > 0x2f5 || revision.isAfterLeerdamerRevision(0x16))
            profile.numWaterLevelsPlayed = serializer.i32(profile.numWaterLevelsPlayed);

        if (head > 0x3cf) 
            profile.autoFilterOn = serializer.bool(profile.autoFilterOn);

        if (head > 0x3d2) {
            profile.hasUsed6Axis = serializer.bool(profile.hasUsed6Axis);
            profile.hasUsedMove = serializer.bool(profile.hasUsedMove);
        }

        if (head > 0x3db)
            profile.paintProperties = serializer.struct(profile.paintProperties, PaintProperties.class);


        if (revision.isAfterVitaRevision(0x2d)) {
            profile.playerDataLabels = serializer.struct(profile.playerDataLabels, PlayerDataLabels.class);
            if (revision.isAfterVitaRevision(0x57)) {
                profile.total3GUpStream = serializer.i32(profile.total3GUpStream);
                profile.total3GDownStream = serializer.i32(profile.total3GDownStream);
                if (revision.isAfterVitaRevision(0x68)) {
                    profile.createRearTouchPan = serializer.bool(profile.createRearTouchPan);
                    profile.createRearTouchPinchZoom = serializer.bool(profile.createRearTouchPinchZoom);
                    profile.createRearTapZoom = serializer.bool(profile.createRearTapZoom);
                    if (revision.isAfterVitaRevision(0x73)) {
                        profile.createFrameRateWarningMessages = serializer.bool(profile.createFrameRateWarningMessages);
                        if (revision.isAfterVitaRevision(0x74)) {
                            profile.onlineBetaNetworkTimestamp = serializer.i64(profile.onlineBetaNetworkTimestamp);
                            profile.onlineBetaPlayTimeSinceOnline = serializer.i32(profile.onlineBetaPlayTimeSinceOnline);
                            profile.onlineBetaBootsSinceOnline = serializer.i32(profile.onlineBetaBootsSinceOnline);
                        }
                    }
                }
            }
        }

        if (head > 0x3e2) 
            profile.distanceMovedWhilstAttracted = serializer.f32(profile.distanceMovedWhilstAttracted);

        if (head > 0x3e4) {
            profile.beakersUsedPart1 = serializer.i32(profile.beakersUsedPart1);
            profile.beakersUsedPart2 = serializer.i32(profile.beakersUsedPart2);
        }

        if (head > 0x3eb)
            profile.profileFlags = serializer.i32(profile.profileFlags);

        if (head > 0x3ee)
            profile.playerDataLabels = serializer.struct(profile.playerDataLabels, PlayerDataLabels.class);

        if (head > 0x3f1) {
            profile.goldMedalsAwarded = serializer.i8(profile.goldMedalsAwarded);
            profile.goldMedalStoryArcPins = serializer.i8(profile.goldMedalStoryArcPins);
        }

        if (head > 0x3f2) {
            profile.twitterEnabled = serializer.bool(profile.twitterEnabled);
            profile.facebookEnabled = serializer.bool(profile.facebookEnabled);
            profile.twitterAccessToken = serializer.str(profile.twitterAccessToken);
            profile.twitterAccessTokenSecret = serializer.str(profile.twitterAccessTokenSecret);
        }

        if (head > 0x3f3)
            profile.playedLBP2 = serializer.bool(profile.playedLBP2);

        if (head > 0x3f4) {
            profile.createChallengeTutorialSeen = serializer.bool(profile.createChallengeTutorialSeen);
            profile.playChallengeTutorialSeen = serializer.bool(profile.playChallengeTutorialSeen);
        }

        if (head > 0x3f5 && head < 0x3f9) {
            if (!serializer.isWriting()) profile.lastLegacyImportedProfileHashLBP2 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.lastLegacyImportedProfileHashLBP2.length);
            for (int i = 0; i < profile.lastLegacyImportedProfileHashLBP2.length; ++i)
                profile.lastLegacyImportedProfileHashLBP2[i] = serializer.sha1(profile.lastLegacyImportedProfileHashLBP2[i]);
        }

        if (head > 0x3f7)
            profile.ownedLBP = serializer.i32(profile.ownedLBP);

        if (revision.isAfterLBP3Revision(0xef))
            profile.dceUuidState = serializer.i32(profile.dceUuidState);

        if (revision.isAfterLBP3Revision(0x15b))
            profile.lastPlayedPPP = serializer.bool(profile.lastPlayedPPP);

        if (revision.isAfterLBP3Revision(0x20e)) {
            profile.lastPlayedPlanet = serializer.struct(profile.lastPlayedPlanet, SlotID.class);
            profile.lastPlayedEarthAdv = serializer.bool(profile.lastPlayedEarthAdv);
            profile.hasSeenDiveInBetaMessage = serializer.bool(profile.hasSeenDiveInBetaMessage);
        } 

        if (revision.isAfterLBP3Revision(0x183))
            profile.touchCreateCursorMode = serializer.i32(profile.touchCreateCursorMode);
        
        if (revision.isAfterLBP3Revision(0x187))
            profile.showAdvancedEditModeMessageCounter = serializer.bool(profile.showAdvancedEditModeMessageCounter);

        if (revision.isAfterLBP3Revision(0x194))
            profile.showAdventureSaveWarning = serializer.bool(profile.showAdventureSaveWarning);

        if (revision.isAfterLBP3Revision(0x19f))
            profile.totalFramesInEditMode = serializer.i32(profile.totalFramesInEditMode);

        if (revision.isAfterLBP3Revision(0x1a1)) {
            if (!serializer.isWriting()) profile.onlineTutorialsPlayed = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(profile.onlineTutorialsPlayed.length);
            for (int i = 0; i < profile.onlineTutorialsPlayed.length; ++i)
                profile.onlineTutorialsPlayed[i] = serializer.sha1(profile.onlineTutorialsPlayed[i]);

            profile.popitPuzzlesCompleted = serializer.array(profile.popitPuzzlesCompleted, SlotID.class);
        }

        if (revision.isAfterLBP3Revision(0x1aa)) {
            profile.timesaverNoticeViewed = serializer.bool(profile.timesaverNoticeViewed);
        }

        if (revision.isAfterLBP3Revision(0x1ad)) {
            profile.questProgressPin = serializer.i32(profile.questProgressPin);
        }

        if (revision.isAfterLBP3Revision(0x209)) 
            profile.activityFilterToggled = serializer.intvector(profile.activityFilterToggled);

        if (revision.isAfterLBP3Revision(0x212))
            profile.lastLandingPageFocusItems = serializer.i32(profile.lastLandingPageFocusItems);

        if (revision.isAfterLBP3Revision(0x215)) 
            profile.goPlayCache = serializer.array(profile.goPlayCache, GoPlayCache.class);
        
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
    public InventoryItem addItem(RPlan plan, ResourceReference descriptor, RTranslationTable table) {
        InventoryItem existing = this.getItem(descriptor);
        if (existing != null) return existing;

        InventoryItem item = new InventoryItem();
        item.plan = descriptor;
        if (descriptor.isGUID())
            item.guid = descriptor.getGUID();
        InventoryItemDetails details = plan.details;
        details.dateAdded = (new Date().getTime() / 1000);
        
        if (details.icon == null)
            details.icon = new ResourceReference(2551, ResourceType.TEXTURE);

        if (table != null) {
            String category = "";
            if (plan.details.category != 0)
                category = table.translate(plan.details.category);
            String location = "";
            if (plan.details.location != 0)
                location = table.translate(plan.details.location);
            if (location == null)
                location = "";
            if (category == null)
                category = "";
            
            

            details.categoryIndex = (short) this.stringTable.add(category, (int) plan.details.category);
            details.locationIndex = (short) this.stringTable.add(location, (int) plan.details.location);
        }

        item.details = plan.details;
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

    public boolean hasItem(ResourceReference descriptor) {
        if (descriptor == null) return false;
        if (descriptor.isGUID()) return this.hasItem(descriptor.getGUID());
        else if (descriptor.isHash()) return this.hasItem(descriptor.getSHA1());
        return false;
    }

    public boolean hasItem(SHA1 hash) {
        for (InventoryItem item : this.inventory) {
            ResourceReference plan = item.plan;
            if (plan == null) continue;
            if (plan.isHash() && plan.getSHA1().equals(hash))
                return true;
        }
        return false;
    }

    public boolean hasItem(GUID guid) {
        for (InventoryItem item : this.inventory) {
            ResourceReference plan = item.plan;
            if (plan == null) continue;
            if (plan.isGUID() && plan.getGUID().equals(guid))
                return true;
        }
        return false;
    }

    public InventoryItem getItem(ResourceReference descriptor) {
        if (descriptor == null) return null;
        for (InventoryItem item : this.inventory)
            if (descriptor.equals(item.plan))
                return item;
        return null;
    }  
}
