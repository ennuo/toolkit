package cwlib.types.archives;

import cwlib.types.data.SHA1;

/**
 * Represents a row in a FileArchive.
 */
public final class Fat {
    /**
     * The archive this entry comes from.
     */
    private final Fart archive;

    /**
     * SHA1 signature of data.
     */
    private final SHA1 sha1;

    /**
     * Offset of the data in the archive.
     */
    private final long offset;

    /**
     * Size of the associated data buffer.
     */
    private final int size;

    public Fat(Fart archive, SHA1 sha1, long offset, int size) {
        if (archive == null)
            throw new NullPointerException("File archive provided to Fat cannot be null!");
        if (sha1 == null)
            throw new NullPointerException("File SHA1 provided to Fat cannot be null!");
        
        this.archive = archive;
        this.sha1 = sha1;
        this.offset = offset;
        this.size = size;
    }

    public SHA1 getSHA1() { return this.sha1; }
    public long getOffset() { return this.offset; }
    public int getSize() { return this.size; }
    public Fart getFileArchive() { return this.archive; }

    /**
     * Extracts this entry from the associated archive.
     * @return Extracted resource.
     */
    public byte[] extract() { return this.archive.extract(this); }

    @Override public String toString() {
        return String.format("Fat (%h, offset=%d, size=%d)", 
            this.sha1, this.offset, this.size);
    }
}
