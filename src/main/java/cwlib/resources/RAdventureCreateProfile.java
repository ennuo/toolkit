package cwlib.resources;

import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.types.data.Revision;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;
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
    private HashSet<ResourceDescriptor> dependencyCache;
    
    @SuppressWarnings("unchecked")
    @Override public RAdventureCreateProfile serialize(Serializer serializer, Serializable structure) {
        RAdventureCreateProfile profile = 
                (structure == null) ? new RAdventureCreateProfile() : (RAdventureCreateProfile) structure;
        
        if (!serializer.isWriting()) {
            this.dependencyCache = new HashSet<>(serializer.getDependencies());
            serializer.clearDependencies();
        }
        
        if (serializer.isWriting()) {
            Set<SlotID> keys = profile.adventureSlots.keySet();
            serializer.getOutput().i32(keys.size());
            for (SlotID key : keys) {
                serializer.struct(key, SlotID.class);
                serializer.struct(profile.adventureSlots.get(key), Slot.class);
            }
        } else {
            int count = serializer.getInput().i32();
            profile.adventureSlots = new HashMap<SlotID, Slot>(count);
            for (int i = 0; i < count; ++i)
                profile.adventureSlots.put(
                        serializer.struct(null, SlotID.class), 
                        serializer.struct(null, Slot.class));
        }
        
        // Not parsing this data right now
        if (serializer.isWriting()) serializer.getOutput().bytes(profile.unparsedData);
        else profile.unparsedData = serializer.getInput().bytes(serializer.getInput().getLength() - serializer.getInput().getOffset());
        
        if (!serializer.isWriting()) {
            // Remove slot dependencies from cache
            for (ResourceDescriptor descriptor : serializer.getDependencies()) {
                ResourceType type = descriptor.getType();
                // These are the only types that appear in slotss.
                if (type.equals(ResourceType.TEXTURE) || type.equals(ResourceType.LEVEL) || 
                    type.equals(ResourceType.ADVENTURE_CREATE_PROFILE) || type.equals(ResourceType.PLAN)) {
                    byte[] pattern = Bytes.getResourceReference(descriptor, serializer.getRevision(), serializer.getCompressionFlags());
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
        for (ResourceDescriptor descriptor : this.dependencyCache)
            serializer.addDependency(descriptor);

        this.serialize(serializer, this);
        return Resource.compressToResource(serializer.output, ResourceType.ADVENTURE_CREATE_PROFILE);      
    }
}
