package cwlib.structs.inventory;

import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;
import cwlib.enums.Branch;
import cwlib.enums.InventoryObjectSubType;
import cwlib.enums.InventoryObjectType;
import cwlib.types.data.ResourceDescriptor;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SlotType;
import cwlib.enums.ToolType;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.data.GUID;
import cwlib.types.data.NetworkPlayerID;
import cwlib.structs.slot.SlotID;
import cwlib.structs.things.parts.PMetadata;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;

import java.util.Date;
import java.util.EnumSet;

public class InventoryItemDetails implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0xB0;

    @GsonRevision(branch = 0, max = 0x2ba)
    @GsonRevision(branch = 0x4c44, max = 0x7)
    public String translationTag = "";

    @GsonRevision(min = 0x222)
    public long dateAdded = new Date().getTime() / 1000;

    @GsonRevision(min = 0x177)
    public SlotID levelUnlockSlotID = new SlotID();

    @GsonRevision(min = 0x14f)
    public GUID highlightSound;

    @GsonRevision(min = 0x157)
    public int colour = -1;

    public EnumSet<InventoryObjectType> type = EnumSet.noneOf(InventoryObjectType.class);
    public int subType = InventoryObjectSubType.NONE;

    @GsonRevision(min = 0x2bb)
    @GsonRevision(branch = 0x4c44, min = 0x8)
    public long titleKey, descriptionKey;

    @GsonRevision(min = 0x1ab)
    public UserCreatedDetails userCreatedDetails;

    @GsonRevision(min = 0x1b1)
    public CreationHistory creationHistory;

    public ResourceDescriptor icon = new ResourceDescriptor(15525, ResourceType.TEXTURE);

    @GsonRevision(min = 0x17b)
    public InventoryItemPhotoData photoData;
    @GsonRevision(min = 0x162)
    public EyetoyData eyetoyData;

    public short locationIndex = -1, categoryIndex = -1;
    @GsonRevision(min = 0x195)
    public short primaryIndex;

    @GsonRevision(min = 0x1c1, max = 0x37c)
    public int lastUsed, numUses;

    @GsonRevision(min = 0x233, max = 0x37c)
    public int fluffCost;

    @GsonRevision(min = 0x205, max = 0x37c)
    public boolean allowEmit;
    @GsonRevision(min = 0x223, max = 0x37c)
    public boolean shareable;
    @GsonRevision(min = 0x182, max = 0x37c)
    public boolean copyright;

    @GsonRevision(min = 0x182)
    public NetworkPlayerID creator = new NetworkPlayerID();

    @GsonRevision(min = 0x197)
    public ToolType toolType = ToolType.NONE;
    @GsonRevision(min = 0x335)
    public byte flags;

    @GsonRevision(branch = 0x4431, min = 125)
    public boolean makeSizeProportional = true;

    @GsonRevision(min = 0x2bb)
    @GsonRevision(branch = 0x4c44, min = 0x8)
    public long location, category;

    @GsonRevision(branch = 0, max = 0x2ba)
    @GsonRevision(branch = 0x4c44, max = 0x7)
    public String categoryTag = "", locationTag = "";

    public transient String translatedTitle = "";
    public transient String translatedDescription;
    public transient String translatedLocation = "";
    public transient String translatedCategory = "";

    public InventoryItemDetails() { }

    public InventoryItemDetails(PMetadata metadata)
    {
        // if (metadata.nameTranslationTag != null && metadata.nameTranslationTag.endsWith
        // ("_NAME"))
        //     this.translationTag = metadata.nameTranslationTag.split("_NAME")[0]; // LOL

        this.titleKey = metadata.titleKey;
        this.descriptionKey = metadata.descriptionKey;
        this.location = metadata.location;
        this.category = metadata.category;

        this.locationTag = metadata.locationTag;
        this.categoryTag = metadata.categoryTag;

        this.primaryIndex = (short) metadata.primaryIndex;
        this.fluffCost = metadata.fluffCost;
        this.type = metadata.type;
        this.subType = metadata.subType;
        this.icon = metadata.icon;

        this.allowEmit = metadata.allowEmit;
    }

    @Override
    public void serialize(Serializer serializer)
    {
        int head = serializer.getRevision().getVersion();

        if (serializer.isWriting() && highlightSound != null)
            serializer.addDependency(new ResourceDescriptor(highlightSound,
                ResourceType.FILENAME));

        if (serializer.getRevision().getVersion() > 0x37c)
        {
            dateAdded = serializer.s64(dateAdded);
            levelUnlockSlotID = serializer.struct(levelUnlockSlotID, SlotID.class);
            highlightSound = serializer.guid(highlightSound);
            colour = serializer.i32(colour);


            if (serializer.isWriting())
                serializer.getOutput().i32(InventoryObjectType.getFlags(type));
            else
                type = InventoryObjectType.fromFlags(serializer.getInput().i32(),
                    serializer.getRevision());

            subType = serializer.i32(subType);

            titleKey = serializer.u32(titleKey);
            descriptionKey = serializer.u32(descriptionKey);

            creationHistory = serializer.reference(creationHistory,
                CreationHistory.class);
            icon = serializer.resource(icon, ResourceType.TEXTURE, true);
            userCreatedDetails = serializer.reference(userCreatedDetails,
                UserCreatedDetails.class);
            photoData = serializer.reference(photoData,
                InventoryItemPhotoData.class);
            eyetoyData = serializer.reference(eyetoyData, EyetoyData.class);

            locationIndex = serializer.i16(locationIndex);
            categoryIndex = serializer.i16(categoryIndex);
            primaryIndex = serializer.i16(primaryIndex);

            creator = serializer.reference(creator, NetworkPlayerID.class);

            toolType = ToolType.fromValue(serializer.i8(toolType.getValue()));
            flags = serializer.i8(flags);


            if (serializer.getRevision().has(Branch.DOUBLE11,
                Revisions.D1_DETAILS_PROPORTIONAL))
                makeSizeProportional = serializer.bool(makeSizeProportional);

            if (!serializer.isWriting())
                updateTranslations();

            return;
        }

        if (head < 0x233)
        {
            if (head < 0x174)
            {
                serializer.wstr(null); // nameTranslationTag
                serializer.wstr(null); // descTranslationTag
            }
            else
            {
                translationTag = serializer.str(translationTag);
            }

            locationIndex = (short) serializer.i32(locationIndex, true);
            categoryIndex = (short) serializer.i32(categoryIndex, true);
            if (head > 0x194)
                primaryIndex = (short) serializer.i32(primaryIndex, true);

            serializer.i32(0, true); // Pad

            if (serializer.isWriting())
                serializer.getOutput().i32(InventoryObjectType.getFlags(type), true);
            else
                type = InventoryObjectType.fromFlags(serializer.getInput().i32(true),
                    serializer.getRevision());
            subType = serializer.i32(subType, true);

            if (head > 0x196)
                toolType =
                    ToolType.fromValue((byte) serializer.i32(toolType.getValue(),
                        true));
            icon = serializer.resource(icon, ResourceType.TEXTURE, true);
            if (head > 0x1c0)
            {
                numUses = serializer.i32(numUses, true);
                lastUsed = serializer.i32(lastUsed, true);
            }

            if (head > 0x14e)
                highlightSound = serializer.guid(highlightSound, true);
            else
                serializer.str(null); // Path to highlight sound?

            if (head > 0x156)
                colour = serializer.i32(colour, true);

            if (head > 0x161)
            {
                eyetoyData = serializer.reference(eyetoyData, EyetoyData.class);
            }

            // 0x17a < revision && revision < 0x182
            // 0x181 < revision ???
            if (head > 0x17a)
                photoData = serializer.reference(photoData,
                    InventoryItemPhotoData.class);

            if (head > 0x176)
            {
                levelUnlockSlotID.slotType =
                    SlotType.fromValue(
                        serializer.i32(levelUnlockSlotID.slotType.getValue())
                    );

                levelUnlockSlotID.slotNumber =
                    serializer.u32(levelUnlockSlotID.slotNumber, true);
            }

            if (head > 0x181)
            {
                copyright = serializer.bool(copyright);
                creator = serializer.struct(creator, NetworkPlayerID.class);
            }

            if (head > 0x1aa)
            {
                userCreatedDetails = serializer.struct(userCreatedDetails,
                    UserCreatedDetails.class);
                if (userCreatedDetails != null &&
                    userCreatedDetails.name.isEmpty() &&
                    userCreatedDetails.description.isEmpty())
                    userCreatedDetails = null;
            }

            if (head > 0x1b0)
                creationHistory = serializer.struct(creationHistory,
                    CreationHistory.class);

            if (head > 0x204)
                allowEmit = serializer.bool(allowEmit);

            if (head > 0x221)
                dateAdded = serializer.s64(dateAdded, true);

            if (head > 0x222)
                shareable = serializer.bool(shareable);

            if (!serializer.isWriting())
                updateTranslations();

            return;
        }

        highlightSound = serializer.guid(highlightSound, true);

        // In these older versions of the inventory details,
        // 32 bit values are enforced while still using encoded values elsewhere,
        // so for some structures like SlotID, we need to force it manually.

        levelUnlockSlotID.slotType =
            SlotType.fromValue(
                serializer.i32(levelUnlockSlotID.slotType.getValue(), true)
            );

        levelUnlockSlotID.slotNumber =
            serializer.u32(levelUnlockSlotID.slotNumber, true);

        locationIndex = (short) serializer.i32(locationIndex, true);
        categoryIndex = (short) serializer.i32(categoryIndex, true);
        primaryIndex = (short) serializer.i32(primaryIndex, true);

        lastUsed = serializer.i32(lastUsed, true);
        numUses = serializer.i32(numUses, true);
        if (head > 0x234)
            serializer.i32(0, true); // Pad


        dateAdded = serializer.s64(dateAdded, true);

        fluffCost = serializer.i32(fluffCost, true);

        colour = serializer.i32(colour, true);

        if (serializer.isWriting())
            serializer.getOutput().i32(InventoryObjectType.getFlags(type), true);
        else
            type = InventoryObjectType.fromFlags(serializer.getInput().i32(true),
                serializer.getRevision());
        subType = serializer.i32(subType, true);
        toolType = ToolType.fromValue((byte) serializer.i32(toolType.getValue(),
            true));

        creator = serializer.struct(creator, NetworkPlayerID.class);

        allowEmit = serializer.bool(allowEmit);
        shareable = serializer.bool(shareable);
        copyright = serializer.bool(copyright);
        if (head > 0x334)
            flags = serializer.i8(flags);

        if (serializer.getRevision().has(Branch.LEERDAMMER, Revisions.LD_LAMS_KEYS) || head > 0x2ba)
        {
            titleKey = serializer.u32(titleKey);
            descriptionKey = serializer.u32(descriptionKey);
        }
        else
            translationTag = serializer.str(translationTag);

        userCreatedDetails = serializer.struct(userCreatedDetails,
            UserCreatedDetails.class);
        if (userCreatedDetails != null &&
            userCreatedDetails.name.isEmpty() &&
            userCreatedDetails.description.isEmpty())
            userCreatedDetails = null;

        creationHistory = serializer.struct(creationHistory, CreationHistory.class);

        icon = serializer.resource(icon, ResourceType.TEXTURE, true);
        photoData = serializer.reference(photoData, InventoryItemPhotoData.class);
        eyetoyData = serializer.reference(eyetoyData, EyetoyData.class);

        if (head > 0x358)
            serializer.u8(0);

        if (!serializer.isWriting())
            updateTranslations();
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.translationTag != null)
            size += (this.translationTag.length() * 2);
        if (this.userCreatedDetails != null)
            size += this.userCreatedDetails.getAllocatedSize();
        if (this.creationHistory != null)
            size += this.creationHistory.getAllocatedSize();
        if (this.photoData != null)
            size += this.photoData.getAllocatedSize();
        if (this.eyetoyData != null)
            size += this.eyetoyData.getAllocatedSize();
        return size;
    }

    public SHA1 generateHashCode(Revision revision)
    {
        // I wonder how slow this is...
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, (byte) 0);
        serializer.struct(this, InventoryItemDetails.class);
        return SHA1.fromBuffer(serializer.getBuffer());
    }

    private void updateTranslations()
    {
        if (this.translationTag != null && !this.translationTag.isEmpty())
        {
            this.titleKey =
                RTranslationTable.makeLamsKeyID(this.translationTag + "_NAME");
            this.descriptionKey =
                RTranslationTable.makeLamsKeyID(this.translationTag + "_DESC");
        }

        RTranslationTable LAMS = ResourceSystem.getLAMS();
        if (LAMS != null)
        {
            if (this.titleKey != 0)
                this.translatedTitle = LAMS.translate(this.titleKey);
            if (this.descriptionKey != 0)
                this.translatedDescription = LAMS.translate(this.descriptionKey);
        }
    }
}
