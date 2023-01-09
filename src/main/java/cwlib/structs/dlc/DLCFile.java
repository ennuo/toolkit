package cwlib.structs.dlc;

import java.util.ArrayList;

import cwlib.enums.Branch;
import cwlib.enums.DLCFileFlags;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;

public class DLCFile implements Serializable {
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public String directory;
    public String file;
    public String contentID;
    public String inGameCommerceID;

    @GsonRevision(min=0x264)
    public String categoryID;

    public ArrayList<GUID> guids = new ArrayList<>();

    @GsonRevision(lbp3=true, min=0x7fff)
    public ArrayList<GUID> nonPlanGuids = new ArrayList<>();

    public int flags = DLCFileFlags.NONE;

    @GsonRevision(branch=0x4431, min=0x85)
    public int typeMask;

    @SuppressWarnings("unchecked")
    @Override public DLCFile serialize(Serializer serializer, Serializable structure) {
        DLCFile file = (structure == null) ? new DLCFile() : (DLCFile)  structure;
        Revision revision = serializer.getRevision();
        
        file.directory = serializer.str(file.directory);
        file.file = serializer.str(file.file);
        file.contentID = serializer.str(file.contentID);
        file.inGameCommerceID = serializer.str(file.inGameCommerceID);
        if (revision.getVersion() >= 0x264)
            file.categoryID = serializer.str(file.categoryID);
        file.flags = serializer.i32(file.flags);

        if (revision.getSubVersion() > 0x20c) {
            if (serializer.isWriting()) {
                int[] output = new int[file.nonPlanGuids.size()];
                for (int i = 0; i < output.length; ++i) {
                    GUID guid = file.nonPlanGuids.get(i);
                    output[i] = guid == null ? 0 : ((int)guid.getValue());
                }
                serializer.intvector(output);
            } else {
                int[] guids = serializer.intvector(null);
                if (guids != null) {
                    file.nonPlanGuids = new ArrayList<>(guids.length);
                    for (int i = 0; i < guids.length; ++i)
                        file.nonPlanGuids.add(new GUID(guids[i]));
                }
            }
        }

        if (serializer.isWriting()) {
            int[] output = new int[file.guids.size()];
            for (int i = 0; i < output.length; ++i) {
                GUID guid = file.guids.get(i);
                output[i] = guid == null ? 0 : ((int)guid.getValue());
            }
            serializer.intvector(output);
        } else {
            int[] guids = serializer.intvector(null);
            if (guids != null) {
                file.guids = new ArrayList<>(guids.length);
                for (int i = 0; i < guids.length; ++i)
                    file.guids.add(new GUID(guids[i]));
            }
        }

        if (revision.has(Branch.DOUBLE11, 0x85))
            file.typeMask = serializer.i32(file.typeMask);

        return file;
    }

    @Override public int getAllocatedSize() {
        int size = DLCFile.BASE_ALLOCATION_SIZE;
        if (this.directory != null) size += this.directory.length();
        if (this.file != null) size += this.file.length();
        if (this.contentID != null) size += this.contentID.length();
        if (this.inGameCommerceID != null) size += this.inGameCommerceID.length();
        if (this.categoryID != null) size += this.categoryID.length();
        if (this.guids != null)
            size += this.guids.size() * 0x4;
        if (this.nonPlanGuids != null)
            size += this.nonPlanGuids.size() * 0x4;
        return size;
    }
}
