package cwlib.types.archives;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import cwlib.enums.ArchiveType;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.util.Bytes;
import cwlib.util.Crypto;
import cwlib.util.FileIO;

/**
 * The archive used for save files
 * and also streaming chunks in LBP3.
 */
public class SaveArchive extends Fart {
    /**
     * The game revision this archive was
     * compiled on.
     */
    private Revision gameRevision = new Revision(0x272, 0x4c44, 0x0017);
    
    /**
     * Local user index that owns this save.
     */
    private int localUserID;

    /**
     * Key used for keeping track of the
     * "primary" resource for the save.
     * RBigProfile in bigfarts,
     * RLocalProfile in littlefarts.
     */
    private SaveKey key = new SaveKey();

    /**
     * Signature used to verify a profile backup
     * hasn't been tampered with.
     */
    private SHA1 hashinate = new SHA1();

    /**
     * The revision of this archive.
     */
    private final int archiveRevision;

    /**
     * PS4/Vita archives have some values written
     * in little endian rather than big endian,
     * so we should keep track of that.
     */
    private boolean isLittleEndian;

    /**
     * The ID of this archive, the ID is generally
     * just the number of changes, this is also only used
     * in FAR version 5.
     */
    private int ID = 1;

    /**
     * The ID of the backup of this archive,
     * this is only used in FAR version 5.
     */
    private int backupID = 0;

    /**
     * List of IDs of the fragments of this archive,
     * only used in FAR version 5 which supports split archives.
     */
    private int[] fragmentIDs = new int[0];

    /**
     * Storage of all resources in-memory.
     */
    private HashMap<Fat, byte[]> buffers = new HashMap<>();

    /**
     * Creates empty save archive.
     * @param file Where this file will get saved
     * @param gameRevision Game revision this archive is built for
     * @param revision Save archive revision
     */
    public SaveArchive(Revision gameRevision, int revision) {
        super(null, ArchiveType.SAVE);
        // FAR1 is considered FARC
        if (revision < 2 || revision > 5)
            throw new IllegalArgumentException("Invalid revision!");
        if (revision == 5)
            this.isLittleEndian = true;
        this.gameRevision = gameRevision;
        this.archiveRevision = revision;
        this.entries = new Fat[0];
    }

    public SaveArchive(byte[] data) {
        this(data, null);
    }

    public SaveArchive(File file) {
        this(FileIO.read(file.getAbsolutePath()), file);
    }


    private SaveArchive(byte[] data, File file) {
        super(file, ArchiveType.SAVE);

        if (file != null && !file.exists())
            throw new SerializationException("Save archive specified doesn't exist!");

        MemoryInputStream stream = new MemoryInputStream(data);
        if (stream.getLength() < 0x8)
            throw new SerializationException("Invalid SaveArchive, size is less than minimum of 8 bytes!");

        stream.seek(0x8, SeekMode.End);
        int entryCount = stream.i32();
        int magic = stream.i32();

        if ((magic >> 8) != 0x464152)
            throw new SerializationException("Invalid SaveArchive, magic does not match!");
        try { this.archiveRevision = Integer.parseInt(Character.toString((char) magic & 0xFF)); }
        catch (NumberFormatException ex) {
            throw new SerializationException("Invalid SaveArchive revision!");
        }

        int fragments = 0;
        if (this.archiveRevision == 5) {
            // Only the Vita uses FAR revision 5, so it's always little endian as a result.
            this.isLittleEndian = true;

            // Temporarily set stream to little endian,
            // so we can read the value.
            stream.setLittleEndian(true);

            stream.seek(0xC, SeekMode.End);
            fragments = stream.i32();

            stream.setLittleEndian(false);
        }

        // FAR3 added HASHINATE
        if (this.archiveRevision > 2) {
            if (this.archiveRevision == 5)
                stream.seek(0x20, SeekMode.End);
            else
                stream.seek(0x1c, SeekMode.End);
            this.hashinate = stream.sha1();
        }

        this.fatOffset = stream.getLength() - 0x8 - (entryCount * 0x1c);
        if (this.archiveRevision > 2) this.fatOffset -= 0x14;
        if (this.archiveRevision == 5) this.fatOffset -= 4;

        int saveKeySize = 0x80;
        // FAR4 added BRANCH DESCRIPTION
        if (this.archiveRevision > 3)
            saveKeySize += 0x4;
        if (this.archiveRevision == 5)
            saveKeySize += (0x8 + (0x4 * fragments));
        int saveKeyOffset = (int) (this.fatOffset - saveKeySize);

        stream.seek(saveKeyOffset, SeekMode.Begin);

        // Local User ID's on PS4 use a hash rather than an incremental index
        // So this byte will always be set in some way on PS4.
        if (this.archiveRevision != 5 && stream.getBuffer()[saveKeyOffset + 0x8] != 0)
            this.isLittleEndian = true;

        if (this.isLittleEndian) stream.setLittleEndian(true);

        if (this.archiveRevision > 3)
            this.gameRevision = new Revision(stream.i32(), stream.i32());
        else
            this.gameRevision = new Revision(stream.i32());

        if (this.archiveRevision == 5) {
            this.ID = stream.i32();
            this.backupID = stream.i32();
            this.fragmentIDs = new int[fragments];
            for (int i = 0; i < fragments; ++i)
                this.fragmentIDs[i] = stream.i32();
        }

        this.localUserID = stream.i32();

        stream.seek(0x4 * 0xA); // deprecated1 int[10]
        this.key.setCopied(stream.i32() == 1); // Boolean is padded
        this.key.setRootType(ResourceType.fromType(stream.i32()));
        stream.seek(0x4 * 0x3); // deprecated2 int[3]
        this.key.setRootHash(stream.sha1());
        stream.seek(0x4 * 0xA); // deprecated3 int[10]

        // Only certain parts are little endian because they were directly written to memory,
        // whereas most other data structures are made to use the same endian across architectures.
        stream.setLittleEndian(false);

        this.entries = new Fat[entryCount];
        this.lookup = new HashMap<>(entryCount);
        for (int i = 0; i < entryCount; ++i) {
            Fat fat = new Fat(this, stream.sha1(), stream.u32(), stream.i32());
            this.entries[i] = fat;
            this.lookup.put(fat.getSHA1(), fat);
        }

        // Store all the resources in-memory, saves
        // are small enough that this will rarely be an issue.
        this.buffers = new HashMap<>(entryCount);
        for (Fat fat : this.entries) {
            stream.seek((int) fat.getOffset(), SeekMode.Begin);
            this.buffers.put(fat, stream.bytes(fat.getSize()));
        }
    }

