package ennuo.craftworld.types.databases;

import ennuo.craftworld.resources.structs.GUID;
import ennuo.craftworld.resources.structs.SHA1;

public final class FileEntry {
    /**
     * The database this entry comes from.
     */
    private final FileDB database;

    /**
     * Path of resource in a database.
     */
    private String path;
    
    /**
     * Timestamp for when the resource was last modified.
     */
    private long lastModified;
    
    /**
     * Size of the associated data buffer.
     */
    private long size;
    
    /**
     * SHA1 signature of data.
     */
    private SHA1 sha1;

    /**
     * Unique identifier for resource in a database.
     */
    private GUID guid;

    /**
     * Creates a FileEntry using default parameters for FileDB.
     * If one wants to create an entry in a database, they should refer to FileDB.newFileEntry instead.
     * @param database Database that owns this FileEntry
     * @param path Path of entry in database
     * @param lastModified Timestamp of when entry was last modified
     * @param size Size of entry's data
     * @param sha1 SHA1 signature of entry's data
     * @param guid Unique identifier for entry in database
     */
    public FileEntry(FileDB database, String path, long lastModified, long size, SHA1 sha1, GUID guid) {
        if (database == null)
            throw new NullPointerException("File database provided to FileEntry constructor cannot be null!");
        if (path == null)
            throw new NullPointerException("File path provided to FileEntry constructor cannot be null!");
        if (sha1 == null)
            throw new NullPointerException("File SHA1 provided to FileEntry constructor cannot be null!");
        if (guid == null)
            throw new NullPointerException("GUID provided to FileEntry constructor cannot be null!");

        this.database = database;
        this.path = path;
        this.lastModified = lastModified;
        this.size = size;
        this.sha1 = sha1;
        this.guid = guid;
    }

    /**
     * Sets the last modified date to the current system time.
     */
    public void updateLastModified() {
        this.lastModified = System.currentTimeMillis() / 1000; 
    }

    public String getPath() { return this.path; }
    public long getLastModified() { return this.lastModified; }
    public long getSize() { return this.size; }
    public SHA1 getSHA1() { return this.sha1; }
    public GUID getGUID() { return this.guid; }
    public FileDB getFileDB() { return this.database; }

    public void setPath(String path) { this.path = path; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    public void setSize(long size) { this.size = size; }
    public void setSHA1(SHA1 sha1) { this.sha1 = sha1; }

    /**
     * Sets the GUID in attached database.
     * @param newGUID New unique identifier for entry in database
     */
    public void setGUID(GUID newGUID) {
        if (newGUID == null || this.database == null) return;
        if (newGUID.equals(this.guid)) return;
        if (this.database.get(newGUID) != null)
            throw new IllegalArgumentException("GUID already exists in database!");
        database.onGUIDChange(this.guid, newGUID);
        this.guid = newGUID;
    }

    /**
     * Sets the GUID in attached database.
     * @param newGUID New unique identifier for entry in database
     */
    public void setGUID(long newGUID) { this.setGUID(new GUID(newGUID)); }

    /**
     * Sets entry data from buffer (hash, size, cache).
     * @param buffer Buffer to use as base
     */
    public void setDetails(byte[] buffer) {
        if (buffer == null) {
            this.sha1 = SHA1.empty();
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
        this.path = entry.getPath();
        this.lastModified = entry.getLastModified();
        this.size = entry.getSize();
        this.sha1 = entry.getSHA1();
        this.setGUID(entry.getGUID());
    }

    /**
     * Removes this FileEntry from attached database.
     */
    public void remove() { this.database.remove(this.guid); }

    @Override public String toString() {
        return String.format("FileEntry (%s, %s)", this.path, this.guid);
    }
}
