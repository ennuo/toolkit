package cwlib.resources;

import java.util.ArrayList;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.io.Compressable;
import cwlib.io.Serializable;
import cwlib.io.serializer.SerializationData;
import cwlib.io.serializer.Serializer;
import cwlib.structs.dlc.DLCFile;
import cwlib.structs.dlc.DLCGUID;
import cwlib.types.data.Revision;

public class RDLC implements Serializable, Compressable {
    public static final int BASE_ALLOCATION_SIZE = 0x10;

    public ArrayList<DLCGUID> guids = new ArrayList<>();
    public ArrayList<DLCFile> files = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override public RDLC serialize(Serializer serializer, Serializable structure) {
        RDLC dlc = (structure == null) ? new RDLC() : (RDLC) structure;

        dlc.guids = serializer.arraylist(dlc.guids, DLCGUID.class);
        dlc.files = serializer.arraylist(dlc.files, DLCFile.class);

        return dlc;
    }

    @Override public SerializationData build(Revision revision, byte compressionFlags) {
        Serializer serializer = new Serializer(this.getAllocatedSize(), revision, compressionFlags);
        serializer.struct(this, RDLC.class);
        return new SerializationData(
            serializer.getBuffer(), 
            revision, 
            compressionFlags,
            ResourceType.DOWNLOADABLE_CONTENT,
            SerializationType.BINARY, 
            serializer.getDependencies()
        );
    }

    @Override public int getAllocatedSize() { 
        int size = RDLC.BASE_ALLOCATION_SIZE;
        if (this.guids != null) size += (this.guids.size() * DLCGUID.BASE_ALLOCATION_SIZE);
        if (this.files != null) {
            for (DLCFile file : this.files)
                size += file.getAllocatedSize();
        }
        return size;
    }    
}
