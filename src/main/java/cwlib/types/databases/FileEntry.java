package cwlib.types.databases;

import cwlib.types.data.SHA1;
import cwlib.types.swing.FileData;

public abstract class FileEntry {
    /**
     * The database this entry comes from.
     */
    protected final FileData source;
    
    /**
     * Path of resource in a database.
     */
    protected String path;
    
    /**
     * Size of the associated data buffer.
     */
    protected long size;
    
    /**
     * SHA1 signature of data.
     */
    protected SHA1 sha1;

    /**
     * Creates a FileEntry using default parameters for FileDB.
     * If one wants to create an entry in a database, they should refer to FileDB.newFileEntry instead.
     * @param path Path of entry in database
     * @param source Database that owns this FileEntry
     * @param size Size of entry's data
     * @param sha1 SHA1 signature of entry's data
     */
    public FileEntry(FileData source, String path, SHA1 sha1, long size) {
        if (path == null)
            throw new NullPointerException("Path provided to FileEntry constructor cannot be null!");
        if (source == null)
            throw new NullPointerException("File database provided to FileEntry constructor cannot be null!");
        if (sha1 == null)
            throw new NullPointerException("File SHA1 provided to FileEntry constructor cannot be null!");

        this.source = source;
        this.path = path;
        this.size = size;
        this.sha1 = sha1;
    }
    
    public FileData getSource() { return this.source; }
    public String getPath() { return this.path; }
    public SHA1 getSHA1() { return this.sha1; }
    public long getSize() { return this.size; }

    public void setPath(String path) {
        // Null strings are annoying, plus it works functionally the same anyway.
        if (path == null) this.path = "";
        else this.path = path; 
    }
    public void setSHA1(SHA1 sha1) { this.sha1 = sha1; }
    public void setSize(long size) { this.size = size; }

    /**
     * Sets entry data from buffer (hash, size, cache).
     * @param buffer Buffer to use as base
     */
    public void setDetails(byte[] buffer) {
        if (buffer == null) {
            this.sha1 = new SHA1();
            this.size = 0;
            return;
        }
        this.sha1 = SHA1.fromBuffer(buffer);
        this.size = buffer.length;
    }

    /**
     * Sets entry data from another entry.
     * @param entry Entry to use as base
     */
    public void setDetails(FileEntry entry) {
        if (entry == null) 
            throw new NullPointerException("Entry cannot be null!");
        this.size = entry.getSize();
        this.sha1 = entry.getSHA1();
    }

    @Override public String toString() {
        return String.format("FileEntry (%s, %s)", this.path, this.sha1);
    }
}
