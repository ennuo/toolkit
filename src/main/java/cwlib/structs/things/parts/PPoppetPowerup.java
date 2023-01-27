package cwlib.structs.things.parts;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;

public class PPoppetPowerup implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x40;

    public long tools;
    public ArrayList<ResourceDescriptor> items = new ArrayList<>();
    public boolean vcrControlsEnabled;
    public int gridSnapMode;
    public boolean sackFreeze;
    public int minLayer, maxLayer;
    public boolean vcrBackupTriggered;
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

        if (((subVersion - 0x8) <= subVersion) && (subVersion < 0x174)) {
            serializer.u8(0);
            serializer.u8(0);
        }

        if (((subVersion - 0xb) <= subVersion) && (subVersion < 0x174))
            serializer.i32(0);

        if (subVersion > 0x8b)
            powerup.vcrControlsEnabled = serializer.bool(powerup.vcrControlsEnabled);

        if (subVersion > 0xa1)
            powerup.gridSnapMode = serializer.s32(powerup.gridSnapMode);

        if (((subVersion - 0xc1) <= subVersion) && subVersion < 0x174)
            serializer.u8(0);

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
