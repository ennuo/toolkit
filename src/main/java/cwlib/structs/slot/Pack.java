package cwlib.structs.slot;

import cwlib.structs.slot.Slot;
import cwlib.types.data.ResourceReference;
import cwlib.enums.ContentsType;
import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import java.util.Date;

public class Pack implements Serializable {
    public static int MAX_SIZE = 0x125 + Slot.MAX_SIZE;
    
    public ContentsType contentsType = ContentsType.LEVEL;
    public ResourceReference mesh = new ResourceReference(16006, ResourceType.MESH);
    public Slot slot = new Slot();
    public String contentID = "";
    public long timestamp = new Date().getTime() / 1000;
    public boolean crossBuyCompatible = false;

    @SuppressWarnings("unchecked")
    @Override public Pack serialize(Serializer serializer, Serializable structure) {
        Pack item = (structure == null) ? new Pack() : (Pack) structure;
        
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
