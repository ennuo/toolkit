package ennuo.craftworld.resources.structs.presets;

import ennuo.craftworld.types.data.ResourcePtr;
import ennuo.craftworld.resources.enums.ContentsType;
import ennuo.craftworld.resources.enums.RType;
import ennuo.craftworld.resources.enums.SlotType;
import ennuo.craftworld.resources.structs.PackItem;
import ennuo.craftworld.resources.structs.SlotID;

public class PackPresets {
    public static PackItem Theme(String name, String description, String author, ResourcePtr icon) {
        PackItem item = new PackItem();
        item.contentsType = ContentsType.THEME;
        item.mesh = new ResourcePtr(0x6a1a, RType.MESH);
        
        item.slot.slot = new SlotID(SlotType.DLC_PACK, 0x340);
        
        item.slot.title = name;
        item.slot.description = description;
        
        item.slot.icon = icon;
        item.slot.authorName = author;
        
        return item;
    }
    
    public static PackItem Level(String name, String description, String author, ResourcePtr icon) {
        PackItem item = new PackItem();
        
        item.contentID = null;
        
        item.contentsType = ContentsType.LEVEL;
        item.mesh = new ResourcePtr(0x3e86, RType.MESH);
        
        item.slot.slot = new SlotID(SlotType.DLC_LEVEL, 0x66062);
        item.slot.primaryLinkGroup = new SlotID(SlotType.DLC_PACK, 0x340);
        
        item.slot.title = name;
        item.slot.description = description;
        
        item.slot.icon = icon;
        item.slot.authorName = author;
        
        return item;
    }
}
