package cwlib.structs.things.parts;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.profile.InventoryItem;
import cwlib.types.data.ResourceDescriptor;

public class PPoppetPowerup implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    @GsonRevision(lbp3=true, min=0x125)
    public long tools;

    @GsonRevision(lbp3=true, min=0x125)
    public ArrayList<ResourceDescriptor> items = new ArrayList<>();

    @GsonRevision(lbp3=true, min=0x8c)
    public boolean vcrControlsEnabled;

    @GsonRevision(lbp3=true, min=0xa2)
    public int gridSnapMode;

    @GsonRevision(lbp3=true, min=0x12e)
    public boolean sackFreeze;

    @GsonRevision(lbp3=true, min=0x12f)
    public int minLayer, maxLayer;

    @GsonRevision(lbp3=true, min=0x14b)
    public boolean vcrBackupTriggered;

    @GsonRevision(lbp3=true, min=0x167)
    public boolean filterContents;

    @SuppressWarnings("unchecked")
    @Override public PPoppetPowerup serialize(Serializer serializer, Serializable structure) {
        PPoppetPowerup powerup = (structure == null) ? new PPoppetPowerup() : (PPoppetPowerup) structure;

        int subVersion = serializer.getRevision().getSubVersion();

        if (subVersion > 0x124) {
            powerup.tools = serializer.i64(powerup.tools);
            int numItems = serializer.i32(powerup.items != null ? powerup.items.size() : 0);
            if (serializer.isWriting()) {
                for (ResourceDescriptor descriptor : powerup.items)
                    serializer.resource(descriptor, ResourceType.PLAN, true);
            } else {
                powerup.items = new ArrayList<>(numItems);
                for (int i = 0; i < numItems; ++i)
                    powerup.items.add(serializer.resource(null, ResourceType.PLAN, true));
            }
        }

        // I should probably fix this up so it
        // can re-serialize the items
        // but also, who the hell is working at these revisions?
        if (subVersion >= 0x7 && subVersion < 0x124)
            serializer.array(null, InventoryItem.class);
        if (subVersion >= 0xb && subVersion < 0x123) {
            int count = serializer.i32(0);
            for (int i = 0; i < count; ++i) {
                serializer.i32(0);
                serializer.i32(0);
            }
        }


        if (subVersion >= 0x8 && subVersion < 0x174) {
            serializer.u8(0); // goodies
            serializer.u8(0); // tools
        }

        if (subVersion >= 0xb && subVersion < 0x174)
            serializer.s32(0); // nextUid

        if (subVersion > 0x8b)
            powerup.vcrControlsEnabled = serializer.bool(powerup.vcrControlsEnabled);

        if (subVersion > 0xa1)
            powerup.gridSnapMode = serializer.s32(powerup.gridSnapMode);

        if (subVersion >= 0xc1 && subVersion < 0x174)
            serializer.u8(0); // allowCursorToCopy

        if (subVersion > 0x12d)
            powerup.sackFreeze = serializer.bool(powerup.sackFreeze);

        if (subVersion > 0x12e) {
            powerup.minLayer = serializer.s32(powerup.minLayer);
            powerup.maxLayer = serializer.s32(powerup.maxLayer);
        }

        if (subVersion > 0x14a)
            powerup.vcrBackupTriggered = serializer.bool(powerup.vcrBackupTriggered);

        if (subVersion > 0x166)
            powerup.filterContents = serializer.bool(powerup.filterContents);


        return powerup;
    }


    @Override public int getAllocatedSize() {
        int size = PPoppetPowerup.BASE_ALLOCATION_SIZE;
        if (this.items != null)
            size += (this.items.size() * 0x24);
        return size;
    }
}
