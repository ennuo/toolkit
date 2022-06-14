package cwlib.resources;

import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.types.data.Revision;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceReference;
import cwlib.util.Bytes;
import cwlib.util.Matcher;
import toolkit.utilities.Globals;

import java.util.HashSet;

public class RPlan implements Serializable {
    public boolean isUsedForStreaming;
    public int revision;
    public byte[] thingData;
    public InventoryItemDetails details = new InventoryItemDetails();
    
    /**
     * Cache of dependencies in thing data, so we don't have
     * to parse it.
     */
    private HashSet<ResourceReference> dependencyCache;
    
    public RPlan(){}
    public RPlan(Resource resource) {
        this.serialize(new Serializer(resource), this);
    }

    public RPlan serialize(Serializer serializer, Serializable structure) {
        RPlan plan = (structure == null) ? new RPlan() : (RPlan) structure;
        
        // We only want to maintain thing data dependencies in this stream
        if (!serializer.isWriting) {
            this.dependencyCache = new HashSet<>(serializer.dependencies);
            serializer.dependencies.clear();
        }
        
        if (serializer.revision.isAfterLBP3Revision(0xcb))
            plan.isUsedForStreaming = serializer.bool(plan.isUsedForStreaming);
        plan.revision = serializer.i32(plan.revision);
        plan.thingData = serializer.i8a(plan.thingData);
        
        // NOTE(Aidan): This serializer isn't fully finished because of how many revisions
        // there are, so wrapping it in an try/catch block just in case.
        
        try {
            if (serializer.revision.head >= 0x197 && !plan.isUsedForStreaming) {
                plan.details = serializer.struct(plan.details, InventoryItemDetails.class);

                if (serializer.revision.isAfterLeerdammerRevision(7) || serializer.revision.head > 0x2ba) {
                    plan.details.location = serializer.u32(plan.details.location);
                    plan.details.category = serializer.u32(plan.details.category);
                } else {
                    plan.details.locationTag = serializer.str8(plan.details.locationTag);
                    plan.details.categoryTag = serializer.str8(plan.details.categoryTag);
                    if (!serializer.isWriting) {
                        plan.details.location 
                                = RTranslationTable.makeLamsKeyID(plan.details.locationTag);
                        plan.details.category 
                                = RTranslationTable.makeLamsKeyID(plan.details.categoryTag);
                    }
                }

                if (Globals.LAMS != null && !serializer.isWriting) {
                    if (plan.details.location != 0)
                        plan.details.translatedLocation 
                                = Globals.LAMS.translate(plan.details.location);
                    if (plan.details.category != 0)
                        plan.details.translatedCategory = 
                                Globals.LAMS.translate(plan.details.category);
                }
            }
        } catch (Exception e) {
            plan.details = null;
            System.err.println("There was an error processing inventory details.");
        }
        
        if (!serializer.isWriting) {
            // Remove dependencies of inventory item details,
            // since dependency cache should only be dependencies
            // in the thing data.
            for (ResourceReference descriptor : serializer.dependencies) {
                ResourceType type = descriptor.type;
                // These are the only types that appear in inventory item details.
                if (type.equals(ResourceType.TEXTURE) || type.equals(ResourceType.FILENAME) || type.equals(ResourceType.PAINTING)) {
                    byte[] pattern = null;
                    // This is GUID only
                    if (type.equals(ResourceType.FILENAME))
                        pattern = Bytes.createGUID(descriptor.GUID, serializer.compressionFlags);
                    else
                        pattern = Bytes.createResourceReference(descriptor, serializer.revision, serializer.compressionFlags);
                    boolean isInThingData = Matcher.indexOf(plan.thingData, pattern) != -1;
                    if (!isInThingData)
                        plan.dependencyCache.remove(descriptor);
                }
            }
        }
        
        return plan;
    }
    
    public static void removePlanDescriptors(Resource resource, long GUID) {
        if (resource.type != ResourceType.PLAN) return;
        RPlan plan = new RPlan(resource);
        plan.removePlanDescriptors(GUID, resource.revision, resource.compressionFlags);
        resource.handle.setData(plan.build(resource.revision, resource.compressionFlags, false));
    }
    
    public void removePlanDescriptors(long GUID, Revision revision, byte compressionFlags) {
        ResourceReference descriptor = new ResourceReference(GUID, ResourceType.PLAN);
        
        MemoryInputStream thingData = new MemoryInputStream(this.thingData);
        
        byte[] descriptorBuffer = Bytes.createResourceReference(descriptor, revision, compressionFlags);
        byte[] guidBuffer = Bytes.getIntegerBuffer(GUID, compressionFlags);
        
        Bytes.ReplaceAll(thingData, descriptorBuffer, new byte[] { 00 });
        Bytes.ReplaceAll(thingData, guidBuffer, new byte[] { 00 });
        
        this.thingData = thingData.data;
    }
    
    public byte[] build(Revision revision, byte compressionFlags, boolean shouldCompress) {
        int dataSize = InventoryItemDetails.MAX_SIZE + this.thingData.length + 0x8;
        Serializer serializer = new Serializer(dataSize, revision, compressionFlags);
        this.serialize(serializer, this);
        
        // Re-add dependencies from thing data.
        for (ResourceReference descriptor : this.dependencyCache) {
            serializer.dependencies.add(descriptor);
            serializer.output.dependencies.add(descriptor);
        }
        
        if (shouldCompress)
            return Resource.compressToResource(serializer.output, ResourceType.PLAN);
        return serializer.getBuffer();
    }
}
