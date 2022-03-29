package ennuo.craftworld.resources.structs;

import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ContentsType;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import java.util.Date;

public class PackItem implements Serializable {
    public static int MAX_SIZE = 0x125 + Slot.MAX_SIZE;
    
    public ContentsType contentsType = ContentsType.LEVEL;
    public ResourceDescriptor mesh = new ResourceDescriptor(16006, ResourceType.MESH);
    public Slot slot = new Slot();
    public String contentID = "";
    public long timestamp = new Date().getTime() / 1000;
    public boolean crossBuyCompatible = false;

    @SuppressWarnings("unchecked")
    @Override public PackItem serialize(Serializer serializer, Serializable structure) {
        PackItem item = (structure == null) ? new PackItem() : (PackItem) structure;
        
        item.contentsType = ContentsType.getValue(serializer.i32(item.contentsType.value));
        item.mesh = serializer.resource(item.mesh, ResourceType.MESH, true);
        item.slot = serializer.struct(item.slot, Slot.class);
        item.contentID = serializer.str8(item.contentID);
        item.timestamp = serializer.i64d(this.timestamp);
        if (serializer.revision.isVita())
            item.crossBuyCompatible = serializer.bool(item.crossBuyCompatible);

        return item;
    }
}
