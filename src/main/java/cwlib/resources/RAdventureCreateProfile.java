package cwlib.resources;

import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.types.data.Revision;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;
import cwlib.util.Bytes;
import cwlib.util.Matcher;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This won't be a complete implementation of the adventure
 * create profile, since I don't have everything setup to parse the
 * world data, this is solely for editing the slots contained within.
 */
public class RAdventureCreateProfile implements Serializable {
    public HashMap<SlotID, Slot> adventureSlots;
    private byte[] unparsedData;
    private HashSet<ResourceReference> dependencyCache;
    
    public RAdventureCreateProfile(){};
    public RAdventureCreateProfile(Resource resource) {
        this.serialize(new Serializer(resource), this);
    }
    
    @SuppressWarnings("unchecked")
    @Override public RAdventureCreateProfile serialize(Serializer serializer, Serializable structure) {
        RAdventureCreateProfile profile = 
                (structure == null) ? new RAdventureCreateProfile() : (RAdventureCreateProfile) structure;
        
        if (!serializer.isWriting) {
            this.dependencyCache = new HashSet<>(serializer.dependencies);
            serializer.dependencies.clear();
        }
        
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
        
        if (!serializer.isWriting) {
            // Remove slot dependencies from cache
            for (ResourceReference descriptor : serializer.dependencies) {
                ResourceType type = descriptor.type;
                // These are the only types that appear in slotss.
                if (type.equals(ResourceType.TEXTURE) || type.equals(ResourceType.LEVEL) || 
                    type.equals(ResourceType.ADVENTURE_CREATE_PROFILE) || type.equals(ResourceType.PLAN)) {
                    byte[] pattern = Bytes.createResourceReference(descriptor, serializer.revision, serializer.compressionFlags);
                    boolean isInData = Matcher.indexOf(profile.unparsedData, pattern) != -1;
                    if (!isInData)
                        profile.dependencyCache.remove(descriptor);
                }
            }
        }
        
        return profile;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        int dataSize = 0x1000 * this.adventureSlots.size() + this.unparsedData.length;
        Serializer serializer = new Serializer(dataSize, revision, compressionFlags);
        
        // Re-add dependencies from unparsed data.
        for (ResourceReference descriptor : this.dependencyCache) {
            serializer.dependencies.add(descriptor);
            serializer.output.dependencies.add(descriptor);
        }
        
        this.serialize(serializer, this);
        return Resource.compressToResource(serializer.output, ResourceType.ADVENTURE_CREATE_PROFILE);      
    }
}
