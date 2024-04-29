package cwlib.structs.dlc;

import cwlib.enums.Branch;
import cwlib.enums.DLCFileFlags;
import cwlib.io.Serializable;
import cwlib.io.gson.GsonRevision;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;

import java.util.ArrayList;

public class DLCFile implements Serializable
{
    public static final int BASE_ALLOCATION_SIZE = 0x30;

    public String directory;
    public String file;
    public String contentID;
    public String inGameCommerceID;

    @GsonRevision(min = 0x264)
    public String categoryID;

    public ArrayList<GUID> guids = new ArrayList<>();

    @GsonRevision(lbp3 = true, min = 0x7fff)
    public ArrayList<GUID> nonPlanGuids = new ArrayList<>();

    public int flags = DLCFileFlags.NONE;

    @GsonRevision(branch = 0x4431, min = 0x85)
    public int typeMask;

    @Override
    public void serialize(Serializer serializer)
    {
        Revision revision = serializer.getRevision();

        directory = serializer.str(directory);
        file = serializer.str(file);
        contentID = serializer.str(contentID);
        inGameCommerceID = serializer.str(inGameCommerceID);
        if (revision.getVersion() >= 0x264)
            categoryID = serializer.str(categoryID);
        flags = serializer.i32(flags);

        if (revision.getSubVersion() > 0x20c)
        {
            if (serializer.isWriting())
            {
                int[] output = new int[nonPlanGuids.size()];
                for (int i = 0; i < output.length; ++i)
                {
                    GUID guid = nonPlanGuids.get(i);
                    output[i] = guid == null ? 0 : ((int) guid.getValue());
                }
                serializer.intvector(output);
            }
            else
            {
                int[] guids = serializer.intvector(null);
                if (guids != null)
                {
                    nonPlanGuids = new ArrayList<>(guids.length);
                    for (int i = 0; i < guids.length; ++i)
                        nonPlanGuids.add(new GUID(guids[i]));
                }
            }
        }

        if (serializer.isWriting())
        {
            int[] output = new int[guids.size()];
            for (int i = 0; i < output.length; ++i)
            {
                GUID guid = guids.get(i);
                output[i] = guid == null ? 0 : ((int) guid.getValue());
            }
            serializer.intvector(output);
        }
        else
        {
            int[] guids = serializer.intvector(null);
            if (guids != null)
            {
                this.guids = new ArrayList<>(guids.length);
                for (int i = 0; i < guids.length; ++i)
                    this.guids.add(new GUID(guids[i]));
            }
        }

        if (revision.has(Branch.DOUBLE11, 0x85))
            typeMask = serializer.i32(typeMask);
    }

    @Override
    public int getAllocatedSize()
    {
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
