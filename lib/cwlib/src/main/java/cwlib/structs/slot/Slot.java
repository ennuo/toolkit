package cwlib.structs.slot;

import cwlib.enums.*;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.server.SlotDescriptor;
import cwlib.types.data.*;
import cwlib.util.Strings;
import org.joml.Vector4f;

public class Slot implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE =
        (SlotID.BASE_ALLOCATION_SIZE * 3) + (0x20 * 2) + NetworkOnlineID.BASE_ALLOCATION_SIZE + 0x30;

    public SlotID id = new SlotID();
    public ResourceDescriptor root;
    @GsonRevision(lbp3 = true, min = 325)
    public ResourceDescriptor adventure;
    public ResourceDescriptor icon;
    public Vector4f location = new Vector4f().zero();
    public NetworkOnlineID authorID;
    @GsonRevision(min = 0x13b)
    public String authorName;
    @GsonRevision(min = 0x183)
    public String translationTag;
    public String name;
    public String description;
    public SlotID primaryLinkLevel = new SlotID();
    @GsonRevision(min = 0x134)
    public SlotID group = new SlotID();
    public boolean initiallyLocked;
    @GsonRevision(min = 0x238)
    public boolean shareable;
    @GsonRevision(min = 0x238)
    public GUID backgroundGUID;
    @GsonRevision(min = 0x1df)
    public LevelType developerLevelType = LevelType.MAIN_PATH;
    @GsonRevision(min = 0x1b9, max = 0x36b)
    public GameProgressionStatus gameProgressionState = GameProgressionStatus.NEW_GAME;

    /* LBP2 fields */

    @GsonRevision(min = 0x333)
    public ResourceDescriptor planetDecorations;
    @GsonRevision(min = 0x33c)
    public Label[] labels;
    @GsonRevision(min = 0x2ea)
    public Collectabubble[] collectabubblesRequired;
    @GsonRevision(min = 0x2f4)
    public Collectabubble[] collectabubblesContained;
    @GsonRevision(min = 0x352)
    public boolean isSubLevel;
    @GsonRevision(min = 0x3d0)
    public byte minPlayers = 1, maxPlayers = 4;
    @GsonRevision(min = 0x3d0)
    public boolean moveRecommended;
    @GsonRevision(min = 0x3e9)
    public boolean crossCompatible;
    @GsonRevision(min = 0x3d0)
    public boolean showOnPlanet = true;
    @GsonRevision(min = 0x3d0)
    public byte livesOverride;

    /* LBP3 fields */
    @GsonRevision(branch = 0x4431, min = 76)
    @GsonRevision(lbp3 = true, min = 533)
    public boolean enforceMinMaxPlayers;

    @GsonRevision(lbp3 = true, min = 18)
    public int gameMode = 0;
    @GsonRevision(lbp3 = true, min = 210)
    public boolean isGameKit;
    @GsonRevision(lbp3 = true, min = 283)
    public String entranceName;
    @GsonRevision(lbp3 = true, min = 283)
    public SlotID originalSlotID = new SlotID();
    @GsonRevision(lbp3 = true, min = 339)
    public byte customBadgeSize = 1;
    @GsonRevision(lbp3 = true, min = 402)
    public String localPath;
    @GsonRevision(lbp3 = true, min = 518)
    public String thumbPath;

    /* Vita fields */
    @GsonRevision(branch = 0x4431, min = 61)
    public boolean acingEnabled = true;
    @GsonRevision(branch = 0x4431, min = 61)
    public int[] customRewardEnabled;
    @GsonRevision(branch = 0x4431, min = 61)
    public String[] rewardConditionDescription;
    @GsonRevision(branch = 0x4431, min = 61)
    public int[] customRewardCondition;
    @GsonRevision(branch = 0x4431, min = 61)
    public float[] amountNeededCustomReward;
    @GsonRevision(branch = 0x4431, min = 61)
    public String[] customRewardDescription;

    @GsonRevision(branch = 0x4431, min = 94)
    public boolean containsCollectabubbles;
    @GsonRevision(branch = 0x4431, min = 77)
    public boolean sameScreenGame;
    @GsonRevision(branch = 0x4431, min = 93)
    public int sizeOfResources;
    @GsonRevision(branch = 0x4431, min = 93)
    public int sizeOfSubLevels;

    @GsonRevision(branch = 0x4431, min = 93)
    public SlotID[] subLevels;
    @GsonRevision(branch = 0x4431, min = 93)
    public ResourceDescriptor slotList;
    @GsonRevision(branch = 0x4431, min = 128)
    public short revision;

    // Server specific data
    public transient int gameVersion;
    public transient SHA1[] resources;

    public Slot() { }

    public Slot(SlotID id, ResourceDescriptor root, Vector4f location)
    {
        this.id = id;
        this.root = root;
        this.location = location;
    }

    public Slot(SlotDescriptor descriptor)
    {
        this.id = new SlotID(SlotType.USER_CREATED_ON_SERVER, descriptor.id);
        this.name = descriptor.name;
        this.description = descriptor.description;

        if (Strings.isSHA1(descriptor.root) || Strings.isGUID(descriptor.root))
        {
            if (descriptor.isAdventurePlanet)
                this.adventure = new ResourceDescriptor(descriptor.root,
                    ResourceType.ADVENTURE_CREATE_PROFILE);
            else
                this.root = new ResourceDescriptor(descriptor.root, ResourceType.LEVEL);
        }

        if (Strings.isSHA1(descriptor.icon) || Strings.isGUID(descriptor.icon))
            this.icon = new ResourceDescriptor(descriptor.icon, ResourceType.TEXTURE);
        if (descriptor.labels != null)
        {
            this.labels = new Label[descriptor.labels.length];
            for (int i = 0; i < this.labels.length; ++i)
                this.labels[i] =
                    new Label((int) RTranslationTable.makeLamsKeyID(descriptor.labels[i]), i);
        }

        this.initiallyLocked = descriptor.locked;
        this.isSubLevel = descriptor.subLevel;
        this.shareable = descriptor.shareable != 0;
        if (descriptor.background != 0)
            this.backgroundGUID = new GUID(descriptor.background);

        this.minPlayers = (byte) descriptor.minPlayers;
        this.maxPlayers = (byte) descriptor.maxPlayers;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();

        id = serializer.struct(id, SlotID.class);

        root = serializer.resource(root, ResourceType.LEVEL, true);
        if (subVersion >= Revisions.ADVENTURE)
            adventure = serializer.resource(adventure, ResourceType.LEVEL, true);
        icon = serializer.resource(icon, ResourceType.TEXTURE, true);

        location = serializer.v4(location);

        authorID = serializer.struct(authorID, NetworkOnlineID.class);
        if (version >= 0x13b)
            authorName = serializer.wstr(authorName);

        if (version >= 0x183)
            translationTag = serializer.str(translationTag);

        name = serializer.wstr(name);
        description = serializer.wstr(description);

        primaryLinkLevel = serializer.struct(primaryLinkLevel, SlotID.class);
        if (version >= 0x134)
            group = serializer.struct(group, SlotID.class);

        initiallyLocked = serializer.bool(initiallyLocked);

        if (version > 0x237)
        {
            shareable = serializer.bool(shareable);
            backgroundGUID = serializer.guid(backgroundGUID);
        }

        if (version >= 0x333)
            planetDecorations = serializer.resource(planetDecorations,
                ResourceType.PLAN, true);

        if (version < 0x188)
            serializer.u8(0); // Unknown

        if (version > 0x1de)
            developerLevelType = serializer.enum32(developerLevelType);
        else
            serializer.bool(false); // SideMission

        if (version > 0x1ad && version < 0x1b9)
            serializer.u8(0); // Unknown

        if (version > 0x1b8 && version < 0x36c)
            gameProgressionState = serializer.enum32(gameProgressionState);

        if (version <= 0x2c3) return;

        if (version >= 0x33c)
            labels = serializer.array(labels, Label.class);

        if (version >= 0x2ea)
            collectabubblesRequired = serializer.array(collectabubblesRequired,
                Collectabubble.class);
        if (version >= 0x2f4)
            collectabubblesContained = serializer.array(collectabubblesContained,
                Collectabubble.class);

        if (version >= 0x352)
            isSubLevel = serializer.bool(isSubLevel);

        if (version < 0x3d0) return;

        minPlayers = serializer.i8(minPlayers);
        maxPlayers = serializer.i8(maxPlayers);

        if (subVersion >= Revisions.SLOT_ENFORCE_MINMAX)
            enforceMinMaxPlayers = serializer.bool(enforceMinMaxPlayers);

        if (version >= 0x3d0)
            moveRecommended = serializer.bool(moveRecommended);

        if (version >= 0x3e9)
            crossCompatible = serializer.bool(crossCompatible);

        if (version >= 0x3d1)
            showOnPlanet = serializer.bool(showOnPlanet);
        if (version >= 0x3d2)
            livesOverride = serializer.i8(livesOverride);

        if (revision.isVita())
        {
            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_REWARDS))
            {
                acingEnabled = serializer.bool(acingEnabled);
                customRewardEnabled = serializer.intarray(customRewardEnabled);

                if (!serializer.isWriting())
                    rewardConditionDescription = new String[serializer.getInput().i32()];
                else serializer.getOutput().i32(rewardConditionDescription.length);
                for (int i = 0; i < rewardConditionDescription.length; ++i)
                    rewardConditionDescription[i] =
                        serializer.wstr(rewardConditionDescription[i]);

                customRewardCondition = serializer.intarray(customRewardCondition);
                amountNeededCustomReward =
                    serializer.floatarray(amountNeededCustomReward);

                if (!serializer.isWriting())
                    customRewardDescription = new String[serializer.getInput().i32()];
                else serializer.getOutput().i32(customRewardDescription.length);
                for (int i = 0; i < customRewardDescription.length; ++i)
                    customRewardDescription[i] =
                        serializer.wstr(customRewardDescription[i]);
            }

            if (revision.has(Branch.DOUBLE11, Revisions.D1_COLLECTABUBBLES))
                containsCollectabubbles = serializer.bool(containsCollectabubbles);

            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_ENFORCE_MINMAX))
                enforceMinMaxPlayers = serializer.bool(enforceMinMaxPlayers);

            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_SAME_SCREEN))
                sameScreenGame = serializer.bool(sameScreenGame);

            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_DOWNLOAD_DATA))
            {
                sizeOfResources = serializer.i32(sizeOfResources);
                sizeOfSubLevels = serializer.i32(sizeOfSubLevels);
                subLevels = serializer.array(subLevels, SlotID.class);
                slotList = serializer.resource(slotList, ResourceType.SLOT_LIST, true);
            }

            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_REVISION))
                this.revision = serializer.i16(this.revision);
        }

        if (!revision.isLBP3()) return;

        if (subVersion >= Revisions.SLOT_GAME_MODE)
            gameMode = serializer.u8(gameMode);

        if (subVersion >= Revisions.SLOT_GAME_KIT)
            isGameKit = serializer.bool(isGameKit);

        if (subVersion >= Revisions.SLOT_ENTRANCE_DATA)
        {
            entranceName = serializer.wstr(entranceName);
            originalSlotID = serializer.struct(originalSlotID, SlotID.class);
        }

        if (subVersion >= Revisions.SLOT_BADGE_SIZE)
            customBadgeSize = serializer.i8(customBadgeSize);

        if (subVersion >= Revisions.SLOT_TRAILER_PATH)
        {
            localPath = serializer.str(localPath);
            if (subVersion >= Revisions.SLOT_TRAILER_THUMBNAIL)
                thumbPath = serializer.str(thumbPath);
        }
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.authorName != null)
            size += (this.authorName.length() * 2);
        if (this.translationTag != null)
            size += (this.translationTag.length() * 2);
        if (this.name != null)
            size += (this.name.length() * 2);
        if (this.description != null)
            size += (this.description.length() * 2);
        return size;
    }

    @Override
    public String toString()
    {
        if (this.translationTag != null && !this.translationTag.isEmpty())
        {
            if (ResourceSystem.getLAMS() == null) return this.translationTag;
            String translated = ResourceSystem.getLAMS().translate(this.translationTag +
                                                                   "_NAME");
            if (translated != null) return translated;
            return this.translationTag;
        }
        if (this.name == null || this.name.isEmpty())
            return "Unnamed Level";
        return this.name;
    }

    public boolean isAdventure()
    {
        return this.adventure != null && this.root == null;
    }

    public boolean isLevel()
    {
        return this.adventure == null && this.root != null;
    }
}
