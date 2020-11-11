package ennuo.craftworld.things;

import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.enums.ItemSubType;
import ennuo.craftworld.resources.enums.ItemType;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.enums.ToolType;
import ennuo.craftworld.resources.structs.Copyright;
import ennuo.craftworld.resources.structs.EyetoyData;
import ennuo.craftworld.resources.structs.PhotoData;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.resources.structs.UserCreatedDetails;

public class InventoryMetadata {
    public static int MAX_SIZE = 0x800;
    
    public ResourcePtr resource = new ResourcePtr(null, RType.PLAN);
    
    public String translationKey;
    
    public String legacyCategoryKey;
    public String legacyLocationKey;
    
    public long dateAdded = 0;
    public SlotID levelUnlockSlotID = new SlotID();
    public long highlightSound = 0;
    public long colour = 0;
    
    public ItemType type = ItemType.CREATED_OBJECTS;
    public ItemSubType subType = ItemSubType.NONE;
    
    public long titleKey, descriptionKey = 0;
    
    public UserCreatedDetails userCreatedDetails = new UserCreatedDetails();
    
    public String[] creationHistory = {};
    public ResourcePtr icon = new ResourcePtr(null, RType.TEXTURE);
    
    public PhotoData photoData = null;
    public EyetoyData eyetoyData = null;
    
    public short locationIndex = -1, categoryIndex = -1;
    public short primaryIndex = 0; 
    
    public Copyright creator = new Copyright("", "");
    
    public ToolType toolType = ToolType.NONE;
    public byte flags = 0;
    
    public long location = 0;
    public long category = 0;
    
    public String translatedLocation = "";
    public String translatedCategory = "";
    
    public int minRevision = -1;
    public int maxRevision = -1;
}
