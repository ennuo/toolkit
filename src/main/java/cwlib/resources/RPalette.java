package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

/**
 * Resource that contains a list of RPlan references,
 * generally used for quickly adding a collection of items
 * to your inventory.
 */
public class RPalette implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x20;

    public ArrayList<ResourceDescriptor> planList = new ArrayList<>();
    public int location, description;

    @GsonRevision(min=803)
    public ResourceDescriptor[] convertedPlans;
    
    @SuppressWarnings("unchecked")
    @Override public RPalette serialize(Serializer serializer, Serializable structure) {
        RPalette palette = (structure == null) ? new RPalette() : (RPalette) structure;

        if (serializer.isWriting()) {
            MemoryOutputStream stream = serializer.getOutput();
            stream.i32(palette.planList.size());
            for (ResourceDescriptor descriptor : palette.planList)
                serializer.resource(descriptor, descriptor.getType());
        } else {
            int count = serializer.getInput().i32();
            palette.planList = new ArrayList<>(count);
            for (int i = 0; i < count; ++i)
                palette.planList.add(serializer.resource(null, ResourceType.PLAN));
        }

        palette.location = serializer.i32(palette.location);
        palette.description = serializer.i32(palette.description);

        if (serializer.getRevision().getVersion() >= Revisions.PALETTE_CONVERTED_PLANS) {
            if (serializer.isWriting()) {
                MemoryOutputStream stream = serializer.getOutput();
                stream.i32(palette.convertedPlans.length);
                for (ResourceDescriptor descriptor : palette.convertedPlans)
                    serializer.resource(descriptor, descriptor.getType());
            } else {
                int count = serializer.getInput().i32();
                palette.convertedPlans = new ResourceDescriptor[count];
                for (int i = 0; i < count; ++i)
                    palette.convertedPlans[i] = serializer.resource(null, ResourceType.PLAN);
            }
        }

        return palette;
    }

    @Override public int getAllocatedSize() { 
        return BASE_ALLOCATION_SIZE + (this.planList.size() * 0x24);
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RPalette.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.PALETTE,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }
}
