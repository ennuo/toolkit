package cwlib.structs.things.parts;

import cwlib.resources.RTranslationTable;
import cwlib.enums.Branch;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.structs.inventory.PhotoMetadata;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PMetadata implements Serializable {
    public String nameTranslationTag, descTranslationTag;
    public String locationTag, categoryTag;
    
    public long titleKey, descriptionKey;
    public long location, category;
    
    public int primaryIndex;
    public int fluffCost;
    public int type, subType;
    public long creationDate;
    
    public ResourceDescriptor icon;
    public PhotoMetadata photoMetadata;
    
    public boolean referencable;
    public boolean allowEmit;
    
    @SuppressWarnings("unchecked")
    @Override public PMetadata serialize(Serializer serializer, Serializable structure) {
        PMetadata metadata = (structure == null) ? new PMetadata() : (PMetadata) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        if (revision.has(Branch.LEERDAMMER, Revisions.LD_LAMS_KEYS) || version > 0x2ba) {
            metadata.titleKey = serializer.u32(metadata.titleKey);
            metadata.descriptionKey = serializer.u32(metadata.descriptionKey);
            metadata.location = serializer.u32(metadata.location);
            metadata.category = serializer.u32(metadata.category);
        } else {
            metadata.nameTranslationTag = serializer.str(metadata.nameTranslationTag);
            metadata.descTranslationTag = serializer.str(metadata.descTranslationTag);
            metadata.locationTag = serializer.str(metadata.locationTag);
            metadata.categoryTag = serializer.str(metadata.categoryTag);
            if (!serializer.isWriting()) {
                metadata.titleKey =
                        RTranslationTable.makeLamsKeyID(metadata.nameTranslationTag);
                metadata.descriptionKey =
                        RTranslationTable.makeLamsKeyID(metadata.descTranslationTag);
                metadata.location 
                        = RTranslationTable.makeLamsKeyID(metadata.locationTag);
                metadata.category 
                        = RTranslationTable.makeLamsKeyID(metadata.categoryTag);
            }
        }
        
        metadata.primaryIndex = serializer.i32(metadata.primaryIndex);
        metadata.fluffCost = serializer.i32(metadata.fluffCost);
        metadata.type = serializer.i32(metadata.type);
        metadata.subType = serializer.i32(metadata.subType);
        metadata.creationDate = serializer.i64(metadata.creationDate);
        
        metadata.icon = serializer.resource(metadata.icon, ResourceType.TEXTURE);
        metadata.photoMetadata = serializer.reference(metadata.photoMetadata, PhotoMetadata.class);
        
        metadata.referencable = serializer.bool(metadata.referencable);
        metadata.allowEmit = serializer.bool(metadata.allowEmit);
        
        return metadata;
    }
    
    // TODO: Actually implement
    @Override public int getAllocatedSize() { return 0; }
}
