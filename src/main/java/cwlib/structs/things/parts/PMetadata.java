package cwlib.structs.things.parts;

import cwlib.resources.RTranslationTable;

import java.util.EnumSet;

import cwlib.enums.Branch;
import cwlib.enums.InventoryObjectType;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.structs.inventory.PhotoMetadata;
import cwlib.structs.things.components.Value;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

public class PMetadata implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x160;

    @GsonRevision(branch=0, max=0x296)
    @GsonRevision(branch=0x4c44, max=1)
    @Deprecated public Value value = new Value();

    @GsonRevision(branch=0, max=0x2ba)
    @GsonRevision(branch=0x4c44, max=0x7)
    public String nameTranslationTag, locationTag, categoryTag;
    @GsonRevision(max=0x158) public String descTranslationTag;
    
    @GsonRevision(min=0x2bb)
    @GsonRevision(branch=0x4c44, min=0x8)
    public long titleKey, descriptionKey, location, category;
    
    @GsonRevision(min=0x195) public int primaryIndex;
    public int fluffCost;
    public EnumSet<InventoryObjectType> type = EnumSet.noneOf(InventoryObjectType.class);
    public int subType;
    public long creationDate;
    
    public ResourceDescriptor icon;
    public PhotoMetadata photoMetadata;
    
    @GsonRevision(min=0x15f) public boolean referencable;
    @GsonRevision(min=0x205) public boolean allowEmit;
    
    @SuppressWarnings("unchecked")
    @Override public PMetadata serialize(Serializer serializer, Serializable structure) {
        PMetadata metadata = (structure == null) ? new PMetadata() : (PMetadata) structure;
        
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        boolean hasDepreciatedValue = (version < 0x297 && !revision.isLeerdammer()) || (revision.isLeerdammer() && !revision.has(Branch.LEERDAMMER, Revisions.LD_RESOURCES));

        if (hasDepreciatedValue)
            metadata.value = serializer.struct(metadata.value, Value.class);
        
        if (revision.has(Branch.LEERDAMMER, Revisions.LD_LAMS_KEYS) || version > 0x2ba) {
            metadata.titleKey = serializer.u32(metadata.titleKey);
            metadata.descriptionKey = serializer.u32(metadata.descriptionKey);
            metadata.location = serializer.u32(metadata.location);
            metadata.category = serializer.u32(metadata.category);
        } else {
            metadata.nameTranslationTag = serializer.str(metadata.nameTranslationTag);
            if (version < 0x159)
                metadata.descTranslationTag = serializer.str(metadata.descTranslationTag);
            else if (!serializer.isWriting())
                metadata.descTranslationTag = metadata.nameTranslationTag + "_DESC";
            
            metadata.locationTag = serializer.str(metadata.locationTag);
            metadata.categoryTag = serializer.str(metadata.categoryTag);
            if (!serializer.isWriting()) {
                if (version < 0x159) {
                    metadata.titleKey =
                        RTranslationTable.makeLamsKeyID(metadata.nameTranslationTag);
                    metadata.descriptionKey =
                        RTranslationTable.makeLamsKeyID(metadata.descTranslationTag);
                } else {
                    metadata.titleKey = RTranslationTable.makeLamsKeyID(metadata.nameTranslationTag + "_NAME");
                    metadata.titleKey = RTranslationTable.makeLamsKeyID(metadata.nameTranslationTag + "_DESC");
                }
                
                metadata.location 
                        = RTranslationTable.makeLamsKeyID(metadata.locationTag);
                metadata.category 
                        = RTranslationTable.makeLamsKeyID(metadata.categoryTag);
            }
        }
        
        if (version >= 0x195)
            metadata.primaryIndex = serializer.i32(metadata.primaryIndex);
        metadata.fluffCost = serializer.i32(metadata.fluffCost);

        if (hasDepreciatedValue) serializer.i32(0); // unknown
        
        if (serializer.isWriting())
            serializer.getOutput().i32(InventoryObjectType.getFlags(metadata.type));
        else
            metadata.type = InventoryObjectType.fromFlags(serializer.getInput().i32(), serializer.getRevision());
        
        metadata.subType = serializer.i32(metadata.subType);
        metadata.creationDate = serializer.u32(metadata.creationDate);
        
        metadata.icon = serializer.resource(metadata.icon, ResourceType.TEXTURE);
        metadata.photoMetadata = serializer.reference(metadata.photoMetadata, PhotoMetadata.class);
        
        if (version >= 0x15f)
            metadata.referencable = serializer.bool(metadata.referencable);
        if (version >= 0x205)
            metadata.allowEmit = serializer.bool(metadata.allowEmit);
        
        return metadata;
    }

    @Override public int getAllocatedSize() {
        int size = BASE_ALLOCATION_SIZE;
        if (this.nameTranslationTag != null) size += this.nameTranslationTag.length();
        if (this.descTranslationTag != null) size += this.descTranslationTag.length();
        if (this.locationTag != null) size += this.locationTag.length();
        if (this.categoryTag != null) size += this.categoryTag.length();
        if (this.photoMetadata != null) size += this.photoMetadata.getAllocatedSize();
        return size;
    }
}
