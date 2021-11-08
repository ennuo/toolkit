package ennuo.craftworld.resources;

import ennuo.craftworld.resources.structs.plan.InventoryDetails;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.utilities.Compressor;
import ennuo.toolkit.utilities.Globals;

public class Plan implements Serializable {
    public boolean isUsedForStreaming;
    public int revision;
    public byte[] thingData;
    public InventoryDetails details = new InventoryDetails();

    public Plan serialize(Serializer serializer, Serializable structure) {
        Plan plan = (structure == null) ? new Plan() : (Plan) structure;
        
        if (serializer.revision >= 0x00D003E7)
            plan.isUsedForStreaming = serializer.bool(plan.isUsedForStreaming);
        plan.revision = serializer.i32(plan.revision);
        plan.thingData = serializer.i8a(plan.thingData);
        
        // NOTE(Abz): This serializer isn't fully finished because of how many revisions
        // there are, so wrapping it in an try/catch block just in case.
        
        try {
            if (serializer.revision > 0x18a) {
                plan.details = serializer.struct(plan.details, InventoryDetails.class);

                if (serializer.revision == 0x272 || serializer.revision > 0x2ba) {
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
        
        return plan;
    }
    
    public byte[] build(int revision) {
        int dataSize = InventoryDetails.MAX_SIZE + this.thingData.length;
        Serializer serializer = new Serializer(dataSize, revision);
        this.serialize(serializer, this);
        return Compressor.Compress(serializer.getBuffer(), "PLNb", revision, 
                    serializer.output.dependencies.toArray(new ResourceDescriptor[serializer.output.dependencies.size()]));      
    }
}
