package ennuo.craftworld.things.parts;

import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.plan.PhotoMetadata;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;
import ennuo.craftworld.types.data.ResourceDescriptor;

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
    
    public PMetadata serialize(Serializer serializer, Serializable structure) {
        PMetadata metadata = (structure == null) ? new PMetadata() : (PMetadata) structure;
        
        if ((serializer.revision.head == 0x272 && serializer.revision.branchID != 0) || serializer.revision.head > 0x2ba) {
            metadata.titleKey = serializer.u32(metadata.titleKey);
            metadata.descriptionKey = serializer.u32(metadata.descriptionKey);
            metadata.location = serializer.u32(metadata.location);
            metadata.category = serializer.u32(metadata.category);
        } else {
            metadata.nameTranslationTag = serializer.str8(metadata.nameTranslationTag);
            metadata.descTranslationTag = serializer.str8(metadata.descTranslationTag);
            metadata.locationTag = serializer.str8(metadata.locationTag);
            metadata.categoryTag = serializer.str8(metadata.categoryTag);
            if (!serializer.isWriting) {
                metadata.titleKey =
                        TranslationTable.makeLamsKeyID(metadata.nameTranslationTag);
                metadata.descriptionKey =
                        TranslationTable.makeLamsKeyID(metadata.descTranslationTag);
                metadata.location 
                        = TranslationTable.makeLamsKeyID(metadata.locationTag);
                metadata.category 
                        = TranslationTable.makeLamsKeyID(metadata.categoryTag);
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
    
}
