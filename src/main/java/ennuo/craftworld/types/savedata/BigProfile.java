package ennuo.craftworld.types.savedata;

import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;
import java.util.HashMap;
import java.util.Set;

public class BigProfile implements Serializable {
    public CachedInventoryItem[] inventory;
    public SHA1[] vitaCrossDependencyHashes;
    public StringTable stringTable;
    public boolean fromProductionBuild;
    public HashMap<SlotID, Slot> myMoonSlots;
    
    public DataLabel[] creatorDataLabels;
    
    public Challenge[] nearMyChallengeDataLog;
    public Challenge[] nearMyChallengeDataOpen;
    public Treasure[] nearMyTreasureLog;
    
    public SlotID[] downloadedSlots;
    public ResourceDescriptor planetDecorations;

    public Serializable serialize(Serializer serializer, Serializable structure) {
        BigProfile profile = (structure == null) ? new BigProfile() : (BigProfile) structure;
        
        profile.inventory = serializer.array(profile.inventory, CachedInventoryItem.class);
        
        // Maybe I should see if I can abuse reflection or something
        // to handle arrays, or I can just write all the ones I need already.
        if (0x3ea < serializer.revision.head) {
            if (serializer.isWriting) {
                if (this.vitaCrossDependencyHashes != null) {
                    serializer.output.i32(this.vitaCrossDependencyHashes.length);
                    for (SHA1 hash : this.vitaCrossDependencyHashes)
                        serializer.output.sha1(hash);
                } else serializer.i32(0);
            } else {
                int size = serializer.input.i32();
                this.vitaCrossDependencyHashes = new SHA1[size];
                for (int i = 0; i < size; ++i)
                    this.vitaCrossDependencyHashes[i] = serializer.input.sha1();
            }
        }
        
        if (serializer.revision.head >= 0x3ef)
            profile.creatorDataLabels = serializer.array(profile.creatorDataLabels, DataLabel.class);
        
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
        
        if (serializer.revision.isAfterVitaRevision(0x2d))
            profile.creatorDataLabels = serializer.array(profile.creatorDataLabels, DataLabel.class);
        
        if (serializer.revision.isAfterVitaRevision(0x56)) {
            profile.nearMyChallengeDataLog = serializer.array(profile.nearMyChallengeDataLog, Challenge.class);
            profile.nearMyChallengeDataOpen = serializer.array(profile.nearMyChallengeDataOpen, Challenge.class);
        }
        
        if (serializer.revision.isAfterVitaRevision(0x58))
            profile.nearMyTreasureLog = serializer.array(profile.nearMyTreasureLog, Treasure.class);

        if (serializer.revision.isAfterVitaRevision(0x59))
            profile.downloadedSlots = serializer.array(profile.downloadedSlots, SlotID.class);
        
        if (serializer.revision.isAfterVitaRevision(0x7a))
            profile.planetDecorations = serializer.resource(profile.planetDecorations, ResourceType.LEVEL, true);
        
        return this;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(1000 * 1024, revision, compressionFlags);
        serializer.struct(this, BigProfile.class);
        return Resource.compressToResource(serializer.output, ResourceType.BIG_PROFILE);
    }
}
