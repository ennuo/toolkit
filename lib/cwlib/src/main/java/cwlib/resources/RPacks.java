package cwlib.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.Revision;
import cwlib.structs.slot.Pack;

/**
 * A resource that's used for DLC slots, it contains
 * costume packs, level kits, etc, these packs are unlocked
 * by the RDLC resource.
 */
public class RPacks implements Resource, Iterable<Pack>
{
    public static final int BASE_ALLOCATION_SIZE = 0x4;

    private ArrayList<Pack> packs = new ArrayList<>();

    public RPacks() { }

    public RPacks(ArrayList<Pack> packs)
    {
        this.packs = packs;
    }

    public RPacks(Pack[] packs)
    {
        this.packs = new ArrayList<Pack>(Arrays.asList(packs));
    }

    @Override
    public void serialize(Serializer serializer)
    {
        packs = serializer.arraylist(packs, Pack.class);
        packs.sort((a, z) -> Long.compareUnsigned(a.timestamp, z.timestamp));
    }

    @Override
    public int getAllocatedSize()
    {
        int size = BASE_ALLOCATION_SIZE;
        if (this.packs != null)
            for (Pack pack : this.packs)
                size += pack.getAllocatedSize();
        return size;
    }

    @Override
    public SerializationData build(Revision revision, byte compressionFlags)
    {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision,
            compressionFlags);
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

    @Override
    public Iterator<Pack> iterator()
    {
        return this.packs.iterator();
    }

    public ArrayList<Pack> getPacks()
    {
        return this.packs;
    }
}