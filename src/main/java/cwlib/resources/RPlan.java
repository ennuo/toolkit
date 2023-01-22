package cwlib.resources;

import java.util.HashSet;

import org.joml.Matrix4f;

import cwlib.enums.Branch;
import cwlib.enums.CompressionFlags;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PMetadata;
import cwlib.structs.things.parts.PPos;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;

/**
 * Represents a "plan" to create an item,
 * often used for inventory items.
 */
public class RPlan implements Compressable, Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    /**
     * Cache of dependencies in thing data
     */
    public HashSet<ResourceDescriptor> dependencyCache = new HashSet<>();

    public boolean isUsedForStreaming = false;
    public Revision revision = new Revision(Revision.LBP1_FINAL_REVISION, 0x4c44, 0x17);
    public byte[] thingData;
    public InventoryItemDetails inventoryData = new InventoryItemDetails();

    /**
     * Compression flags used during the loading of the thing data.
     */
    public byte compressionFlags = CompressionFlags.USE_ALL_COMPRESSION;

    public RPlan() {};
    public RPlan(Revision revision, byte compressionFlags, Thing thing, PMetadata metadata) {
        this.revision = revision;
        this.compressionFlags = compressionFlags;
        this.setThing(thing);
        this.inventoryData = new InventoryItemDetails(metadata);
    }

    public RPlan(Revision revision, byte compressionFlags, Thing[] things, PMetadata metadata) {
        this.revision = revision;
        this.compressionFlags = compressionFlags;
        this.setThings(things);
        this.inventoryData = new InventoryItemDetails(metadata);
    }

    public RPlan(Revision revision, byte compressionFlags, Thing thing, InventoryItemDetails details) {
        this.revision = revision;
        this.compressionFlags = compressionFlags;
        this.setThing(thing);
        this.inventoryData = details;
    }

    public RPlan(Revision revision, byte compressionFlags, Thing[] things, InventoryItemDetails details) {
        this.revision = revision;
        this.compressionFlags = compressionFlags;
        this.setThings(things);
        this.inventoryData = details;
    }

    @SuppressWarnings("unchecked")
    @Override public RPlan serialize(Serializer serializer, Serializable structure) {
        RPlan plan = (structure == null) ? new RPlan() : (RPlan) structure;

        Revision revision = serializer.getRevision();
        int head = revision.getVersion();
        
        // Keep track of dependencies in thing data
        if (!serializer.isWriting()) {
            for (ResourceDescriptor descriptor : serializer.getDependencies())
                plan.dependencyCache.add(descriptor);
            serializer.clearDependencies();
        }

        /* We'll use this when we get the thing data. */
        if (!serializer.isWriting()) {
            plan.compressionFlags = serializer.getCompressionFlags();
            plan.revision = revision;
        }

        if (revision.getSubVersion() >= Revisions.STREAMING_PLAN)
            plan.isUsedForStreaming = serializer.bool(plan.isUsedForStreaming);

        /* Ignore the plan revision, use the resource revision. */
        if (serializer.isWriting())
            serializer.i32(serializer.getRevision().getHead());
        else 
            serializer.getInput().i32();
        
        plan.thingData = serializer.bytearray(plan.thingData);
        if (head >= Revisions.PLAN_DETAILS && !plan.isUsedForStreaming) {
            plan.inventoryData = serializer.struct(plan.inventoryData, InventoryItemDetails.class);
            if (revision.has(Branch.LEERDAMMER, Revisions.LD_LAMS_KEYS) || head >= Revisions.LAMS_KEYS) {
                plan.inventoryData.location = serializer.u32(plan.inventoryData.location);
                plan.inventoryData.category = serializer.u32(plan.inventoryData.category);
            } else {
                plan.inventoryData.locationTag = serializer.str(plan.inventoryData.locationTag);
                plan.inventoryData.categoryTag = serializer.str(plan.inventoryData.categoryTag);
                if (!serializer.isWriting()) {
                    plan.inventoryData.location = RTranslationTable.makeLamsKeyID(plan.inventoryData.locationTag);
                    plan.inventoryData.category = RTranslationTable.makeLamsKeyID(plan.inventoryData.categoryTag);
                }
            }
        }

        // Remove dependencies that'll be re-added after writing
        if (!serializer.isWriting()) {
            for (ResourceDescriptor descriptor : serializer.getDependencies())
                plan.dependencyCache.remove(descriptor);
            serializer.clearDependencies();
        }

        return plan;
    }

    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        if (this.thingData != null)
            size += this.thingData.length;
        if (this.inventoryData != null)
            size += this.inventoryData.getAllocatedSize();
        return size;
    }

    public SerializationData build() { return this.build(this.revision, this.compressionFlags); }
    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize() + 0x8000, revision, compressionFlags);
        serializer.struct(this, RPlan.class);
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

    /**
     * Parses the thing data buffer.
     * @return Things
     */
    public Thing[] getThings() {
        Serializer serializer = new Serializer(this.thingData, this.revision, this.compressionFlags);
        Thing[] things = serializer.array(null, Thing.class, true);

        // Fixup local positions in revisions where they were removed
        // from serialization.
        if (this.revision.getVersion() >= 0x341) {
            for (Thing thing : things) {
                if (thing == null || thing.parent == null) continue;

                PPos pos = thing.getPart(Part.POS);
                if (pos == null) continue;

                PPos parent = thing.parent.getPart(Part.POS);

                // This generally shouldn't happen, but make sure to check it anyway
                if (parent == null) continue;

                Matrix4f inv = parent.worldPosition.invert(new Matrix4f());
                pos.localPosition = inv.mul(pos.worldPosition);
            }
        }

        return things;
    }

    /**
     * Sets the ThingData buffer from an array of things,
     * serializer uses RPlan's current revision and compressionFlags.
     * @param things Thing array to set
     */
    public void setThings(Thing[] things) {
        // {
        //     // This is terribly inefficient, but whatever
        //     Serializer serializer = new Serializer(0x800000, this.revision, this.compressionFlags);
        //     serializer.array(things, Thing.class, true);
        //     things = serializer.getThings();
        // }
        
        Serializer serializer = new Serializer(0x800000, this.revision, this.compressionFlags);
        serializer.array(things, Thing.class, true);
        this.thingData = serializer.getBuffer();
        
        ResourceDescriptor[] dependencies = serializer.getDependencies();
        this.dependencyCache.clear();
        for (ResourceDescriptor dependency : dependencies)
            this.dependencyCache.add(dependency);
    }

    public void setThing(Thing thing) {
        // This is terribly inefficient, but whatever
        Serializer serializer = new Serializer(0x800000, this.revision, this.compressionFlags);
        serializer.reference(thing, Thing.class);
        Thing[] things = serializer.getThings();

        serializer = new Serializer(0x800000, this.revision, this.compressionFlags);   
        serializer.array(things, Thing.class, true);

        this.thingData = serializer.getBuffer();

        ResourceDescriptor[] dependencies = serializer.getDependencies();
        this.dependencyCache.clear();
        for (ResourceDescriptor dependency : dependencies)
            this.dependencyCache.add(dependency);
    }
}
