package cwlib.structs.slot;

import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.enums.Branch;
import cwlib.enums.GameProgressionStatus;
import cwlib.enums.LevelType;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.GUID;
import cwlib.types.data.NetworkOnlineID;

import org.joml.Vector4f;

public class Slot implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 
        (SlotID.BASE_ALLOCATION_SIZE * 3) + (0x20 * 2) + NetworkOnlineID.BASE_ALLOCATION_SIZE + 0x30;
    
    public SlotID id = new SlotID();
    public ResourceDescriptor root;
    @GsonRevision(lbp3=true,min=325) public ResourceDescriptor adventure;
    public ResourceDescriptor icon;
    public Vector4f location = new Vector4f().zero();
    public NetworkOnlineID authorID;
    @GsonRevision(min=0x13b) public String authorName;
    @GsonRevision(min=0x183) public String translationTag;
    public String name;
    public String description;
    public SlotID primaryLinkLevel = new SlotID();
    @GsonRevision(min=0x134) public SlotID group = new SlotID();
    public boolean initiallyLocked;
    @GsonRevision(min=0x238) public boolean shareable;
    @GsonRevision(min=0x238) public GUID backgroundGUID;
    @GsonRevision(min=0x1df) public LevelType developerLevelType = LevelType.MAIN_PATH;
    @GsonRevision(min=0x1b9,max=0x36b) public GameProgressionStatus gameProgressionState = GameProgressionStatus.NEW_GAME;

    /* LBP2 fields */

    @GsonRevision(min=0x333) public ResourceDescriptor planetDecorations;
    @GsonRevision(min=0x33c) public Label[] labels;
    @GsonRevision(min=0x2ea) public Collectabubble[] collectabubblesRequired;
    @GsonRevision(min=0x2f4) public Collectabubble[] collectabubblesContained;
    @GsonRevision(min=0x352) public boolean isSubLevel;
    @GsonRevision(min=0x3d0) public byte minPlayers = 1, maxPlayers = 4;
    @GsonRevision(min=0x3d0) public boolean moveRecommended;
    @GsonRevision(min=0x3e9) public boolean crossCompatible;
    @GsonRevision(min=0x3d0) public boolean showOnPlanet = true;
    @GsonRevision(min=0x3d0) public byte livesOverride;

    /* LBP3 fields */
    @GsonRevision(branch=0x4431,min=76)
    @GsonRevision(lbp3=true,min=533) 
    public boolean enforceMinMaxPlayers;

    @GsonRevision(lbp3=true,min=18) public int gameMode = 0;
    @GsonRevision(lbp3=true,min=210)  public boolean isGameKit;
    @GsonRevision(lbp3=true,min=283) public String entranceName;
    @GsonRevision(lbp3=true,min=283) public SlotID originalSlotID = new SlotID();
    @GsonRevision(lbp3=true,min=339) public byte customBadgeSize = 1;
    @GsonRevision(lbp3=true,min=402) public String localPath;
    @GsonRevision(lbp3=true,min=518) public String thumbPath;

    /* Vita fields */
    @GsonRevision(branch=0x4431,min=61)public boolean acingEnabled = true;
    @GsonRevision(branch=0x4431,min=61) public int[] customRewardEnabled;
    @GsonRevision(branch=0x4431,min=61) public String[] rewardConditionDescription;
    @GsonRevision(branch=0x4431,min=61) public int[] customRewardCondition;
    @GsonRevision(branch=0x4431,min=61) public float[] amountNeededCustomReward;
    @GsonRevision(branch=0x4431,min=61) public String[] customRewardDescription;

    @GsonRevision(branch=0x4431,min=94) public boolean containsCollectabubbles;
    @GsonRevision(branch=0x4431,min=77) public boolean sameScreenGame;
    @GsonRevision(branch=0x4431,min=93) public int sizeOfResources;
    @GsonRevision(branch=0x4431,min=93) public int sizeOfSubLevels;

    @GsonRevision(branch=0x4431,min=93) public SlotID[] subLevels;
    @GsonRevision(branch=0x4431,min=93) public ResourceDescriptor slotList;
    @GsonRevision(branch=0x4431,min=128) public short revision;

    // Server specific data
    public transient int gameVersion;
    public transient SHA1[] resources;

    public Slot() {};
    public Slot(SlotID id, ResourceDescriptor root, Vector4f location) {
        this.id = id;
        this.root = root;
        this.location = location;
    }
    
    @SuppressWarnings("unchecked")
    @Override public Slot serialize(Serializer serializer, Serializable structure) {
        Slot slot = (structure == null) ? new Slot() : (Slot) structure;
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();
        int subVersion = revision.getSubVersion();
        
        slot.id = serializer.struct(slot.id, SlotID.class);

        slot.root = serializer.resource(slot.root, ResourceType.LEVEL, true);
        if (subVersion >= Revisions.ADVENTURE)
            slot.adventure = serializer.resource(slot.adventure, ResourceType.LEVEL, true);
        slot.icon = serializer.resource(slot.icon, ResourceType.TEXTURE, true);

        slot.location = serializer.v4(slot.location);

        slot.authorID = serializer.struct(slot.authorID, NetworkOnlineID.class);
        if (version >= 0x13b)
            slot.authorName = serializer.wstr(slot.authorName);

        if (version >= 0x183)
            slot.translationTag = serializer.str(slot.translationTag);

        slot.name = serializer.wstr(slot.name);
        slot.description = serializer.wstr(slot.description);

        slot.primaryLinkLevel = serializer.struct(slot.primaryLinkLevel, SlotID.class);
        if (version >= 0x134)
            slot.group = serializer.struct(slot.group, SlotID.class);

        slot.initiallyLocked = serializer.bool(slot.initiallyLocked);

        if (version > 0x237) {
            slot.shareable = serializer.bool(slot.shareable);
            slot.backgroundGUID = serializer.guid(slot.backgroundGUID);
        }

        if (version >= 0x333)
            slot.planetDecorations = serializer.resource(slot.planetDecorations, ResourceType.PLAN, true);

        if (version < 0x188)
            serializer.u8(0); // Unknown
        
        if (version > 0x1de)
            slot.developerLevelType = serializer.enum32(slot.developerLevelType);
        else 
            serializer.bool(false); // SideMission

        if (version > 0x1ad && version < 0x1b9)
            serializer.u8(0); // Unknown
            
        if (version > 0x1b8 && version < 0x36c)
            slot.gameProgressionState = serializer.enum32(slot.gameProgressionState);

        if (version <= 0x2c3) return slot;

        if (version >= 0x33c)
            slot.labels = serializer.array(slot.labels, Label.class);

        if (version >= 0x2ea)
            slot.collectabubblesRequired = serializer.array(slot.collectabubblesRequired, Collectabubble.class);
        if (version >= 0x2f4)
            slot.collectabubblesContained = serializer.array(slot.collectabubblesContained, Collectabubble.class);

        if (version >= 0x352)
            slot.isSubLevel = serializer.bool(slot.isSubLevel);
        
        if (version < 0x3d0) return slot;

        slot.minPlayers = serializer.i8(slot.minPlayers);
        slot.maxPlayers = serializer.i8(slot.maxPlayers);

        if (subVersion >= Revisions.SLOT_ENFORCE_MINMAX)
            slot.enforceMinMaxPlayers = serializer.bool(slot.enforceMinMaxPlayers);

        if (version >= 0x3d0)
            slot.moveRecommended = serializer.bool(slot.moveRecommended);

        if (version >= 0x3e9)
            slot.crossCompatible = serializer.bool(slot.crossCompatible);

        if (version >= 0x3d1)
            slot.showOnPlanet = serializer.bool(slot.showOnPlanet);
        if (version >= 0x3d2)
            slot.livesOverride = serializer.i8(slot.livesOverride);

        if (revision.isVita()) {
            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_REWARDS)) {
                slot.acingEnabled = serializer.bool(slot.acingEnabled);
                slot.customRewardEnabled = serializer.intarray(slot.customRewardEnabled);

                if (!serializer.isWriting()) slot.rewardConditionDescription = new String[serializer.getInput().i32()];
                else serializer.getOutput().i32(slot.rewardConditionDescription.length);
                for (int i = 0; i < slot.rewardConditionDescription.length; ++i)
                    slot.rewardConditionDescription[i] = serializer.wstr(slot.rewardConditionDescription[i]);

                slot.customRewardCondition = serializer.intarray(slot.customRewardCondition);
                slot.amountNeededCustomReward = serializer.floatarray(slot.amountNeededCustomReward);

                if (!serializer.isWriting()) slot.customRewardDescription = new String[serializer.getInput().i32()];
                else serializer.getOutput().i32(slot.customRewardDescription.length);
                for (int i = 0; i < slot.customRewardDescription.length; ++i)
                    slot.customRewardDescription[i] = serializer.wstr(slot.customRewardDescription[i]);
            }

            if (revision.has(Branch.DOUBLE11, Revisions.D1_COLLECTABUBBLES))
                slot.containsCollectabubbles = serializer.bool(slot.containsCollectabubbles);

            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_ENFORCE_MINMAX))
                slot.enforceMinMaxPlayers = serializer.bool(slot.enforceMinMaxPlayers);

            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_SAME_SCREEN))
                slot.sameScreenGame = serializer.bool(slot.sameScreenGame);

            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_DOWNLOAD_DATA)) {
                slot.sizeOfResources = serializer.i32(slot.sizeOfResources);
                slot.sizeOfSubLevels = serializer.i32(slot.sizeOfSubLevels);
                slot.subLevels = serializer.array(slot.subLevels, SlotID.class);
                slot.slotList = serializer.resource(slot.slotList, ResourceType.SLOT_LIST, true);
            }

            if (revision.has(Branch.DOUBLE11, Revisions.D1_SLOT_REVISION))
                slot.revision = serializer.i16(slot.revision);
        }

        if (!revision.isLBP3()) return slot;

        if (subVersion >= Revisions.SLOT_GAME_MODE)
            slot.gameMode = serializer.u8(slot.gameMode);

        if (subVersion >= Revisions.SLOT_GAME_KIT)
            slot.isGameKit = serializer.bool(slot.isGameKit);

        if (subVersion >= Revisions.SLOT_ENTRANCE_DATA) {
            slot.entranceName = serializer.wstr(slot.entranceName);
            slot.originalSlotID = serializer.struct(slot.originalSlotID, SlotID.class);
        }

        if (subVersion >= Revisions.SLOT_BADGE_SIZE)
            slot.customBadgeSize = serializer.i8(slot.customBadgeSize);

        if (subVersion >= Revisions.SLOT_TRAILER_PATH) {
            slot.localPath = serializer.str(slot.localPath);
            if (subVersion >= Revisions.SLOT_TRAILER_THUMBNAIL)
                slot.thumbPath = serializer.str(slot.thumbPath);
        }

        return slot;
    }

    @Override public int getAllocatedSize() { 
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

    @Override public String toString() {
        if (this.translationTag != null && !this.translationTag.isEmpty()) {
            if (ResourceSystem.getLAMS() == null) return this.translationTag;
            String translated = ResourceSystem.getLAMS().translate(this.translationTag + "_NAME");
            if (translated != null) return translated;
            return this.translationTag;
        }
        if (this.name == null || this.name.isEmpty())
            return "Unnamed Level";
        return this.name;
    }

    public boolean isAdventure() {
        return this.adventure != null && this.root == null;
    }
    
    public boolean isLevel() { 
        return this.adventure == null && this.root != null;
    }
}
