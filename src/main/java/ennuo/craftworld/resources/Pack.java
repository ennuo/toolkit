package ennuo.craftworld.resources;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.PackItem;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.serializer.Serializable;
import ennuo.craftworld.serializer.Serializer;

public class Pack implements Serializable {
    public PackItem[] packs;

    @SuppressWarnings("unchecked")
    @Override public Pack serialize(Serializer serializer, Serializable structure) {
        Pack pack = (structure == null) ? new Pack() : (Pack) structure;

        pack.packs = serializer.array(pack.packs, PackItem.class);

        return pack;
    }
    
    public byte[] build(Revision revision, byte compressionFlags) {
        int dataSize = 0x1000 * this.packs.length;
        Serializer serializer = new Serializer(dataSize, revision, compressionFlags);
        this.serialize(serializer, this);
        return Resource.compressToResource(serializer.output, ResourceType.PACKS);      
    }    
}