package cwlib.types.archives;

import java.io.File;
import java.util.ArrayList;

import cwlib.enums.ArchiveType;
import cwlib.types.data.SHA1;

public class FartManyRO extends Fart
{
    private ArrayList<Fart> farts = new ArrayList<>();

    public FartManyRO() { super(null, ArchiveType.MANYRO); }
    
    public void incorporate(File file)
    {
        farts.add(new FileArchive(file));
    }

    @Override public byte[] extract(SHA1 sha1)
    {
        for (Fart fart : farts)
        {
            byte[] fileData = fart.extract(sha1);
            if (fileData != null) return fileData;
        }

        return null;
    }

    @Override public boolean exists(SHA1 sha1)
    {
        for (Fart fart : farts)
        {
            if (fart.exists(sha1))
                return true;
        }

        return false;
    }
    
    @Override public SHA1 add(byte[] data) { throw new UnsupportedOperationException("Can't save data to a readonly cache!"); }
    @Override public SHA1[] add(Fart fart) { throw new UnsupportedOperationException("Can't save data to a readonly cache!"); }
    @Override public boolean save() { throw new UnsupportedOperationException("Can't save a readonly cache!"); }
}
