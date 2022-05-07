package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.resources.enums.InventoryObjectSubType;
import ennuo.craftworld.resources.enums.InventoryObjectType;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.enums.ToolType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.resources.structs.SceNpId;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.toolkit.utilities.Globals;
import java.util.Date;
import java.util.EnumSet;

public class InventoryDetails implements Serializable {
    public static int MAX_SIZE = 0x800;
    
    public ResourceDescriptor resource;
    public int inventoryFlags;
    public long tempGUID;
    
    public String translationTag = "";
    public String categoryTag = "";
    public String locationTag = "";
    
    public long dateAdded = new Date().getTime() / 1000;
    public SlotID levelUnlockSlotID = new SlotID();
    public long highlightSound;
    public long colour;
    
    public EnumSet<InventoryObjectType> type = EnumSet.noneOf(InventoryObjectType.class);
    public int subType = InventoryObjectSubType.NONE;
    
    public long titleKey, descriptionKey;
    
    public UserCreatedDetails userCreatedDetails;
    
    public CreationHistory creationHistory;
    public ResourceDescriptor icon;
    
    public PhotoData photoData;
    public EyetoyData eyetoyData;
    
    public short locationIndex = -1, categoryIndex = -1;
    public short primaryIndex;
    public int lastUsed;
    public int numUses;
    public int fluffCost;
    
    public boolean allowEmit;
    public boolean shareable;
    public boolean copyright;
    
    public SceNpId creator;
    
    public ToolType toolType = ToolType.NONE;
    public byte flags;
    
    public boolean makeSizeProportional = true;
    
    public long location;
    public long category;
    
    /**
     * Sometimes this is set
     */
    private int pad;
    
    public String translatedTitle = "";
    public String translatedDescription;
    public String translatedLocation = "";
    public String translatedCategory = "";

