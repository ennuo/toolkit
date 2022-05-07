package ennuo.craftworld.resources;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.resources.structs.SlotID;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import java.util.HashMap;
import java.util.Set;

/**
 * This won't be a complete implementation of the adventure
 * create profile, since I don't have everything setup to parse the
 * world data, this is solely for editing the slots contained within.
 */
public class AdventureCreateProfile implements Serializable {
    public HashMap<SlotID, Slot> adventureSlots;
    private byte[] unparsedData;
    
    @SuppressWarnings("unchecked")
    @Override public AdventureCreateProfile serialize(Serializer serializer, Serializable structure) {
        AdventureCreateProfile profile = 
                (structure == null) ? new AdventureCreateProfile() : (AdventureCreateProfile) structure;
        
        if (serializer.isWriting) {
            Set<SlotID> keys = profile.adventureSlots.keySet();
            serializer.output.i32(keys.size());
            for (SlotID key : keys) {
                serializer.struct(key, SlotID.class);
                serializer.struct(profile.adventureSlots.get(key), Slot.class);
            }
        } else {
            int count = serializer.input.i32();
            profile.adventureSlots = new HashMap<SlotID, Slot>(count);
            for (int i = 0; i < count; ++i)
                profile.adventureSlots.put(
                        serializer.struct(null, SlotID.class), 
                        serializer.struct(null, Slot.class));
        }
        
        // Not parsing this data right now
        if (serializer.isWriting) serializer.output.bytes(profile.unparsedData);
        else profile.unparsedData = serializer.input.bytes(serializer.input.length - serializer.input.offset);
        
        return profile;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        int dataSize = 0x1000 * this.adventureSlots.size();
        Serializer serializer = new Serializer(dataSize, revision, compressionFlags);
        this.serialize(serializer, this);
        return Resource.compressToResource(serializer.output, ResourceType.ADVENTURE_CREATE_PROFILE);      
    }
}
