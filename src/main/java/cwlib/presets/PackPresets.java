package cwlib.presets;

import cwlib.types.data.ResourceDescriptor;
import cwlib.enums.ContentsType;
import cwlib.enums.ResourceType;
import cwlib.enums.SlotType;
import cwlib.structs.slot.Pack;
import cwlib.structs.slot.SlotID;

public class PackPresets {
    public static Pack Group(String name, String description, String author, ResourceDescriptor icon) {
        Pack item = new Pack();
        item.contentsType = ContentsType.GROUP;
        item.mesh = new ResourceDescriptor(0x6a1a, ResourceType.MESH);
        
        item.slot.id = new SlotID(SlotType.DLC_PACK, 0x340);
        
        item.slot.name = name;
        item.slot.description = description;
        
        item.slot.icon = icon;
        item.slot.authorName = author;
        
        return item;
    }
    
    public static Pack Level(String name, String description, String author, ResourceDescriptor icon) {
        Pack item = new Pack();
        
        item.contentID = null;
        
        item.contentsType = ContentsType.LEVEL;
        item.mesh = new ResourceDescriptor(0x3e86, ResourceType.MESH);
        
        item.slot.id = new SlotID(SlotType.DLC_LEVEL, 0x66062);
        item.slot.group = new SlotID(SlotType.DLC_PACK, 0x340);
        
        item.slot.name = name;
        item.slot.description = description;
        
        item.slot.icon = icon;
        item.slot.authorName = author;
        
        return item;
    }
}