    public InventoryDetails serialize(Serializer serializer, Serializable structure) {
        InventoryDetails details = 
                (structure == null) ? new InventoryDetails() : (InventoryDetails) structure;
        
        int head = serializer.revision.head;
        
        if (serializer.isWriting && details.highlightSound != 0)
            serializer.dependencies.add(new ResourceDescriptor(details.highlightSound, ResourceType.FILENAME));
        
        if (serializer.revision.head > 0x37c) {
            details.dateAdded = serializer.i64d(details.dateAdded);
            details.levelUnlockSlotID = serializer.struct(details.levelUnlockSlotID, SlotID.class);
            details.highlightSound = serializer.u32(details.highlightSound);
            details.colour = serializer.u32(details.colour);
            
            
            if (serializer.isWriting)
                serializer.output.i32(InventoryObjectType.getFlags(details.type));
            else
                details.type = InventoryObjectType.fromFlags(serializer.input.i32(), serializer.revision);
            
            details.subType = serializer.i32(details.subType);
            
            details.titleKey = serializer.u32(details.titleKey);
            details.descriptionKey = serializer.u32(details.descriptionKey);
            
            details.creationHistory = serializer.reference(details.creationHistory, CreationHistory.class);
            details.icon = serializer.resource(details.icon, ResourceType.TEXTURE, true);
            details.userCreatedDetails = serializer.reference(details.userCreatedDetails, UserCreatedDetails.class);
            details.photoData = serializer.reference(details.photoData, PhotoData.class);
            details.eyetoyData = serializer.reference(details.eyetoyData, EyetoyData.class);
            
            details.locationIndex = serializer.i16(details.locationIndex);
            details.categoryIndex = serializer.i16(details.categoryIndex);
            details.primaryIndex = serializer.i16(details.primaryIndex);
            
            details.creator = serializer.reference(details.creator, SceNpId.class);
            
            details.toolType = ToolType.getValue(serializer.i8(details.toolType.value));
            details.flags = serializer.i8(details.flags);
            
            if (serializer.revision.isAfterVitaRevision(0x7c))
                details.makeSizeProportional = serializer.bool(details.makeSizeProportional);
            
            if (!serializer.isWriting)
                details.updateTranslations();
            
            return details;
        }
        
        if (head < 0x233) {
            if (head < 0x174) {
                serializer.str16(null); // nameTranslationTag
                serializer.str16(null); // descTranslationTag
            } else {
                details.translationTag = serializer.str8(details.translationTag);
            }

            details.locationIndex = (short) serializer.i32f(details.locationIndex);
            details.categoryIndex = (short) serializer.i32f(details.categoryIndex);
            if (head > 0x194)
                details.primaryIndex = (short) serializer.i32f(details.primaryIndex);
            
            details.pad = serializer.i32f(details.pad); // Pad
            
            if (serializer.isWriting)
                serializer.output.i32f(InventoryObjectType.getFlags(details.type));
            else
                details.type = InventoryObjectType.fromFlags(serializer.input.i32f(), serializer.revision);
            details.subType = serializer.i32f(details.subType);
            
            if (head > 0x196)
                details.toolType = ToolType.getValue((byte) serializer.i32f(details.toolType.value));
            details.icon = serializer.resource(details.icon, ResourceType.TEXTURE, true);
            if (head > 0x1c0) {
                details.numUses = serializer.i32f(details.numUses);
                details.lastUsed = serializer.i32f(details.lastUsed);
            }

            if (head > 0x14e)
                details.highlightSound = serializer.u32f(details.highlightSound);
            else
                serializer.str8(null); // Path to highlight sound?

            if (head > 0x156)
                details.colour = serializer.u32f(details.colour);

            if (head > 0x161) {
                details.eyetoyData = serializer.reference(details.eyetoyData, EyetoyData.class);
            }

            if (head > 0x181)
                details.photoData = serializer.reference(details.photoData, PhotoData.class);

            if (head > 0x176) {
                details.levelUnlockSlotID.type =
                    SlotType.getValue(
                        serializer.i32f(details.levelUnlockSlotID.type.value)
                    );

                details.levelUnlockSlotID.ID = 
                    serializer.u32f(details.levelUnlockSlotID.ID);
            }

            if (head > 0x181) {
                details.copyright = serializer.bool(details.copyright);
                details.creator = serializer.struct(details.creator, SceNpId.class);
            }

            if (head > 0x1aa) {
                details.userCreatedDetails = serializer.struct(details.userCreatedDetails, UserCreatedDetails.class);
                if (details.userCreatedDetails != null && 
                        details.userCreatedDetails.title.isEmpty() && 
                        details.userCreatedDetails.description.isEmpty())
                    details.userCreatedDetails = null;
            }

            if (head > 0x1b0)
                details.creationHistory = serializer.struct(details.creationHistory, CreationHistory.class);
            
            if (head > 0x204)
                details.allowEmit = serializer.bool(details.allowEmit);

            if (head > 0x221)
                details.dateAdded = serializer.i64f(details.dateAdded);
            
            if (head > 0x222)
                details.shareable = serializer.bool(details.shareable);
            
            if (!serializer.isWriting)
                details.updateTranslations();

            return details;
        }
        
        details.highlightSound = serializer.u32f(details.highlightSound);

        // NOTE(Aidan): In these older versions of the inventory details,
        // 32 bit values are enforced while still using encoded values elsewhere,
        // so for some structures like SlotID, we need to force it manually.

        details.levelUnlockSlotID.type =
            SlotType.getValue(
                serializer.i32f(details.levelUnlockSlotID.type.value)
            );

        details.levelUnlockSlotID.ID = 
            serializer.u32f(details.levelUnlockSlotID.ID);

        details.locationIndex = (short) serializer.i32f(details.locationIndex);
        details.categoryIndex = (short) serializer.i32f(details.categoryIndex);
        details.primaryIndex = (short) serializer.i32f(details.primaryIndex);

        details.lastUsed = serializer.i32f(details.lastUsed);
        details.numUses = serializer.i32f(details.numUses);
        if (head > 0x234)
            details.pad = serializer.i32f(details.pad);


        details.dateAdded = serializer.i64f(details.dateAdded);
        
        details.fluffCost = serializer.i32f(details.fluffCost);
        
        details.colour = serializer.u32f(details.colour);
        
        if (serializer.isWriting)
            serializer.output.i32f(InventoryObjectType.getFlags(details.type));
        else
            details.type = InventoryObjectType.fromFlags(serializer.input.i32f(), serializer.revision);
        details.subType = serializer.i32f(details.subType);
        details.toolType = ToolType.getValue((byte) serializer.i32f(details.toolType.value));

        details.creator = serializer.struct(details.creator, SceNpId.class);

        details.allowEmit = serializer.bool(details.allowEmit);
        details.shareable = serializer.bool(details.shareable);
        details.copyright = serializer.bool(details.copyright);
        if (head > 0x334) 
            details.flags = serializer.i8(details.flags);

        if (serializer.revision.isAfterLeerdammerRevision(7) || head > 0x2ba) {
            details.titleKey = serializer.u32(details.titleKey);
            details.descriptionKey = serializer.u32(details.descriptionKey);
        } else 
            details.translationTag = serializer.str8(details.translationTag);

        details.userCreatedDetails = serializer.struct(details.userCreatedDetails, UserCreatedDetails.class);
        if (details.userCreatedDetails != null && 
                details.userCreatedDetails.title.isEmpty() && 
                details.userCreatedDetails.description.isEmpty())
            details.userCreatedDetails = null;
        
        details.creationHistory = serializer.struct(details.creationHistory, CreationHistory.class);

        details.icon = serializer.resource(details.icon, ResourceType.TEXTURE, true);
        details.photoData = serializer.reference(details.photoData, PhotoData.class);
        details.eyetoyData = serializer.reference(details.eyetoyData, EyetoyData.class);
        
        if (!serializer.isWriting)
            details.updateTranslations();
        
        return details;
    }
    
    private void updateTranslations() {
        if (this.translationTag != null && !this.translationTag.isEmpty()) {
            this.titleKey = 
                    TranslationTable.makeLamsKeyID(this.translationTag + "_NAME");
            this.descriptionKey = 
                    TranslationTable.makeLamsKeyID(this.translationTag + "_DESC");
        }
        
        if (Globals.LAMS != null) {
            if (this.titleKey != 0)
                this.translatedTitle = Globals.LAMS.translate(this.titleKey);
            if (this.descriptionKey != 0)
                this.translatedDescription = Globals.LAMS.translate(this.descriptionKey);
        }
    }
}
