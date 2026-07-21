package sync;

import cwlib.io.Serializable;
import cwlib.io.serializer.Serializer;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDBRow;

public class CommitFile implements Serializable
{
    public String path;
    public GUID fileGuid;
    public int fileSize;
    public SHA1 fileHash = SHA1.EMPTY;
    
    private CommitFile()
    {

    }

    public static CommitFile fromRow(FileDBRow row)
    {
        var file = new CommitFile();
        file.path = row.getPath();
        file.fileGuid = row.getGUID();
        file.fileSize = (int)row.getSize();
        file.fileHash = row.getSHA1();
        return file;
    }

    public static CommitFile createDeleted(GUID guid)
    {
        var file = new CommitFile();
        file.path = "";
        file.fileGuid = guid;
        file.fileSize = -1;
        return file;
    }

    public boolean IsDeletedFile()
    {
        return fileSize == -1;
    }

    @Override public void serialize(Serializer serializer) 
    {
        fileGuid = serializer.guid(fileGuid);
        fileSize = serializer.i32(fileSize);
        if (IsDeletedFile()) return;
        fileHash = serializer.sha1(fileHash);
        path = serializer.str(path);
    }
    
    @Override public int getAllocatedSize() 
    {
        return 0x20 + path != null ? path.length() : 0;
    }

}
