package cwlib.structs.level;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;

public class AdventureData implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int adventureFixedID;
    public int adventureItemPUIDCounter;
    public ArrayList<AdventureItem> adventureItems = new ArrayList<>();
    public ArrayList<ResourceDescriptor> questDescriptors = new ArrayList<>();
    public ArrayList<StartPoint> startPointList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public AdventureData serialize(Serializer serializer, Serializable structure) {
        AdventureData data = (structure == null) ? new AdventureData() : (AdventureData) structure;

        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0xaf)
            data.adventureFixedID = serializer.i32(data.adventureFixedID);

        if (subVersion > 0x93) {
            data.adventureItemPUIDCounter = serializer.i32(data.adventureItemPUIDCounter);
            data.adventureItems = serializer.arraylist(data.adventureItems, AdventureItem.class);
        }

        if (subVersion > 0xa6) {
            int numItems = serializer.i32(data.questDescriptors != null ? data.questDescriptors.size() : 0);
            if (serializer.isWriting()) {
                for (ResourceDescriptor descriptor : data.questDescriptors)
                    serializer.resource(descriptor, ResourceType.QUEST, true);
            } else {
                data.questDescriptors = new ArrayList<>(numItems);
                for (int i = 0; i < numItems; ++i)
                    data.questDescriptors.add(serializer.resource(null, ResourceType.QUEST, true));
            }
        }

        if (subVersion >= 0xd1)
            data.startPointList = serializer.arraylist(data.startPointList, StartPoint.class);

        return data;
    }

    @Override public int getAllocatedSize() {
        int size = AdventureData.BASE_ALLOCATION_SIZE;
        if (this.adventureItems != null)
            size += (this.adventureItems.size() * AdventureItem.BASE_ALLOCATION_SIZE);
        if (this.questDescriptors != null)
            size += (this.questDescriptors.size() * 0x24);
        if (this.startPointList != null)
            size += (this.startPointList.size() * SlotID.BASE_ALLOCATION_SIZE);
        return size;
    }
    
}
