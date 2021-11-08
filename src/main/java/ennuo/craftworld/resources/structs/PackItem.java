package ennuo.craftworld.resources.structs;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.enums.ContentsType;
import ennuo.craftworld.resources.enums.ResourceType;
import java.util.Date;

public class PackItem {
    
    public static int MAX_SIZE = 0x125 + Slot.MAX_SIZE;
    
    public ContentsType contentsType = ContentsType.LEVEL;
    public ResourceDescriptor mesh = new ResourceDescriptor(16006, ResourceType.MESH);
    public Slot slot = new Slot();
    public String contentID = "";
    public long timestamp = new Date().getTime() * 2 / 1000;
    public boolean crossBuyCompatible = false;
    
    public PackItem() {}
    public PackItem(Data data) {
        contentsType = ContentsType.getValue(data.i32());
        mesh = data.resource(ResourceType.MESH, true);
        slot = new Slot(data, true, false);
        contentID = data.str8();
        timestamp = data.u32();
        if (data.revision == 0x3e2)
            crossBuyCompatible = data.bool();
    }
    
    public void serialize(Output output) {
        output.u8(contentsType.value);
        output.resource(mesh, true);
        slot.serialize(output, true, false);
        output.str8(contentID);
        output.u32(timestamp);
        if (output.revision == 0x3e2)
            output.bool(crossBuyCompatible);
    }
}