    public SaveArchive(String path) { this(new File(path)); }

    @Override public byte[] extract(Fat fat) {
        if (fat == null)
            throw new NullPointerException("Can\'t search for null entry in archive!");
        if (fat.getFileArchive() != this)
            throw new IllegalArgumentException("This entry does not belong to this archive!");
        return this.buffers.get(fat);
    }
    
    /**
     * Generates a save key buffer from the current state of the archive.
     * @return Save key buffer
     */
    private byte[] generateSaveKey() {
        int saveKeySize = 0x80;
        if (this.archiveRevision > 3)
            saveKeySize += 0x4;
        if (this.archiveRevision == 5)
            saveKeySize += (0x8 + (0x4 * this.fragmentIDs.length));

        // Construct the save key
        MemoryOutputStream keyStream = new MemoryOutputStream(saveKeySize);
        if (this.isLittleEndian) keyStream.setLittleEndian(true);

        int branch = 
            ((int)this.gameRevision.getBranchID() << 16) |
            ((int)this.gameRevision.getBranchRevision());
        
        keyStream.i32(this.gameRevision.getHead());
        if (this.archiveRevision > 3)
            keyStream.i32(branch);
        
        if (this.archiveRevision == 5) {
            keyStream.i32(this.ID);
            keyStream.i32(this.backupID);
            for (int fragment : this.fragmentIDs)
                keyStream.i32(fragment);
        }

        keyStream.i32(this.localUserID);
        keyStream.pad(0x4 * 0xa); // deprecated1 int[10]
        keyStream.i32(this.key.getCopied() ? 1 : 0);
        keyStream.i32(this.key.getRootType().getValue());
        keyStream.pad(0x4 * 0x3); // deprecated2 int[3]
        keyStream.sha1(this.key.getRootHash());
        keyStream.pad(0x4 * 0xa); // deprecated3 int[10]

        return keyStream.getBuffer();
    }

    /**
     * Gets a list of all resource hashes that are depended on by
     * at least one other resource.
     * @param resource Resource to dependinate
     * @param hashes List of hashes that are depended on
     */
    public void getFilterList(byte[] resource, HashSet<SHA1> hashes) {
        MemoryInputStream stream = new MemoryInputStream(resource);

        // Parsing the resource again here because the main class will
        // auto-decompress the handle and do additional checks we don't
        // care about here.

        ResourceType type = ResourceType.fromMagic(stream.str(3));
        if (type == ResourceType.INVALID) return;
        SerializationType method = SerializationType.fromValue(stream.str(1));
        if (method != SerializationType.BINARY && method != SerializationType.ENCRYPTED_BINARY)
            return;
        int revision = stream.i32();

        // Dependency table wasn't added until 0x109
        if (revision < 0x109) return;

        stream.seek(stream.i32(), SeekMode.Begin);
        int count = stream.i32();
        for (int i = 0; i < count; ++i) {
            byte flags = stream.i8();
            if ((flags & 2) != 0) stream.guid();
            if ((flags & 1) != 0) {
                SHA1 sha1 = stream.sha1();
                if (this.exists(sha1) && !hashes.contains(sha1)) {
                    hashes.add(sha1);
                    byte[] data = this.extract(sha1);
                    if (data != null)
                        this.getFilterList(data, hashes);
                }
            }
            stream.i32(); // ResourceType
        }
    }

