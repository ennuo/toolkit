package cwlib.resources;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.types.data.Revision;
import cwlib.structs.slot.Slot;
import cwlib.structs.slot.SlotID;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
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
public class RAdventureCreateProfile implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x8;

    private HashMap<SlotID, Slot> adventureSlots = new HashMap<>();

    private byte[] unparsedData;
    private HashSet<ResourceDescriptor> dependencyCache;
    
    @SuppressWarnings("unchecked")
    @Override public RAdventureCreateProfile serialize(Serializer serializer, Serializable structure) {
        RAdventureCreateProfile profile = 
                (structure == null) ? new RAdventureCreateProfile() : (RAdventureCreateProfile) structure;
        
        if (!serializer.isWriting()) {
            for (ResourceDescriptor descriptor : serializer.getDependencies())
                profile.dependencyCache.add(descriptor);
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

    @Override public int getAllocatedSize() {
        int size = RAdventureCreateProfile.BASE_ALLOCATION_SIZE;
        for (Slot slot : this.adventureSlots.values())
            size += slot.getAllocatedSize() + SlotID.BASE_ALLOCATION_SIZE;
        if (this.unparsedData != null) size += this.unparsedData.length;
        return size;
    }
    
    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RAdventureCreateProfile.class);
        for (ResourceDescriptor descriptor : this.dependencyCache)
            serializer.addDependency(descriptor);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.PLAN,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    public HashMap<SlotID, Slot> getAdventureSlots() { return this.adventureSlots; }
}
