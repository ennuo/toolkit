package cwlib.resources;

import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.structs.slot.Pack;
import cwlib.types.data.Revision;
import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;

public class RPacks implements Serializable {
    public Pack[] packs;

    @SuppressWarnings("unchecked")
    @Override public RPacks serialize(Serializer serializer, Serializable structure) {
        RPacks pack = (structure == null) ? new RPacks() : (RPacks) structure;

        pack.packs = serializer.array(pack.packs, Pack.class);

        return pack;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        int dataSize = 0x1000 * this.packs.length;
        Serializer serializer = new Serializer(dataSize, revision, compressionFlags);
        this.serialize(serializer, this);
        return Resource.compressToResource(serializer.output, ResourceType.PACKS);      
    }    
}