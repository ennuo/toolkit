package cwlib.types.archives;

import cwlib.enums.ArchiveType;
import cwlib.types.Resource;
import cwlib.types.data.SHA1;
import cwlib.io.Serializable;
import cwlib.io.streams.MemoryOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Base class for archive resources.
 */
public abstract class Fart implements Iterable<Fat> {
    /**
     * Archive path on local disk.
     */
    protected final File file;

    /**
     * The last time this archive was modified,
     * used for checking if we need to refresh
     * the FAT table.
     */
    protected long lastModified;

    /**
     * Offset of the FAT table.
     */
    protected long fatOffset;

    /**
     * Type of FileArchive
     */
    private final ArchiveType type;

    /**
     * Queue mapping for entries to be added on save.
     */
    protected HashMap<SHA1, byte[]> queue = new HashMap<>(32);

    protected Fat[] entries;

    /**
     * Structure to map SHA1s to their respective entries in the archive,
     * so access is constant time.
     */
    protected HashMap<SHA1, Fat> lookup = new HashMap<>();

    protected Fart(File file, ArchiveType type) {
        // Only save archives can have null paths
        if (file == null && type != ArchiveType.SAVE)
            throw new NullPointerException("Archive path cannot be null!");
        if (type == null)
            throw new NullPointerException("Archive type cannot be null!");
        this.type = type;
        this.file = file;
        if (this.file != null)
            this.lastModified = this.file.lastModified();
    }

    /**
     * Extracts a resource from the archive via SHA1.
     * @param sha1 SHA1 signature of resource to extract
     * @return Extracted resource
     */
    public byte[] extract(SHA1 sha1) { 
        if (sha1 == null)
            throw new NullPointerException("Can't search for null hash in archive!");
        
        // Grab the resource from the queue if it exists
        if (this.queue.containsKey(sha1))
            return this.queue.get(sha1);

        if (this.lookup.containsKey(sha1))
            return this.extract(this.lookup.get(sha1));
        
        return null;
    }

    /**
     * Extracts a resource via a FAT entry.
     * @param fat FAT row to extract
     * @return Extracted resource
     */
    public byte[] extract(Fat fat) {
        if (fat == null)
            throw new NullPointerException("Can\'t search for null entry in archive!");
        if (fat.getFileArchive() != this)
            throw new IllegalArgumentException("This entry does not belong to this archive!");
        try (RandomAccessFile archive = new RandomAccessFile(this.file.getAbsolutePath(), "r")) {
            byte[] buffer = new byte[fat.getSize()];
            archive.seek(fat.getOffset());
            archive.read(buffer);
            return buffer;
        } catch (IOException ex) { return null; }
    }

    /**
     * Checks if a hash exists in the archive.
     * @param sha1 Hash to query
     * @return Whether or not the hash exists
     */
    public boolean exists(SHA1 sha1) { 
        if (sha1 == null)
            throw new NullPointerException("Can't search for null hash in archive!");
        return this.lookup.containsKey(sha1) || this.queue.containsKey(sha1); 
    }

    /**
     * Adds a buffer to the archive.
     * @param data Data to add
     * @return SHA1 hash of data added
     */
    public SHA1 add(byte[] data) {
        if (data == null) 
            throw new NullPointerException("Can't add null buffer to archive!");
        SHA1 sha1 = SHA1.fromBuffer(data);

        // Already exists, no point adding it to the queue.
        if (this.exists(sha1)) return sha1;

        this.queue.put(sha1, data);

        return sha1;
    }

    /**
     * Adds the contents of another archive to this one.
     * @param fart Archive containing data to add
     * @return Hashes added
     */
    public SHA1[] add(Fart fart) {
        ArrayList<SHA1> hashes = new ArrayList<>(fart.entries.length);
        for (Fat fat : fart.entries) {
            SHA1 sha1 = fat.getSHA1();
            if (this.exists(sha1))
                continue;
            // Don't use the add method since it hashes the data again.
            this.queue.put(sha1, fart.extract(fat));
            hashes.add(sha1);
        }
        return hashes.toArray(SHA1[]::new);
    }

    /**
     * Flushes changes to the archive.
     * @return Whether or not the operation was successful.
     */
    public abstract boolean save();

    public ArchiveType getArchiveType() { return this.type; }
    public File getFile() { return this.file; }

    /**
     * Gets the accumulated size of the data in the queue.
     * @return Queue size
     */
    public long getQueueSize() {
        return this.queue.values()
            .stream()
            .mapToLong(data -> data.length)
            .reduce(0, (p, c) -> p + c);
    }

    /**
     * Gets a list of all hashes queued to be saved.
     * @return All hashes currently in queue
     */
    public ArrayList<SHA1> getQueueHashes() {
        return new ArrayList<SHA1>(this.queue.keySet());
    }

    /**
     * Generates a FAT buffer.
     * @param fat Fat array
     * @return Generated buffer
     */
    protected static byte[] generateFAT(Fat[] fat) {
        MemoryOutputStream stream = new MemoryOutputStream(fat.length * 0x1c);
        for (Fat entry : fat) {
            stream.sha1(entry.getSHA1());
            stream.u32(entry.getOffset());
            stream.i32(entry.getSize());
        }
        return stream.getBuffer();
    }

    /**
     * Deserializes a resource extracted from this archive.
     * @param <T> Resource type that implements Serializable
     * @param hash Hash of resource to extract
     * @param clazz Resource class reference that implements Serializable
     * @return Deserialized resource
     */
    public <T extends Serializable> T loadResource(SHA1 hash, Class<T> clazz) {
        byte[] data = this.extract(hash);
        if (data == null) return null;
        Resource resource = new Resource(data);
        return resource.loadResource(clazz);
    }

    /**
     * Checks if the archive has been modified since
     * being loaded.
     * @return Whether or not the archive has been modified
     */
    public boolean wasModified() {
        if (this.file == null) return true;
        if (!this.file.exists()) return true;
        return this.file.lastModified() != this.lastModified;
    }

    /**
     * Validates that all SHA1s match their corresponding buffers in FAT
     * @return Number of entries that failed validation
     */
    public int validate() {
        ArrayList<Fat> entries = new ArrayList<>(this.entries.length);
        for (Fat fat : this.entries) {
            SHA1 sha1 = SHA1.fromBuffer(fat.extract());
            if (sha1.equals(fat.getSHA1()))
                entries.add(fat);
        }
        int missing = this.entries.length - entries.size();
        this.entries = entries.toArray(Fat[]::new);
        return missing;
    }

    /**
     * Checks if the archive contains data to save.
     * @return Whether or not the archive should save.
     */
    public boolean shouldSave() {
        return this.queue.size() != 0;
    }
    
    /**
     * Gets number of entries in FAT.
     * @return Number of entries.
     */
    public int getEntryCount() { return this.entries.length; }

    @Override public Iterator<Fat> iterator() {
        return Arrays.stream(this.entries).iterator();
    }
}
