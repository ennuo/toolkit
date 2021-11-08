package ennuo.craftworld.resources.structs.presets;

import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ContentsType;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.PackItem;
import ennuo.craftworld.resources.structs.SlotID;

public class PackPresets {
    public static PackItem Theme(String name, String description, String author, ResourceDescriptor icon) {
        PackItem item = new PackItem();
        item.contentsType = ContentsType.THEME;
        item.mesh = new ResourceDescriptor(0x6a1a, ResourceType.MESH);
        
        item.slot.slot = new SlotID(SlotType.DLC_PACK, 0x340);
        
        item.slot.title = name;
        item.slot.description = description;
        
        item.slot.icon = icon;
        item.slot.authorName = author;
        
        return item;
    }
    
    public static PackItem Level(String name, String description, String author, ResourceDescriptor icon) {
        PackItem item = new PackItem();
        
        item.contentID = null;
        
        item.contentsType = ContentsType.LEVEL;
        item.mesh = new ResourceDescriptor(0x3e86, ResourceType.MESH);
        
        item.slot.slot = new SlotID(SlotType.DLC_LEVEL, 0x66062);
        item.slot.primaryLinkGroup = new SlotID(SlotType.DLC_PACK, 0x340);
        
        item.slot.title = name;
        item.slot.description = description;
        
        item.slot.icon = icon;
        item.slot.authorName = author;
        
        return item;
    }
}
