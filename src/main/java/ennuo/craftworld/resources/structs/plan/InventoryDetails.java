package ennuo.craftworld.resources.structs.plan;

import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ItemSubType;
import ennuo.craftworld.resources.enums.ItemType;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.enums.ToolType;
import ennuo.craftworld.resources.structs.SceNpId;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.toolkit.utilities.Globals;
import java.util.Date;

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
    
    public ItemType type = ItemType.CREATED_OBJECTS;
    public ItemSubType subType = ItemSubType.NONE;
    
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
    
    public long location;
    public long category;
    
    public String translatedTitle = "";
    public String translatedDescription;
    public String translatedLocation = "";
    public String translatedCategory = "";

    public InventoryDetails serialize(Serializer serializer, Serializable structure) {
        InventoryDetails details = 
                (structure == null) ? new InventoryDetails() : (InventoryDetails) structure;
        
        if (serializer.revision > 0x377) {
            details.dateAdded = serializer.u32d(details.dateAdded);
            details.levelUnlockSlotID = serializer.struct(details.levelUnlockSlotID, SlotID.class);
            details.highlightSound = serializer.u32(details.highlightSound);
            details.colour = serializer.u32(details.colour);
            
            details.type = ItemType.getValue(serializer.u32(details.type.value), serializer.revision);
            details.subType = ItemSubType.getValue(serializer.u32(details.subType.value), details.type);
            
            details.titleKey = serializer.u32(details.titleKey);
            details.descriptionKey = serializer.u32(details.descriptionKey);
            
            if (Globals.LAMS != null && !serializer.isWriting) {
                if (details.titleKey != 0)
                    details.translatedTitle = Globals.LAMS.translate(details.titleKey);
                if (details.descriptionKey != 0)
                    details.translatedDescription = Globals.LAMS.translate(details.descriptionKey);
            }
            
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
            
            return details;
        }
        
        if (serializer.revision > 0x233) {
            details.highlightSound = serializer.u32f(details.highlightSound);
            
            // NOTE(Abz): In these older versions of the inventory details,
            // 32 bit values are enforced while still using encoded values elsewhere,
            // so for some structures like SlotID, we need to force it manually.

            if (details.levelUnlockSlotID == null)
                details.levelUnlockSlotID = new SlotID();

            details.levelUnlockSlotID.type = SlotType.getValue(serializer.i32f(details.levelUnlockSlotID.type.value));
            details.levelUnlockSlotID.ID = serializer.u32f(details.levelUnlockSlotID.ID);
        } else details.translationTag = serializer.str8(details.translationTag);
        
        details.locationIndex = (short) serializer.i32f(details.locationIndex);
        details.categoryIndex = (short) serializer.i32f(details.locationIndex);
        details.primaryIndex = (short) serializer.i32f(details.primaryIndex);
        
        if (serializer.revision > 0x233) {
            details.lastUsed = serializer.i32f(details.lastUsed);
            details.numUses = serializer.i32f(details.numUses);
            serializer.i32f(0); // PAD
        } else {
            serializer.i32f(0); // PAD
            details.type = ItemType.getValue(serializer.u32f(details.type.value), serializer.revision);
            details.subType = ItemSubType.getValue(serializer.u32f(details.subType.value), details.type);
            if (serializer.revision > 0x196) {
                details.toolType = ToolType.getValue((byte) (serializer.i32f(details.toolType.value) & 0xFF));
                details.icon = serializer.resource(details.icon, ResourceType.TEXTURE, true);
            }
        }
        
        if (serializer.revision > 0x233) {
            // Fake long!
            serializer.u32f(0);
            details.dateAdded = serializer.u32f(details.dateAdded);
            
            details.fluffCost = serializer.i32f(details.fluffCost);
        } else if (serializer.revision > 0x1c0) {
            details.numUses = serializer.i32f(details.numUses);
            details.lastUsed = serializer.i32f(details.lastUsed);
        }
        
        if (serializer.revision > 0x14e) {
            if (serializer.revision > 0x233) {
                details.colour = serializer.u32f(details.colour);
                details.type = ItemType.getValue(serializer.u32f(details.type.value), serializer.revision);
                details.subType = ItemSubType.getValue(serializer.u32f(details.subType.value), details.type);
                details.toolType = ToolType.getValue((byte) (serializer.i32f(details.toolType.value) & 0xFF));
            }
            else {
                details.highlightSound = serializer.u32f(details.highlightSound);
                if (serializer.revision > 0x156) {
                    details.colour = serializer.u32f(details.colour);
                    details.eyetoyData = serializer.reference(details.eyetoyData, EyetoyData.class);
                }
                if (serializer.revision > 0x176) {
                    if (serializer.revision > 0x181)
                        details.photoData = serializer.reference(details.photoData, PhotoData.class);
                    if (details.levelUnlockSlotID == null)
                        details.levelUnlockSlotID = new SlotID();

                    details.levelUnlockSlotID.type = SlotType.getValue(serializer.i32f(details.levelUnlockSlotID.type.value));
                    details.levelUnlockSlotID.ID = serializer.u32f(details.levelUnlockSlotID.ID);
                }
                if (serializer.revision > 0x181)
                    details.copyright = serializer.bool(details.copyright);
            }
        }
        
        if (serializer.revision > 0x181)
            details.creator = serializer.struct(details.creator, SceNpId.class);
        
        if (serializer.revision > 0x233) {
            details.allowEmit = serializer.bool(details.allowEmit);
            details.shareable = serializer.bool(details.shareable);
            details.copyright = serializer.bool(details.copyright);
            if (serializer.revision >= 0x336) 
                serializer.pad(1);
        }
        
        if ((serializer.revision == 0x272 && serializer.branchDescription != 0) || serializer.revision > 0x2ba) {
            details.titleKey = serializer.u32(details.titleKey);
            details.descriptionKey = serializer.u32(details.descriptionKey);
        } else if (serializer.revision > 0x233)
            details.translationTag = serializer.str8(details.translationTag);
        
        if (!serializer.isWriting && !details.translationTag.isEmpty()) {
            details.titleKey = 
                    TranslationTable.makeLamsKeyID(details.translationTag + "_NAME");
            details.descriptionKey = 
                    TranslationTable.makeLamsKeyID(details.translationTag + "_DESC");
        }
        
        if (Globals.LAMS != null && !serializer.isWriting) {
            if (details.titleKey != 0)
                details.translatedTitle = Globals.LAMS.translate(details.titleKey);
            if (details.descriptionKey != 0)
                details.translatedDescription = Globals.LAMS.translate(details.descriptionKey);
        }
        
        if (serializer.revision > 0x1aa) {
            details.userCreatedDetails = serializer.struct(details.userCreatedDetails, UserCreatedDetails.class);
            if (details.userCreatedDetails != null && 
                    details.userCreatedDetails.title.isEmpty() && 
                    details.userCreatedDetails.description.isEmpty())
                details.userCreatedDetails = null;
            if (serializer.revision > 0x1b0) {
                if (!serializer.isWriting) {
                    details.creationHistory = new CreationHistory();
                    details.creationHistory.creators = new String[serializer.input.i32()];
                } else serializer.output.i32(details.creationHistory.creators.length);
                for (int i = 0; i < details.creationHistory.creators.length; ++i)
                    details.creationHistory.creators[i] = 
                            serializer.str16(details.creationHistory.creators[i]);
            }
        }
        
        if (serializer.revision > 0x233) {
            details.icon = serializer.resource(details.icon, ResourceType.TEXTURE, true);

            details.photoData = serializer.reference(details.photoData, PhotoData.class);
            details.eyetoyData = serializer.reference(details.eyetoyData, EyetoyData.class);
        } else if (serializer.revision > 0x204) {
            details.allowEmit = serializer.bool(details.allowEmit);
            if (serializer.revision > 0x221) {
                // Fake long!
                serializer.u32f(0);
                details.dateAdded = serializer.u32f(details.dateAdded);
            }
        }
        
        return details;
    }
}