    /**
     * Builds this archive and returns the resulting byte array.
     * @return Built archive
     */
    public byte[] build(boolean hashinate) {
        // If the root exists, use it to filter what
        // resources are actually necessary.
        SHA1 rootHash = this.key.getRootHash();
        byte[] root = this.extract(rootHash);
        HashSet<SHA1> resources = new HashSet<>(this.entries.length + this.queue.size());
        if (root != null) {
            resources.add(rootHash);
            this.getFilterList(root, resources);
        } else {
            // If the root doesn't exist, or there just is no root,
            // just serialize everything.
            for (Fat fat : this.entries)
                resources.add(fat.getSHA1());
            for (SHA1 hash : this.queue.keySet())
                resources.add(hash);
        }

        SHA1[] hashes = resources.toArray(SHA1[]::new);
        Arrays.sort(hashes, (e1, e2) -> e1.toString().compareTo(e2.toString()));
        
        byte[][] buffers = new byte[hashes.length][];
        Fat[] entries = new Fat[hashes.length];

        int size = 0;
        for (int i = 0; i < hashes.length; ++i) {
            byte[] buffer = this.extract(hashes[i]);
            entries[i] = new Fat(this, hashes[i], size, buffer.length);
            buffers[i] = buffer;
            size += buffer.length;
        }

        byte[] saveKey = this.generateSaveKey();
        byte[] fat = Fart.generateFAT(entries);

        // Save key has to be aligned at 4 byte boundary.
        int modulo = size % 4, pad = 0;
        if (modulo != 0)
            pad = 4 - modulo;
        
        // Some additional padding in case of alignment issues.
        size += (saveKey.length + fat.length + 0x8 + pad);
        if (this.archiveRevision > 2) size += 0x14;
        if (this.archiveRevision == 5) size += 4;

        // Start building the archive

        MemoryOutputStream stream = new MemoryOutputStream(size);
        for (byte[] buffer : buffers)
            stream.bytes(buffer);
        
        stream.pad(pad);

        stream.bytes(saveKey);
        int fatOffset = stream.getOffset();
        stream.bytes(fat);

        int hashinateOffset = stream.getOffset();
        if (this.archiveRevision > 2) {
            // Pad out the hashinate, we need to actually generate it.
            stream.pad(0x14);
        }

        if (this.archiveRevision == 5)
            stream.bytes(Bytes.toBytesLE(this.fragmentIDs.length));
        stream.i32(entries.length);
        stream.str("FAR", 0x3);
        stream.u8(this.archiveRevision + '0');

        byte[] archive = stream.getBuffer();

        // Compute hash and write it to the buffer
        if (hashinate && this.archiveRevision > 2) {
            this.hashinate = Crypto.HMAC(archive, Crypto.HASHINATE_KEY);
            System.arraycopy(this.hashinate.getHash(), 0, archive, hashinateOffset, 0x14);
        } else this.hashinate = SHA1.EMPTY;
        
        // Update state of the archive in memory.
        this.entries = entries;
        this.queue.clear();
        this.fatOffset = fatOffset;
        if (this.file != null)
            this.lastModified = this.file.lastModified();

        // Reset the lookup tables
        this.lookup = new HashMap<>(entries.length);
        this.buffers = new HashMap<>(entries.length);
        for (int i = 0; i < entries.length; ++i) {
            Fat entry = entries[i];
            byte[] buffer = buffers[i];

            this.lookup.put(entry.getSHA1(), entry);
            this.buffers.put(entry, buffer);
        }

        return archive;
    }

    /**
     * Save archive at specified location
     * @param path Location to save
     * @return Whether or not the save was successful
     */
    public boolean save(String path) {
        if (path == null) 
            throw new IllegalArgumentException("Can't save archive to null path!");
        byte[] archive = this.build(false);
        return FileIO.write(archive, path);
    }

    @Override public boolean save() {
        if (this.file == null)
            throw new IllegalStateException("Can't save archive with no associated file!");
        byte[] archive = this.build(false);
        return FileIO.write(archive, this.file.getAbsolutePath());
    }

    public Revision getGameRevision() { return this.gameRevision; }
    public SaveKey getKey() { return this.key; }
    public SHA1 getHashinate() { return this.hashinate; }
    public int getArchiveRevision() { return this.archiveRevision; }
    public int getID() { return this.ID; }
    public int getBackupID() { return this.backupID; }
    public int[] getFragmentIDs() { return this.fragmentIDs; }
    public boolean isLittleEndian() { return this.isLittleEndian; }

    public void setGameRevision(Revision value) {
        if (value == null)
            throw new NullPointerException("Game revision cannot be null!");
        this.gameRevision = value;
    }

    public void setLittleEndian(boolean value) { this.isLittleEndian = value; }
}
