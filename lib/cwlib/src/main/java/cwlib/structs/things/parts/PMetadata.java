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

public class PMetadata implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x160;

    @GsonRevision(branch = 0, max = 0x296)
    @GsonRevision(branch = 0x4c44, max = 1)
    @Deprecated
    public Value value = new Value();

    @GsonRevision(branch = 0, max = 0x2ba)
    @GsonRevision(branch = 0x4c44, max = 0x7)
    public String nameTranslationTag, locationTag, categoryTag;
    @GsonRevision(max = 0x158)
    public String descTranslationTag;

    @GsonRevision(min = 0x2bb)
    @GsonRevision(branch = 0x4c44, min = 0x8)
    public long titleKey, descriptionKey, location, category;

    @GsonRevision(min = 0x195)
    public int primaryIndex;
    public int fluffCost;
    public EnumSet<InventoryObjectType> type = EnumSet.noneOf(InventoryObjectType.class);
    public int subType;
    public long creationDate;

    public ResourceDescriptor icon;
    public PhotoMetadata photoMetadata;

    @GsonRevision(min = 0x15f)
    public boolean referencable;
    @GsonRevision(min = 0x205)
    public boolean allowEmit;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();
        int version = revision.getVersion();

        boolean hasDepreciatedValue =
            (version < 0x297 && !revision.isLeerdammer()) || (revision.isLeerdammer() && !revision.has(Branch.LEERDAMMER, Revisions.LD_RESOURCES));

        if (hasDepreciatedValue)
            value = serializer.struct(value, Value.class);

        if (revision.has(Branch.LEERDAMMER, Revisions.LD_LAMS_KEYS) || version > 0x2ba)
        {
            titleKey = serializer.u32(titleKey);
            descriptionKey = serializer.u32(descriptionKey);
            location = serializer.u32(location);
            category = serializer.u32(category);
        }
        else
        {
            nameTranslationTag = serializer.str(nameTranslationTag);
            if (version < 0x159)
                descTranslationTag = serializer.str(descTranslationTag);
            else if (!serializer.isWriting())
                descTranslationTag = nameTranslationTag + "_DESC";

            locationTag = serializer.str(locationTag);
            categoryTag = serializer.str(categoryTag);
            if (!serializer.isWriting())
            {
                if (version < 0x159)
                {
                    titleKey =
                        RTranslationTable.makeLamsKeyID(nameTranslationTag);
                    descriptionKey =
                        RTranslationTable.makeLamsKeyID(descTranslationTag);
                }
                else
                {
                    titleKey =
                        RTranslationTable.makeLamsKeyID(nameTranslationTag + "_NAME");
                    titleKey =
                        RTranslationTable.makeLamsKeyID(nameTranslationTag + "_DESC");
                }

                location
                    = RTranslationTable.makeLamsKeyID(locationTag);
                category
                    = RTranslationTable.makeLamsKeyID(categoryTag);
            }
        }

        if (version >= 0x195)
            primaryIndex = serializer.i32(primaryIndex);
        fluffCost = serializer.i32(fluffCost);

        if (hasDepreciatedValue) serializer.i32(0); // unknown

        if (serializer.isWriting())
            serializer.getOutput().i32(InventoryObjectType.getFlags(type));
        else
            type = InventoryObjectType.fromFlags(serializer.getInput().i32(),
                serializer.getRevision());

        subType = serializer.i32(subType);
        creationDate = serializer.u32(creationDate);

        icon = serializer.resource(icon, ResourceType.TEXTURE);
        photoMetadata = serializer.reference(photoMetadata, PhotoMetadata.class);

        if (version >= 0x15f)
            referencable = serializer.bool(referencable);
        if (version >= 0x205)
            allowEmit = serializer.bool(allowEmit);
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.nameTranslationTag != null) size += this.nameTranslationTag.length();
        if (this.descTranslationTag != null) size += this.descTranslationTag.length();
        if (this.locationTag != null) size += this.locationTag.length();
        if (this.categoryTag != null) size += this.categoryTag.length();
        if (this.photoMetadata != null) size += this.photoMetadata.getAllocatedSize();
        return size;
    }
}
