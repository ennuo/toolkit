package ennuo.craftworld.resources;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.utilities.Bytes;
import ennuo.toolkit.utilities.Globals;
import java.util.HashSet;

public class Plan implements Serializable {
    public boolean isUsedForStreaming;
    public int revision;
    public byte[] thingData;
    public InventoryDetails details = new InventoryDetails();
    
    /**
     * Cache of dependencies in thing data, so we don't have
     * to parse it.
     */
    public HashSet<ResourceDescriptor> dependencyCache;
    
    public Plan(){}
    public Plan(Resource resource) {
        this.dependencyCache = new HashSet<>(resource.dependencies);
        this.serialize(new Serializer(resource.handle), this);
    }

    public Plan serialize(Serializer serializer, Serializable structure) {
        Plan plan = (structure == null) ? new Plan() : (Plan) structure;
        
        if (serializer.revision.isAfterLBP3Revision(0xcb))
            plan.isUsedForStreaming = serializer.bool(plan.isUsedForStreaming);
        plan.revision = serializer.i32(plan.revision);
        plan.thingData = serializer.i8a(plan.thingData);
        
        // NOTE(Aidan): This serializer isn't fully finished because of how many revisions
        // there are, so wrapping it in an try/catch block just in case.
        
        try {
            if (serializer.revision.head >= 0x197 && !plan.isUsedForStreaming) {
                plan.details = serializer.struct(plan.details, InventoryDetails.class);

                if (serializer.revision.isAfterLeerdammerRevision(7) || serializer.revision.head > 0x2ba) {
                    plan.details.location = serializer.u32(plan.details.location);
                    plan.details.category = serializer.u32(plan.details.category);
                } else {
                    plan.details.locationTag = serializer.str8(plan.details.locationTag);
                    plan.details.categoryTag = serializer.str8(plan.details.categoryTag);
                    if (!serializer.isWriting) {
                        plan.details.location 
                                = TranslationTable.makeLamsKeyID(plan.details.locationTag);
                        plan.details.category 
                                = TranslationTable.makeLamsKeyID(plan.details.categoryTag);
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
        
        // Remove dependencies of inventory item details,
        // since dependency cache should only be dependencies
        // in the thing data.
        //for (ResourceDescriptor descriptor : serializer.dependencies)
        //    this.dependencyCache.remove(descriptor);
        
        return plan;
    }
    
    public static void removePlanDescriptors(Resource resource, long GUID) {
        if (resource.type != ResourceType.PLAN) return;
        Plan plan = new Plan(resource);
        plan.removePlanDescriptors(GUID, resource.revision, resource.compressionFlags);
        resource.handle.setData(plan.build(resource.revision, resource.compressionFlags, false));
    }
    
    public void removePlanDescriptors(long GUID, Revision revision, byte compressionFlags) {
        ResourceDescriptor descriptor = new ResourceDescriptor(GUID, ResourceType.PLAN);
        
        Data thingData = new Data(this.thingData);
        
        byte[] descriptorBuffer = Bytes.createResourceReference(descriptor, revision, compressionFlags);
        byte[] guidBuffer = new Output(0x8, this.revision).u32(GUID).shrink().buffer;
        
        Bytes.ReplaceAll(thingData, descriptorBuffer, new byte[] { 00 });
        Bytes.ReplaceAll(thingData, guidBuffer, new byte[] { 00 });
        
        this.thingData = thingData.data;
    }
    
    public byte[] build(Revision revision, byte compressionFlags, boolean shouldCompress) {
        int dataSize = InventoryDetails.MAX_SIZE + this.thingData.length;
        Serializer serializer = new Serializer(dataSize, revision, compressionFlags);
        this.serialize(serializer, this);
        if (shouldCompress)
            return Resource.compressToResource(serializer.output, ResourceType.PLAN);
        return serializer.getBuffer();
    }
}
