package sync;

import cwlib.types.databases.FileDB;

public class Depot 
{
    public long Id;
    public long CommitId;
    public String UniqueIdentifier = "";
    public String DisplayName = "";
    public String Branch = "";
    public int Priority;
    public int Flags;
    public FileDB Database;
    public FileDB WorkingDatabase;
}
