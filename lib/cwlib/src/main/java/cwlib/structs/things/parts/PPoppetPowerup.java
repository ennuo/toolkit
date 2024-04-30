package cwlib.structs.things.parts;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.profile.InventoryItem;
import cwlib.types.data.ResourceDescriptor;

public class PPoppetPowerup implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    @GsonRevision(lbp3 = true, min = 0x125)
    public long tools;

    @GsonRevision(lbp3 = true, min = 0x125)
    public ArrayList<ResourceDescriptor> items = new ArrayList<>();

    @GsonRevision(lbp3 = true, min = 0x8c)
    public boolean vcrControlsEnabled;

    @GsonRevision(lbp3 = true, min = 0xa2)
    public int gridSnapMode;

    @GsonRevision(lbp3 = true, min = 0x12e)
    public boolean sackFreeze;

    @GsonRevision(lbp3 = true, min = 0x12f)
    public int minLayer, maxLayer;

    @GsonRevision(lbp3 = true, min = 0x14b)
    public boolean vcrBackupTriggered;

    @GsonRevision(lbp3 = true, min = 0x167)
    public boolean filterContents;

    @Override
    public void serialize(Serializer serializer)
    {
        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x124)
        {
            tools = serializer.u64(tools);
            int numItems = serializer.i32(items != null ? items.size() : 0);
            if (serializer.isWriting())
            {
                for (ResourceDescriptor descriptor : items)
                    serializer.resource(descriptor, ResourceType.PLAN, true);
            }
            else
            {
                items = new ArrayList<>(numItems);
                for (int i = 0; i < numItems; ++i)
                    items.add(serializer.resource(null, ResourceType.PLAN, true));
            }
        }

        // I should probably fix this up so it
        // can re-serialize the items
        // but also, who the hell is working at these revisions?
        if (subVersion >= 0x7 && subVersion < 0x124)
            serializer.array(null, InventoryItem.class);
        if (subVersion >= 0xb && subVersion < 0x123)
        {
            int count = serializer.i32(0);
            for (int i = 0; i < count; ++i)
            {
                serializer.i32(0);
                serializer.i32(0);
            }
        }


        if (subVersion >= 0x8 && subVersion < 0x174)
        {
            serializer.u8(0); // goodies
            serializer.u8(0); // tools
        }

        if (subVersion >= 0xb && subVersion < 0x174)
            serializer.s32(0); // nextUid

        if (subVersion > 0x8b)
            vcrControlsEnabled = serializer.bool(vcrControlsEnabled);

        if (subVersion > 0xa1)
            gridSnapMode = serializer.s32(gridSnapMode);

        if (subVersion >= 0xc1 && subVersion < 0x174)
            serializer.u8(0); // allowCursorToCopy

        if (subVersion > 0x12d)
            sackFreeze = serializer.bool(sackFreeze);

        if (subVersion > 0x12e)
        {
            minLayer = serializer.s32(minLayer);
            maxLayer = serializer.s32(maxLayer);
        }

        if (subVersion > 0x14a)
            vcrBackupTriggered = serializer.bool(vcrBackupTriggered);

        if (subVersion > 0x166)
            filterContents = serializer.bool(filterContents);
    }


    @Override
    public int getAllocatedSize()
    {
        int size = PPoppetPowerup.BASE_ALLOCATION_SIZE;
        if (this.items != null)
            size += (this.items.size() * 0x24);
        return size;
    }
}
