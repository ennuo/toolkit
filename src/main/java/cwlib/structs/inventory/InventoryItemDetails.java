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

public class InventoryItemDetails implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0xB0;
    
    @GsonRevision(branch=0, max=0x2ba)
    @GsonRevision(branch=0x4c44, max=0x7)
    public String translationTag = "";
    
    @GsonRevision(min=0x222)
    public long dateAdded = new Date().getTime() / 1000;

    @GsonRevision(min=0x177)
    public SlotID levelUnlockSlotID = new SlotID();

    @GsonRevision(min=0x14f)
    public GUID highlightSound;

    @GsonRevision(min=0x157)
    public int colour = -1;
    
    public EnumSet<InventoryObjectType> type = EnumSet.noneOf(InventoryObjectType.class);
    public int subType = InventoryObjectSubType.NONE;
    
    @GsonRevision(min=0x2bb)
    @GsonRevision(branch=0x4c44, min=0x8)
    public long titleKey, descriptionKey;
    
    @GsonRevision(min=0x1ab)
    public UserCreatedDetails userCreatedDetails;
    
    @GsonRevision(min=0x1b1)
    public CreationHistory creationHistory;

    public ResourceDescriptor icon = new ResourceDescriptor(15525, ResourceType.TEXTURE);
    
    @GsonRevision(min=0x17b)
    public InventoryItemPhotoData photoData;
    @GsonRevision(min=0x162)
    public EyetoyData eyetoyData;
    
    public short locationIndex = -1, categoryIndex = -1;
    @GsonRevision(min=0x195)
    public short primaryIndex;

    @GsonRevision(min=0x1c1,max=0x37c)
    public int lastUsed, numUses;

    @GsonRevision(min=0x233,max=0x37c)
    public int fluffCost;
    
    @GsonRevision(min=0x205,max=0x37c)
    public boolean allowEmit;
    @GsonRevision(min=0x223,max=0x37c)
    public boolean shareable;
    @GsonRevision(min=0x182,max=0x37c)
    public boolean copyright;
    
    @GsonRevision(min=0x182)
    public NetworkPlayerID creator;
    
    @GsonRevision(min=0x197)
    public ToolType toolType = ToolType.NONE;
    @GsonRevision(min=0x335)
    public byte flags;
    
    @GsonRevision(branch=0x4431,min=125)
    public boolean makeSizeProportional = true;
    
    @GsonRevision(min=0x2bb)
    @GsonRevision(branch=0x4c44, min=0x8)
    public long location, category;

    @GsonRevision(branch=0, max=0x2ba)
    @GsonRevision(branch=0x4c44, max=0x7)
    public String categoryTag = "", locationTag = "";
    
    public transient String translatedTitle = "";
    public transient String translatedDescription;
    public transient String translatedLocation = "";
    public transient String translatedCategory = "";

    public InventoryItemDetails() {};
    public InventoryItemDetails(PMetadata metadata) {
        // if (metadata.nameTranslationTag != null && metadata.nameTranslationTag.endsWith("_NAME"))
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

    @SuppressWarnings("unchecked")
    @Override public InventoryItemDetails serialize(Serializer serializer, Serializable structure) {
        InventoryItemDetails details = 
                (structure == null) ? new InventoryItemDetails() : (InventoryItemDetails) structure;
        
        int head = serializer.getRevision().getVersion();
        
        if (serializer.isWriting() && details.highlightSound != null)
            serializer.addDependency(new ResourceDescriptor(details.highlightSound, ResourceType.FILENAME));
        
        if (serializer.getRevision().getVersion() > 0x37c) {
            details.dateAdded = serializer.i64(details.dateAdded);
            details.levelUnlockSlotID = serializer.struct(details.levelUnlockSlotID, SlotID.class);
            details.highlightSound = serializer.guid(details.highlightSound);
            details.colour = serializer.i32(details.colour);
            
            
            if (serializer.isWriting())
                serializer.getOutput().i32(InventoryObjectType.getFlags(details.type));
            else
                details.type = InventoryObjectType.fromFlags(serializer.getInput().i32(), serializer.getRevision());
            
            details.subType = serializer.i32(details.subType);
            
            details.titleKey = serializer.u32(details.titleKey);
            details.descriptionKey = serializer.u32(details.descriptionKey);
            
            details.creationHistory = serializer.reference(details.creationHistory, CreationHistory.class);
            details.icon = serializer.resource(details.icon, ResourceType.TEXTURE, true);
            details.userCreatedDetails = serializer.reference(details.userCreatedDetails, UserCreatedDetails.class);
            details.photoData = serializer.reference(details.photoData, InventoryItemPhotoData.class);
            details.eyetoyData = serializer.reference(details.eyetoyData, EyetoyData.class);
            
            details.locationIndex = serializer.i16(details.locationIndex);
            details.categoryIndex = serializer.i16(details.categoryIndex);
            details.primaryIndex = serializer.i16(details.primaryIndex);
            
            details.creator = serializer.reference(details.creator, NetworkPlayerID.class);
            
            details.toolType = ToolType.fromValue(serializer.i8(details.toolType.getValue()));
            details.flags = serializer.i8(details.flags);

            
            if (serializer.getRevision().has(Branch.DOUBLE11, Revisions.D1_DETAILS_PROPORTIONAL))
                details.makeSizeProportional = serializer.bool(details.makeSizeProportional);
            
            if (!serializer.isWriting())
                details.updateTranslations();
            
            return details;
        }
        
        if (head < 0x233) {
            if (head < 0x174) {
                serializer.wstr(null); // nameTranslationTag
                serializer.wstr(null); // descTranslationTag
            } else {
                details.translationTag = serializer.str(details.translationTag);
            }

            details.locationIndex = (short) serializer.i32(details.locationIndex, true);
            details.categoryIndex = (short) serializer.i32(details.categoryIndex, true);
            if (head > 0x194)
                details.primaryIndex = (short) serializer.i32(details.primaryIndex, true);
            
            serializer.i32(0, true); // Pad
            
            if (serializer.isWriting())
                serializer.getOutput().i32(InventoryObjectType.getFlags(details.type), true);
            else
                details.type = InventoryObjectType.fromFlags(serializer.getInput().i32(true), serializer.getRevision());
            details.subType = serializer.i32(details.subType, true);
            
            if (head > 0x196)
                details.toolType = ToolType.fromValue((byte) serializer.i32(details.toolType.getValue(), true));
            details.icon = serializer.resource(details.icon, ResourceType.TEXTURE, true);
            if (head > 0x1c0) {
                details.numUses = serializer.i32(details.numUses, true);
                details.lastUsed = serializer.i32(details.lastUsed, true);
            }

            if (head > 0x14e)
                details.highlightSound = serializer.guid(details.highlightSound, true);
            else
                serializer.str(null); // Path to highlight sound?

            if (head > 0x156)
                details.colour = serializer.i32(details.colour, true);

            if (head > 0x161) {
                details.eyetoyData = serializer.reference(details.eyetoyData, EyetoyData.class);
            }

            // 0x17a < revision && revision < 0x182
            // 0x181 < revision ???
            if (head > 0x17a)
                details.photoData = serializer.reference(details.photoData, InventoryItemPhotoData.class);

            if (head > 0x176) {
                details.levelUnlockSlotID.slotType =
                    SlotType.fromValue(
                        serializer.i32(details.levelUnlockSlotID.slotType.getValue())
                    );

                details.levelUnlockSlotID.slotNumber = 
                    serializer.u32(details.levelUnlockSlotID.slotNumber, true);
            }

            if (head > 0x181) {
                details.copyright = serializer.bool(details.copyright);
                details.creator = serializer.struct(details.creator, NetworkPlayerID.class);
            }

            if (head > 0x1aa) {
                details.userCreatedDetails = serializer.struct(details.userCreatedDetails, UserCreatedDetails.class);
                if (details.userCreatedDetails != null && 
                        details.userCreatedDetails.name.isEmpty() && 
                        details.userCreatedDetails.description.isEmpty())
                    details.userCreatedDetails = null;
            }

            if (head > 0x1b0)
                details.creationHistory = serializer.struct(details.creationHistory, CreationHistory.class);
            
            if (head > 0x204)
                details.allowEmit = serializer.bool(details.allowEmit);

            if (head > 0x221)
                details.dateAdded = serializer.i64(details.dateAdded, true);
            
            if (head > 0x222)
                details.shareable = serializer.bool(details.shareable);
            
            if (!serializer.isWriting())
                details.updateTranslations();

            return details;
        }
        
        details.highlightSound = serializer.guid(details.highlightSound, true);

        // In these older versions of the inventory details,
        // 32 bit values are enforced while still using encoded values elsewhere,
        // so for some structures like SlotID, we need to force it manually.

        details.levelUnlockSlotID.slotType =
            SlotType.fromValue(
                serializer.i32(details.levelUnlockSlotID.slotType.getValue(), true)
            );

        details.levelUnlockSlotID.slotNumber = 
            serializer.u32(details.levelUnlockSlotID.slotNumber, true);

        details.locationIndex = (short) serializer.i32(details.locationIndex, true);
        details.categoryIndex = (short) serializer.i32(details.categoryIndex, true);
        details.primaryIndex = (short) serializer.i32(details.primaryIndex, true);

        details.lastUsed = serializer.i32(details.lastUsed, true);
        details.numUses = serializer.i32(details.numUses, true);
        if (head > 0x234)
            serializer.i32(0, true); // Pad


        details.dateAdded = serializer.i64(details.dateAdded, true);
        
        details.fluffCost = serializer.i32(details.fluffCost, true);
        
        details.colour = serializer.i32(details.colour, true);
        
        if (serializer.isWriting())
            serializer.getOutput().i32(InventoryObjectType.getFlags(details.type), true);
        else
            details.type = InventoryObjectType.fromFlags(serializer.getInput().i32(true), serializer.getRevision());
        details.subType = serializer.i32(details.subType, true);
        details.toolType = ToolType.fromValue((byte) serializer.i32(details.toolType.getValue(), true));

        details.creator = serializer.struct(details.creator, NetworkPlayerID.class);

        details.allowEmit = serializer.bool(details.allowEmit);
        details.shareable = serializer.bool(details.shareable);
        details.copyright = serializer.bool(details.copyright);
        if (head > 0x334) 
            details.flags = serializer.i8(details.flags);

        if (serializer.getRevision().has(Branch.LEERDAMMER, Revisions.LD_LAMS_KEYS) || head > 0x2ba) {
            details.titleKey = serializer.u32(details.titleKey);
            details.descriptionKey = serializer.u32(details.descriptionKey);
        } else 
            details.translationTag = serializer.str(details.translationTag);

        details.userCreatedDetails = serializer.struct(details.userCreatedDetails, UserCreatedDetails.class);
        if (details.userCreatedDetails != null && 
                details.userCreatedDetails.name.isEmpty() && 
                details.userCreatedDetails.description.isEmpty())
            details.userCreatedDetails = null;
        
        details.creationHistory = serializer.struct(details.creationHistory, CreationHistory.class);

        details.icon = serializer.resource(details.icon, ResourceType.TEXTURE, true);
        details.photoData = serializer.reference(details.photoData, InventoryItemPhotoData.class);
        details.eyetoyData = serializer.reference(details.eyetoyData, EyetoyData.class);

        if (head > 0x358)
            serializer.u8(0);
        
        if (!serializer.isWriting())
            details.updateTranslations();
        
        return details;
    }

    @Override public int getAllocatedSize() {
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
    
    public SHA1 generateHashCode(Revision revision) {
        // I wonder how slow this is...
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, (byte) 0);
        serializer.struct(this, InventoryItemDetails.class);
        return SHA1.fromBuffer(serializer.getBuffer());
    }
    
    private void updateTranslations() {
        if (this.translationTag != null && !this.translationTag.isEmpty()) {
            this.titleKey = 
                    RTranslationTable.makeLamsKeyID(this.translationTag + "_NAME");
            this.descriptionKey = 
                    RTranslationTable.makeLamsKeyID(this.translationTag + "_DESC");
        }
        
        RTranslationTable LAMS = ResourceSystem.getLAMS();
        if (LAMS != null) {
            if (this.titleKey != 0)
                this.translatedTitle = LAMS.translate(this.titleKey);
            if (this.descriptionKey != 0)
                this.translatedDescription = LAMS.translate(this.descriptionKey);
        }
    }
}
