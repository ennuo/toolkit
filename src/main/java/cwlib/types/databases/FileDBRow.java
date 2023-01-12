package cwlib.types.databases;

import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;

public final class FileDBRow extends FileEntry {
    /**
     * Timestamp for when the resource was last modified.
     */
    private long date;

    /**
     * Creates a FileDBRow using default parameters for FileDB.
     * If one wants to create an entry in a database, they should refer to FileDB.newFileEntry instead.
     * @param database Database that owns this FileDBRow
     * @param path Path of entry in database
     * @param date Timestamp of when entry was last modified
     * @param size Size of entry's data
     * @param sha1 SHA1 signature of entry's data
     * @param guid Unique identifier for entry in database
     */
    public FileDBRow(FileDB database, String path, long date, long size, SHA1 sha1, GUID guid) {
        super(database, path, sha1, size);
        if (sha1 == null)
            throw new NullPointerException("File SHA1 provided to FileDBRow constructor cannot be null!");
        if (guid == null)
            throw new NullPointerException("GUID provided to FileDBRow constructor cannot be null!");
        
        this.path = path;
        this.date = date;
        this.key = guid;
    }

    public FileDB getFileDB() { return (FileDB) this.source; }
    public long getDate() { return this.date; }
    public GUID getGUID() { return (GUID) this.key; }

    public void setDate(long date) { this.date = date; }

    /**
     * Sets the GUID in attached database.
     * @param newGUID New unique identifier for entry in database
     */
    public void setGUID(GUID newGUID) {
        if (newGUID == null || this.source == null) return;
        if (newGUID.equals(this.key)) return;
        FileDB database = this.getFileDB();
        if (database.get(newGUID) != null)
            throw new IllegalArgumentException("GUID already exists in database!");
        database.onGUIDChange(this.getGUID(), newGUID);
        this.key = newGUID;
    }

    /**
     * Sets the GUID in attached database.
     * @param newGUID New unique identifier for entry in database
     */
    public void setGUID(long newGUID) { this.setGUID(new GUID(newGUID)); }
    
    /**
     * Sets the last modified date to the current system time.
     */
    public void updateDate() {
        this.date = System.currentTimeMillis() / 1000; 
    }

    public void setDetails(byte[] data) {
        super.setDetails(data);
        this.updateDate();
    }

    /**
     * Sets entry data from another entry.
     * @param entry Entry to use as base
     */
    public void setDetails(FileDBRow entry) {
        if (entry == null) 
            throw new NullPointerException("Entry cannot be null!");
        super.setDetails(entry);
        this.date = entry.getDate();
        this.setGUID(entry.getGUID());
    }

    @Override public String toString() {
        return String.format("FileDBRow (%s, %s, %s)", this.path, this.sha1, this.key);
    }
}
