package sync;

import java.util.ArrayList;
import java.util.HashMap;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.SHA1;

public class CommitInfo implements Serializable
{
    public long id;
    public int numChanges, numAdditions, numDeletions;
    public ArrayList<CommitFile> files = new ArrayList<>();
    public HashMap<SHA1, String> loose = new HashMap<>();

    public CommitFile find(SHA1 hash)
    {
        for (var file : files)
        {
            if (file.fileHash != null && file.fileHash.equals(hash))
                return file;
        }

        return null;
    }
    
    @Override public void serialize(Serializer serializer) 
    {
        id = serializer.u64(id);
        files = serializer.arraylist(files, CommitFile.class);
    }

    @Override public int getAllocatedSize() 
    {
        int size = 0x10;
        for (var file : files)
            size += file.getAllocatedSize();
        return size;
    }
}
