package cwlib.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;
import cwlib.structs.slot.Pack;

/**
 * A resource that's used for DLC slots, it contains
 * costume packs, level kits, etc, these packs are unlocked
 * by the RDLC resource.
 */
public class RPacks implements Compressable, Serializable, Iterable<Pack> {
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    private ArrayList<Pack> packs = new ArrayList<>();

    public RPacks() {}
    public RPacks(ArrayList<Pack> packs) { this.packs = packs; }
    public RPacks(Pack[] packs) {
        this.packs = new ArrayList<Pack>(Arrays.asList(packs));
    }

    @SuppressWarnings("unchecked")
    @Override public RPacks serialize(Serializer serializer, Serializable structure) {
        RPacks packs = (structure == null) ? new RPacks() : (RPacks) structure;

        packs.packs = serializer.arraylist(packs.packs, Pack.class);

        return packs;
    }

    @Override public int getAllocatedSize() { 
        int size = BASE_ALLOCATION_SIZE;
        if (this.packs != null)
            for (Pack pack : this.packs)
                size += pack.getAllocatedSize();
        return size;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RPacks.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.PACKS,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    @Override public Iterator<Pack> iterator() { return this.packs.iterator(); }

    public ArrayList<Pack> getPacks() { return this.packs; }
}