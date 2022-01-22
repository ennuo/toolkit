package ennuo.craftworld.types.savedata;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import java.util.HashMap;
import java.util.Set;

public class BigProfile implements Serializable {
    public CachedInventoryItem[] inventory;
    public StringTable stringTable;
    public boolean fromProductionBuild;
    public HashMap<SlotID, Slot> myMoonSlots;
    public DataLabel[] creatorDataLabels;
    public Challenge[] nearMyChallengeDataStore;
    public Treasure[] nearMyTreasuresDataStore;
    public SlotID[] downloadedSlots;
    public Resource planetDecorations;

    public Serializable serialize(Serializer serializer, Serializable structure) {
        BigProfile profile = (structure == null) ? new BigProfile() : (BigProfile) structure;
        
        profile.inventory = serializer.array(profile.inventory, CachedInventoryItem.class);
        profile.stringTable = serializer.struct(profile.stringTable, StringTable.class);
        if (serializer.revision.head > 0x3b5)
            profile.fromProductionBuild = serializer.bool(profile.fromProductionBuild);
        
        if (serializer.isWriting) {
            Set<SlotID> keys = profile.myMoonSlots.keySet();
            serializer.output.i32(keys.size());
            for (SlotID key : keys) {
                serializer.struct(key, SlotID.class);
                serializer.struct(profile.myMoonSlots.get(key), Slot.class);
            }
        } else {
            int count = serializer.input.i32();
            profile.myMoonSlots = new HashMap<SlotID, Slot>(count);
            for (int i = 0; i < count; ++i)
                profile.myMoonSlots.put(
                        serializer.struct(null, SlotID.class), 
                        serializer.struct(null, Slot.class));
        }
        
        return this;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(1000 * 1024, revision, compressionFlags);
        serializer.struct(this, BigProfile.class);
        return Resource.compressToResource(serializer.output, ResourceType.BIG_PROFILE);
    }
}
