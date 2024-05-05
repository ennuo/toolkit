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
import cwlib.io.Resource;
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

public class RLocalProfile implements Resource
{
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
    @GsonRevision(min = 0x3b6)
    public boolean fromProductionBuild = true;

    @GsonRevision(min = 0x1e4)
    public HashMap<TutorialLevel, TutorialState> lbp1TutorialStates = new HashMap<>();

    @GsonRevision(min = 0x266)
    public int[] dlcPackViewed, dlcPackShown;

    @GsonRevision(min = 0x1e5)
    public int lbp1MainMenuButtonUnlocks;

    @GsonRevision(min = 0x16e)
    public ArrayList<PlayedLevelData> playedLevelData;

    @GsonRevision(min = 0x37a)
    public ArrayList<ViewedLevelData> viewedLevelData;
    @GsonRevision(min = 0x37a)
    public ArrayList<ViewedPlayerData> viewedPlayerData;

    @GsonRevision(min = 0x16e)
    public HashMap<SlotID, Slot> oldMyMoonSlots = new HashMap<>();

    @GsonRevision(min = 0x20e)
    public boolean copiedFromAnotherUser;

    @GsonRevision(min = 0x268)
    public boolean fixedUpByDeveloper;

    @GsonRevision(min = 0x297)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_SAVEGAME_INFO)
    public boolean showSaveGameInfo;

    @GsonRevision(min = 0x297)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_SAVEGAME_INFO)
    public int totalLevelCount;

    @GsonRevision(min = 0x394)
    public int lastMmPicksCount;

    @GsonRevision(min = 0x2cc)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_NEWS_TIMESTAMP)
    public long lastNewsItemTimestamp;

    @GsonRevision(min = 0x324)
    public long lastStreamEventTimestamp;

    @GsonRevision(min = 0x2cd)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_QUEUE)
    public long lolcatFtwTimestamp;

    @GsonRevision(min = 0x328)
    public String[] oldTextSearches;

    public SlotLink[] oldDiscoveredLinks;
    public SlotLink[] pendingLinks;
    public SlotID lastPlayed = new SlotID();

    @GsonRevision(min = 0x17e)
    public int[] lbp1VideosPlayed;

    @GsonRevision(min = 0x36c)
    public int[] lbp2VideosPlayed;

    @GsonRevision(min = 0x170)
    public PlayerMetrics currentPlayerMetrics;

    @GsonRevision(min = 0x1ac)
    public PlayerMetrics lastPostedPlayerMetrics;

    @GsonRevision(min = 0x1b9)
    public GameProgressionStatus lbp1GameProgressionStatus = GameProgressionStatus.NEW_GAME;

    @GsonRevision(min = 0x1d0)
    public int lbp1GameProgressionEventHappenedBits;

    @GsonRevision(min = 0x1d0)
    public int lbp1GameProgressionEventsExplainedBits;

    @GsonRevision(min = 0x26d)
    public int demoProgressionStatus;

    @GsonRevision(min = 0x36c)
    public int lbp2GameProgressionFlags;

    @GsonRevision(min = 0x387)
    public InventoryItem[] pendingInventoryItems;

    public ResourceDescriptor podLevel;

    @GsonRevision(min = 0x150)
    public NetworkPlayerID playerId;


    @GsonRevision(min = 0x297)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_SAVEGAME_INFO)
    public NetworkPlayerID ownerPlayerId;

    @GsonRevision(min = 0x297)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_SAVEGAME_INFO)
    public OpenPSID ownerPsid;

    @GsonRevision(min = 0x2ec)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_OWNER_ID)
    public int ownerUserId;

    @GsonRevision(min = 0x176)
    public SHA1 eulaAgreed;

    @GsonRevision(min = 0x3d5)
    public NetworkPlayerID acceptingPlayer;

    public ResourceDescriptor syncedProfile;

    @GsonRevision(min = 0x1a2)
    public NetworkPlayerID[] voipMutedPlayers;

    @GsonRevision(min = 0x1a2)
    public boolean voipChatPaused;

    @GsonRevision(min = 0x1a2)
    public boolean enableMusicInPlayMode;

    @GsonRevision(min = 0x1b5)
    public boolean enableMusicInPod;

    @GsonRevision(min = 0x1b6)
    public LegacyInventoryCollection[] legacyInventoryCollections;

    @GsonRevision(min = 0x1b6)
    public InventoryView[] legacyInventoryViews;

    @GsonRevision(min = 0x317)
    public InventoryCollection[] inventoryCollections;

    @GsonRevision(min = 0x1e1)
    public ResourceDescriptor avatarIcon;

    @GsonRevision(min = 0x23d)
    public ResourceDescriptor saveIcon;

    @GsonRevision(min = 0x1e6)
    public int lbp1CreateModeVOProgress;

    @GsonRevision(min = 0x200)
    public float gamma;

    @GsonRevision(min = 0x200)
    public float screenSize;

    @GsonRevision(min = 0x35f)
    public boolean hasSeenCalibrationScreen;

    @GsonRevision(min = 0x206)
    public int[] lbp1VOPlayed;

    @GsonRevision(min = 0x36c)
    public int[] lbp2VOPlayed;

    @GsonRevision(min = 0x23b)
    public int subtitleMode;

    @GsonRevision(min = 0x32f)
    public PinsAwarded pinsAwarded;

    @GsonRevision(min = 0x261)
    public boolean userSettingPhotoBooth;

    @GsonRevision(min = 0x261)
    public boolean userSettingCollection;

    @GsonRevision(lbp3 = true, min = 0x17f)
    public boolean userSettingAdvancedEditMode;

    @GsonRevision(min = 0x2d3)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_THERMOMETER)
    public boolean showThermometer;

    @GsonRevision(min = 0x384)
    public boolean saveOnlinePlaySettings;

    @GsonRevision(min = 0x30e)
    public int onlinePlayMode;

    @GsonRevision(min = 0x30e)
    public int friendJoinRequestMode;

    @GsonRevision(min = 0x30e)
    public int nonFriendJoinRequestMode;

    @GsonRevision(min = 0x353)
    public int[] lbp2TutorialsPlayed;

    @GsonRevision(min = 0x353)
    public int[] sectionHeadingToggled;

    @GsonRevision(lbp3 = true, min = 0x20e)
    public MysteryPodEventSeen[] mysteryPodEventsSeen;

    @GsonRevision(min = 0x367)
    public SHA1[] lastLegacyImportedProfileHashLBP1;

    @GsonRevision(min = 0x3f6)
    public SHA1[] lastLegacyImportedProfileHashLBP2;

    @GsonRevision(min = 0x36c)
    public boolean playedLBP1;

    @GsonRevision(lbp3 = true, min = 0x12d)
    public boolean hasPerformedFirstRun;

    @GsonRevision(min = 0x3af)
    public boolean lbp2TutorialUnlockButtonUnleashed;

    @GsonRevision(min = 0x28c)
    public int[] hiddenCategories;

    @GsonRevision(min = 0x2d2)
    public CollectedBubble[] collectedBubbles;

    @GsonRevision(min = 0x2f6)
    @GsonRevision(branch = 0x4c44, min = Revisions.LD_WATER_LEVELS)
    public int numWaterLevelsPlayed;

    @GsonRevision(min = 0x3d0)
    public boolean autoFilterOn;

    @GsonRevision(min = 0x3d3)
    public boolean hasUsed6Axis, hasUsedMove;

    @GsonRevision(min = 0x3dc)
    public PaintProperties paintProperties;

    @GsonRevision(min = 0x3ef)
    @GsonRevision(branch = 0x4431, min = Revisions.D1_DATALABELS)
    public PlayerDataLabels playerDataLabels;

    /* Vita stuff */

    @GsonRevision(branch = 0x4431, min = Revisions.D1_3G_CONNECTIVITY)
    public int total3GUpStream, total3GDownStream;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_TOUCH_CREATE)
    public boolean createRearTouchPan, createRearTouchPinchZoom, createRearTapZoom;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_CREATE_WARNING)
    public boolean createFrameRateWarningMessages;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_CREATE_BETA)
    public long onlineBetaNetworkTimestamp;

    @GsonRevision(branch = 0x4431, min = Revisions.D1_CREATE_BETA)
    public int onlineBetaPlayTimeSinceOnline, onlineBetaBootsSinceOnline;

    @GsonRevision(min = 0x3e3)
    public float distanceMovedWhilstAttracted;

    @GsonRevision(min = 0x3e5)
    public int beakersUsedPart1, beakersUsedPart2;

    @GsonRevision(min = 0x3ec)
    public int profileFlags;

    @GsonRevision(min = 0x3f2)
    public int[] goldMedalsAwarded;

    @GsonRevision(min = 0x3f2)
    public byte goldMedalStoryArcPins;

    @GsonRevision(min = 0x3f3)
    public boolean twitterEnabled, facebookEnabled;
    @GsonRevision(min = 0x3f3)
    public String twitterAccessToken, twitterAccessTokenSecret;

    @GsonRevision(min = 0x3f4)
    public boolean playedLBP2;

    @GsonRevision(min = 0x3f5)
    public boolean createChallengeTutorialSeen, playChallengeTutorialSeen;

    @GsonRevision(min = 0x3f8)
    public int ownedLBP;

    @GsonRevision(lbp3 = true, min = 0xf0)
    public int dceUuidState;

    @GsonRevision(lbp3 = true, min = 0x15c)
    public boolean lastPlayedPPP;

    @GsonRevision(lbp3 = true, min = 0x20f)
    public SlotID lastPlayedPlanet;

    @GsonRevision(lbp3 = true, min = 0x20f)
    public boolean lastPlayedEarthAdv;

    @GsonRevision(lbp3 = true, min = 0x20f)
    public boolean hasSeenDiveInBetaMessage;

    @GsonRevision(lbp3 = true, min = 0x184)
    public int touchCreateCursorMode;

    @GsonRevision(lbp3 = true, min = 0x188)
    public byte showAdvancedEditModeMessageCounter;

    @GsonRevision(lbp3 = true, min = 0x195)
    public boolean showAdventureSaveWarning;

    @GsonRevision(lbp3 = true, min = 0x1a0)
    public int totalFramesInEditMode;

    @GsonRevision(lbp3 = true, min = 0x1a2)
    public SHA1[] onlineTutorialsPlayed;

    @GsonRevision(lbp3 = true, min = 0x1a2)
    public SlotID[] popitPuzzlesCompleted;

    @GsonRevision(lbp3 = true, min = 0x1ab)
    public boolean timesaverNoticeViewed;

    @GsonRevision(lbp3 = true, min = 0x1ae)
    public int questProgressPin;

    @GsonRevision(lbp3 = true, min = 0x20a)
    public int[] activityFilterToggled;

    @GsonRevision(lbp3 = true, min = 0x213)
    public int lastLandingPageFocusItems;

    @GsonRevision(lbp3 = true, min = 0x216)
    public GoPlayCache[] goPlayCache;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        if (version < 0x187)
        {
            // descriptor array with types, no pointer
            return;
        }

        inventory = serializer.arraylist(inventory, InventoryItem.class);
        stringTable = serializer.struct(stringTable, StringLookupTable.class);

        if (version >= 0x3b6)
            fromProductionBuild = serializer.bool(fromProductionBuild);

        if (version >= 0x133 && version < 0x1df)
        {
            // SlotID[]
        }

        if (version < 0x269 && version > 0x128)
        {
            // HashMap<SlotID, int> LockStates
        }

        if (version > 0x1e3)
        {
            if (serializer.isWriting())
            {
                MemoryOutputStream stream = serializer.getOutput();
                ArrayList<TutorialLevel> keys =
                    new ArrayList<>(lbp1TutorialStates.keySet());
                keys.sort((l, r) -> l.getValue() - r.getValue());
                stream.i32(keys.size());
                for (TutorialLevel key : keys)
                {
                    stream.enum32(key);
                    stream.enum32(lbp1TutorialStates.get(key));
                }
            }
            else
            {
                MemoryInputStream stream = serializer.getInput();
                int count = stream.i32();
                lbp1TutorialStates = new HashMap<TutorialLevel, TutorialState>(count);
                for (int i = 0; i < count; ++i)
                    lbp1TutorialStates.put(stream.enum32(TutorialLevel.class),
                        stream.enum32(TutorialState.class));
            }
        }

        if (version > 0x265)
        {
            dlcPackViewed = serializer.intarray(dlcPackViewed, true);
            dlcPackShown = serializer.intarray(dlcPackShown, true);
        }

        if (version > 0x1e4)
            lbp1MainMenuButtonUnlocks = serializer.i32(lbp1MainMenuButtonUnlocks);

        if (version >= 0x16e)
            playedLevelData = serializer.arraylist(playedLevelData,
                PlayedLevelData.class);
        else
        {
            // HashMap<SlotID, OldSlot>
        }

        if (version > 0x379)
        {
            viewedLevelData = serializer.arraylist(viewedLevelData,
                ViewedLevelData.class);
            viewedPlayerData = serializer.arraylist(viewedPlayerData,
                ViewedPlayerData.class);
        }

        if (version > 0x16d)
        {
            if (serializer.isWriting())
            {
                Set<SlotID> keys = oldMyMoonSlots.keySet();
                serializer.getOutput().i32(keys.size());
                for (SlotID key : keys)
                {
                    serializer.struct(key, SlotID.class);
                    serializer.struct(oldMyMoonSlots.get(key), Slot.class);
                }
            }
            else
            {
                int count = serializer.getInput().i32();
                oldMyMoonSlots = new HashMap<SlotID, Slot>(count);
                for (int i = 0; i < count; ++i)
                    oldMyMoonSlots.put(
                        serializer.struct(null, SlotID.class),
                        serializer.struct(null, Slot.class));
            }
        }

        if (version > 0x20d)
            copiedFromAnotherUser = serializer.bool(copiedFromAnotherUser);
        if (version > 0x267)
            fixedUpByDeveloper = serializer.bool(fixedUpByDeveloper);
        if (version > 0x296 || revision.has(Branch.LEERDAMMER, Revisions.LD_SAVEGAME_INFO))
        {
            showSaveGameInfo = serializer.bool(showSaveGameInfo);
            totalLevelCount = serializer.i32(totalLevelCount);
        }

        if (version > 0x393)
            lastMmPicksCount = serializer.i32(lastMmPicksCount);

        if (version > 0x2cb || revision.has(Branch.LEERDAMMER, Revisions.LD_NEWS_TIMESTAMP))
            lastNewsItemTimestamp = serializer.s64(lastNewsItemTimestamp);

        if (version > 0x323)
            lastStreamEventTimestamp = serializer.s64(lastStreamEventTimestamp);

        if (version > 0x2cc || revision.has(Branch.LEERDAMMER, Revisions.LD_QUEUE))
            lolcatFtwTimestamp = serializer.s64(lolcatFtwTimestamp);

        if (version >= 0x328)
        {
            if (!serializer.isWriting())
                oldTextSearches = new String[serializer.getInput().i32()];
            else serializer.getOutput().i32(oldTextSearches.length);
            for (int i = 0; i < oldTextSearches.length; ++i)
                oldTextSearches[i] = serializer.wstr(oldTextSearches[i]);
        }

        oldDiscoveredLinks = serializer.array(oldDiscoveredLinks, SlotLink.class);
        pendingLinks = serializer.array(pendingLinks, SlotLink.class);

        lastPlayed = serializer.struct(lastPlayed, SlotID.class);

        if (version < 0x170)
        {
            // ArrayList<SlotID>
        }

        if (version > 0x17d)
            lbp1VideosPlayed = serializer.intarray(lbp1VideosPlayed); //
        // ArrayList<GUID>
        if (version > 0x36b)
            lbp2VideosPlayed = serializer.intarray(lbp2VideosPlayed); //
        // ArrayList<GUID>

        if (version > 0x16f)
            currentPlayerMetrics = serializer.struct(currentPlayerMetrics,
                PlayerMetrics.class);

        if (version > 0x1ab)
            lastPostedPlayerMetrics = serializer.struct(lastPostedPlayerMetrics,
                PlayerMetrics.class);

        if (version > 0x1b8)
            lbp1GameProgressionStatus =
                serializer.enum32(lbp1GameProgressionStatus);

        if (version > 0x1cf)
        {
            lbp1GameProgressionEventHappenedBits =
                serializer.i32(lbp1GameProgressionEventHappenedBits);
            lbp1GameProgressionEventsExplainedBits =
                serializer.i32(lbp1GameProgressionEventsExplainedBits);
        }

        if (version > 0x26c)
            demoProgressionStatus = serializer.i32(demoProgressionStatus);

        if (version > 0x36b)
            lbp2GameProgressionFlags = serializer.i32(lbp2GameProgressionFlags);

        if (version >= 0x387)
            pendingInventoryItems = serializer.array(pendingInventoryItems,
                InventoryItem.class);

        podLevel = serializer.resource(podLevel, ResourceType.LEVEL, true);

        if (version > 0x14f)
            playerId = serializer.struct(playerId, NetworkPlayerID.class);

        if (version > 0x296 || revision.has(Branch.LEERDAMMER, Revisions.LD_SAVEGAME_INFO))
        {
            ownerPlayerId = serializer.struct(ownerPlayerId, NetworkPlayerID.class);
            ownerPsid = serializer.struct(ownerPsid, OpenPSID.class);
        }

        if (version > 0x2eb || revision.has(Branch.LEERDAMMER, Revisions.LD_OWNER_ID))
            ownerUserId = serializer.i32(ownerUserId);

        if (version > 0x175)
            eulaAgreed = serializer.sha1(eulaAgreed);

        if (version >= 0x3d5)
            acceptingPlayer = serializer.struct(acceptingPlayer,
                NetworkPlayerID.class);

        syncedProfile = serializer.resource(syncedProfile,
            ResourceType.SYNCED_PROFILE, true);

        if (version > 0x1a1)
        {
            voipMutedPlayers = serializer.array(voipMutedPlayers,
                NetworkPlayerID.class);
            voipChatPaused = serializer.bool(voipChatPaused);
            enableMusicInPlayMode = serializer.bool(enableMusicInPlayMode);
        }

        if (version > 0x1b4)
            enableMusicInPod = serializer.bool(enableMusicInPod);

        if (version > 0x1b5)
        {
            legacyInventoryCollections =
                serializer.array(legacyInventoryCollections,
                    LegacyInventoryCollection.class, true);
            legacyInventoryViews = serializer.array(legacyInventoryViews,
                InventoryView.class, true);
        }

        if (version > 0x316)
            inventoryCollections = serializer.array(inventoryCollections,
                InventoryCollection.class, true);

        if (version > 0x1e0)
            avatarIcon = serializer.resource(avatarIcon, ResourceType.TEXTURE,
                true);

        if (version > 0x23c)
            saveIcon = serializer.resource(saveIcon, ResourceType.TEXTURE, true);

        if (version > 0x1e5)
            lbp1CreateModeVOProgress = serializer.i32(lbp1CreateModeVOProgress);

        if (version > 0x1ff)
        {
            gamma = serializer.f32(gamma);
            screenSize = serializer.f32(screenSize);
        }

        if (version > 0x35e)
            hasSeenCalibrationScreen = serializer.bool(hasSeenCalibrationScreen);

        if (version > 0x205)
            lbp1VOPlayed = serializer.intvector(lbp1VOPlayed, true);
        if (version > 0x36b)
            lbp2VOPlayed = serializer.intvector(lbp2VOPlayed, true);

        if (version > 0x23a)
            subtitleMode = serializer.s32(subtitleMode);

        if (version > 0x255 && version < 0x351)
        {
            serializer.i32(0);
        }

        if (version > 0x32e)
        {
            pinsAwarded = serializer.struct(pinsAwarded, PinsAwarded.class);
        }

        if (version > 0x260)
        {
            userSettingPhotoBooth = serializer.bool(userSettingPhotoBooth);

            if (subVersion > 0x17e)
                userSettingAdvancedEditMode =
                    serializer.bool(userSettingAdvancedEditMode);

            userSettingCollection = serializer.bool(userSettingCollection);

            if (version == 0x261)
                serializer.bool(false);
        }

        if (version > 0x2d2 || revision.has(Branch.LEERDAMMER, Revisions.LD_THERMOMETER))
            showThermometer = serializer.bool(showThermometer);

        if (version > 0x383)
            saveOnlinePlaySettings = serializer.bool(saveOnlinePlaySettings);

        if (version > 0x30d)
        {
            onlinePlayMode = serializer.i32(onlinePlayMode);
            friendJoinRequestMode = serializer.i32(friendJoinRequestMode);
            nonFriendJoinRequestMode = serializer.i32(nonFriendJoinRequestMode);
        }

        if (version > 0x352)
        {
            lbp2TutorialsPlayed = serializer.intarray(lbp2TutorialsPlayed);
            sectionHeadingToggled = serializer.intvector(sectionHeadingToggled);
        }

        if (subVersion > 0x20d)
            mysteryPodEventsSeen = serializer.array(mysteryPodEventsSeen,
                MysteryPodEventSeen.class);

        if (version > 0x366)
        {
            if (!serializer.isWriting())
                lastLegacyImportedProfileHashLBP1 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(lastLegacyImportedProfileHashLBP1.length);
            for (int i = 0; i < lastLegacyImportedProfileHashLBP1.length; ++i)
                lastLegacyImportedProfileHashLBP1[i] =
                    serializer.sha1(lastLegacyImportedProfileHashLBP1[i]);
        }

        if (subVersion > 0x37)
        {
            if (!serializer.isWriting())
                lastLegacyImportedProfileHashLBP2 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(lastLegacyImportedProfileHashLBP2.length);
            for (int i = 0; i < lastLegacyImportedProfileHashLBP2.length; ++i)
                lastLegacyImportedProfileHashLBP2[i] =
                    serializer.sha1(lastLegacyImportedProfileHashLBP2[i]);
        }

        if (version > 0x36b)
            playedLBP1 = serializer.bool(playedLBP1);

        if (subVersion > 0x37)
            playedLBP2 = serializer.bool(playedLBP2);

        if (subVersion > 0x12c)
            hasPerformedFirstRun = serializer.bool(hasPerformedFirstRun);

        if (version > 0x3ae)
            lbp2TutorialUnlockButtonUnleashed =
                serializer.bool(lbp2TutorialUnlockButtonUnleashed);

        if (version >= 0x28c)
            hiddenCategories = serializer.intvector(hiddenCategories);

        if (version >= 0x2d2)
            collectedBubbles = serializer.array(collectedBubbles,
                CollectedBubble.class);

        if (version > 0x2f5 || revision.has(Branch.LEERDAMMER, Revisions.LD_WATER_LEVELS))
            numWaterLevelsPlayed = serializer.i32(numWaterLevelsPlayed);

        if (version > 0x3cf)
            autoFilterOn = serializer.bool(autoFilterOn);

        if (version > 0x3d2)
        {
            hasUsed6Axis = serializer.bool(hasUsed6Axis);
            hasUsedMove = serializer.bool(hasUsedMove);
        }

        if (revision.has(Branch.DOUBLE11, 0x86))
            serializer.bool(true); // hasSeenCrossCompatInfo
        if (revision.has(Branch.DOUBLE11, 0x87))
            serializer.bool(false); // wantsCrossCompatDownloadNotification

        if (version > 0x3db)
            paintProperties = serializer.struct(paintProperties,
                PaintProperties.class);


        if (revision.has(Branch.DOUBLE11, Revisions.D1_DATALABELS))
        {
            playerDataLabels = serializer.struct(playerDataLabels,
                PlayerDataLabels.class);
            if (revision.has(Branch.DOUBLE11, Revisions.D1_3G_CONNECTIVITY))
            {
                total3GUpStream = serializer.i32(total3GUpStream);
                total3GDownStream = serializer.i32(total3GDownStream);
                if (revision.has(Branch.DOUBLE11, Revisions.D1_TOUCH_CREATE))
                {
                    createRearTouchPan = serializer.bool(createRearTouchPan);
                    createRearTouchPinchZoom =
                        serializer.bool(createRearTouchPinchZoom);
                    createRearTapZoom = serializer.bool(createRearTapZoom);
                    if (revision.has(Branch.DOUBLE11, Revisions.D1_CREATE_WARNING))
                    {
                        createFrameRateWarningMessages =
                            serializer.bool(createFrameRateWarningMessages);
                        if (revision.has(Branch.DOUBLE11, Revisions.D1_CREATE_BETA))
                        {
                            onlineBetaNetworkTimestamp =
                                serializer.s64(onlineBetaNetworkTimestamp);
                            onlineBetaPlayTimeSinceOnline =
                                serializer.i32(onlineBetaPlayTimeSinceOnline);
                            onlineBetaBootsSinceOnline =
                                serializer.i32(onlineBetaBootsSinceOnline);
                        }
                    }
                }
            }
        }

        if (version > 0x3e2)
            distanceMovedWhilstAttracted =
                serializer.f32(distanceMovedWhilstAttracted);

        if (version > 0x3e4)
        {
            beakersUsedPart1 = serializer.s32(beakersUsedPart1);
            beakersUsedPart2 = serializer.s32(beakersUsedPart2);
        }

        if (version > 0x3eb)
            profileFlags = serializer.i32(profileFlags);

        if (version > 0x3ee)
            playerDataLabels = serializer.struct(playerDataLabels,
                PlayerDataLabels.class);

        if (version > 0x3f1)
        {
            goldMedalsAwarded = serializer.intvector(goldMedalsAwarded);
            goldMedalStoryArcPins = serializer.i8(goldMedalStoryArcPins);
        }

        if (version > 0x3f2)
        {
            twitterEnabled = serializer.bool(twitterEnabled);
            facebookEnabled = serializer.bool(facebookEnabled);
            twitterAccessToken = serializer.str(twitterAccessToken);
            twitterAccessTokenSecret = serializer.str(twitterAccessTokenSecret);
        }

        if (version > 0x3f3)
            playedLBP2 = serializer.bool(playedLBP2);

        if (version > 0x3f4)
        {
            createChallengeTutorialSeen =
                serializer.bool(createChallengeTutorialSeen);
            playChallengeTutorialSeen = serializer.bool(playChallengeTutorialSeen);
        }

        if (version > 0x3f5 && version < 0x3f9)
        {
            if (!serializer.isWriting())
                lastLegacyImportedProfileHashLBP2 = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(lastLegacyImportedProfileHashLBP2.length);
            for (int i = 0; i < lastLegacyImportedProfileHashLBP2.length; ++i)
                lastLegacyImportedProfileHashLBP2[i] =
                    serializer.sha1(lastLegacyImportedProfileHashLBP2[i]);
        }

        if (version > 0x3f7)
            ownedLBP = serializer.i32(ownedLBP);

        if (subVersion > 0xef)
            dceUuidState = serializer.i32(dceUuidState);

        if (subVersion > 0x15b)
            lastPlayedPPP = serializer.bool(lastPlayedPPP);

        if (subVersion > 0x20e)
        {
            lastPlayedPlanet = serializer.struct(lastPlayedPlanet, SlotID.class);
            lastPlayedEarthAdv = serializer.bool(lastPlayedEarthAdv);
            hasSeenDiveInBetaMessage = serializer.bool(hasSeenDiveInBetaMessage);
        }

        if (subVersion > 0x183)
            touchCreateCursorMode = serializer.i32(touchCreateCursorMode);

        if (subVersion > 0x187)
            showAdvancedEditModeMessageCounter =
                serializer.i8(showAdvancedEditModeMessageCounter);

        if (subVersion > 0x194)
            showAdventureSaveWarning = serializer.bool(showAdventureSaveWarning);

        if (subVersion > 0x19f)
            totalFramesInEditMode = serializer.i32(totalFramesInEditMode);

        if (subVersion > 0x1a1)
        {
            if (!serializer.isWriting())
                onlineTutorialsPlayed = new SHA1[serializer.getInput().i32()];
            else serializer.getOutput().i32(onlineTutorialsPlayed.length);
            for (int i = 0; i < onlineTutorialsPlayed.length; ++i)
                onlineTutorialsPlayed[i] =
                    serializer.sha1(onlineTutorialsPlayed[i]);

            popitPuzzlesCompleted = serializer.array(popitPuzzlesCompleted,
                SlotID.class);
        }

        if (subVersion > 0x1aa)
            timesaverNoticeViewed = serializer.bool(timesaverNoticeViewed);
        if (subVersion > 0x1ad)
            questProgressPin = serializer.s32(questProgressPin);
        if (subVersion > 0x209)
            activityFilterToggled = serializer.intvector(activityFilterToggled);
        if (subVersion > 0x212)
            lastLandingPageFocusItems = serializer.i32(lastLandingPageFocusItems);
        if (subVersion > 0x215)
            goPlayCache = serializer.array(goPlayCache, GoPlayCache.class);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        size += this.stringTable.getAllocatedSize();
        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(0xFFFF00 + (this.inventory.size() * 0xC0),
            revision, compressionFlags);
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
     *
     * @param plan       Plan to add
     * @param descriptor GUID/SHA1 reference to the plan to add
     * @param table      Translation table for resolving categories/locations
     * @return Added inventory item
     */
    public InventoryItem addItem(RPlan plan, ResourceDescriptor descriptor,
                                 RTranslationTable table)
    {
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

        if (table != null)
        {
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

            details.categoryIndex = (short) this.stringTable.add(category,
                (int) (plan.inventoryData.category & 0xffffffff));
            details.locationIndex = (short) this.stringTable.add(location,
                (int) (plan.inventoryData.location & 0xffffffff));
        }

        item.details = plan.inventoryData;
        item.flags = 4;
        item.UID = this.getLastInventoryUID() + 1;

        this.inventory.add(item);

        return item;
    }

    public int getLastInventoryUID()
    {
        int UID = 1;
        for (InventoryItem item : this.inventory)
            if (item.UID > UID)
                UID = item.UID;
        return UID;
    }

    public boolean hasItem(ResourceDescriptor descriptor)
    {
        if (descriptor == null) return false;
        if (descriptor.isGUID()) return this.hasItem(descriptor.getGUID());
        else if (descriptor.isHash()) return this.hasItem(descriptor.getSHA1());
        return false;
    }

    public boolean hasItem(SHA1 hash)
    {
        for (InventoryItem item : this.inventory)
        {
            ResourceDescriptor plan = item.plan;
            if (plan == null) continue;
            if (plan.isHash() && plan.getSHA1().equals(hash))
                return true;
        }
        return false;
    }

    public boolean hasItem(GUID guid)
    {
        for (InventoryItem item : this.inventory)
        {
            ResourceDescriptor plan = item.plan;
            if (plan == null) continue;
            if (plan.isGUID() && plan.getGUID().equals(guid))
                return true;
        }
        return false;
    }

    public InventoryItem getItem(ResourceDescriptor descriptor)
    {
        if (descriptor == null) return null;
        for (InventoryItem item : this.inventory)
            if (descriptor.equals(item.plan))
                return item;
        return null;
    }
}
