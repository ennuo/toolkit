package cwlib.structs.level;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.structs.slot.SlotID;
import cwlib.types.data.ResourceDescriptor;

import java.util.ArrayList;

public class AdventureData implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public int adventureFixedID;
    public int adventureItemPUIDCounter;
    public ArrayList<AdventureItem> adventureItems = new ArrayList<>();
    public ArrayList<ResourceDescriptor> questDescriptors = new ArrayList<>();
    public ArrayList<StartPoint> startPointList = new ArrayList<>();

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0xaf)
            adventureFixedID = serializer.i32(adventureFixedID);

        if (subVersion > 0x93)
        {
            adventureItemPUIDCounter = serializer.i32(adventureItemPUIDCounter);
            adventureItems = serializer.arraylist(adventureItems, AdventureItem.class);
        }

        if (subVersion > 0xa6)
        {
            int numItems = serializer.i32(questDescriptors != null ?
                questDescriptors.size() : 0);
            if (serializer.isWriting())
            {
                for (ResourceDescriptor descriptor : questDescriptors)
                    serializer.resource(descriptor, ResourceType.QUEST, true);
            }
            else
            {
                questDescriptors = new ArrayList<>(numItems);
                for (int i = 0; i < numItems; ++i)
                    questDescriptors.add(serializer.resource(null, ResourceType.QUEST,
                        true));
            }
        }

        if (subVersion >= 0xd1)
            startPointList = serializer.arraylist(startPointList, StartPoint.class);
    }

    @Override
    public int getAllocatedSize()
    {
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
