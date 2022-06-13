package ennuo.craftworld.types.databases;

import ennuo.craftworld.resources.io.FileIO;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ennuo.craftworld.resources.structs.GUID;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;

/**
 * The FileDB is a resource used by the game for assigning
 * paths and unique identifiers to resources stored in the
 * associated archives.
 */
public class FileDB implements Iterable<FileEntry> {
    /**
     * Default capacity for entry array when none is provided.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Minimum set of GUIDs not used by any of the LittleBigPlanet games.
     */
    private static final long MIN_SAFE_GUID = 0x00180000;

    /**
     * Original source of the FileDB when read from disk,
     * used for saving.
     */
    private File file;

    private int revision;
    private ArrayList<FileEntry> entries;

    private HashMap<GUID, FileEntry> lookup;

    /**
     * Creates a FileDB with specified version and capacity.
     * @param revision Game database version
     * @param capacity Capacity of underlying entry array
     */
    public FileDB(int revision, int capacity) {
        this.revision = revision;
        if (capacity < 0)
            throw new IllegalArgumentException("Cannot allocate entry array with negative count!");
        this.entries = new ArrayList<>(capacity);
        this.lookup = new HashMap<>(capacity);
    }

    /**
     * Creates a FileDB with specified version.
     * @param revision Game database version
     */
    public FileDB(int revision) {
        this(revision, FileDB.DEFAULT_CAPACITY);
    }

    /**
     * Reads a FileDB from a file.
     * @param file FileDB source file
     */
    public FileDB(File file) {
        this.file = file;
        final Data stream = new Data(file.getAbsolutePath());
        this.process(stream);
    }

    /**
     * Reads a FileDB from path.
     * @param path FileDB source path
     */
    public FileDB(String path) {
        this(new File(path));
    }

    private void process(Data stream) {
        this.revision = stream.i32();
        boolean isLBP3 = (this.revision >> 0x10) >= 0x148;
        int count = stream.i32();
        this.entries = new ArrayList<>(count);
        this.lookup = new HashMap<>(count);

        for (int i = 0; i < count; ++i) {
            String path = stream.str(isLBP3 ? stream.i16() : stream.i32());
            long timestamp = isLBP3 ? stream.u32() : stream.i64();
            long size = stream.u32();
            SHA1 sha1 = stream.sha1();
            GUID guid = stream.guid();

            // If a GUID is duplicated, skip it
            if (this.lookup.containsKey(guid))
                continue;

            /* 	In LittleBigPlanet Vita, some versions of the databases don't store any filenames,
                only the extensions, so we'll use the hash of the resource in place of a name. */
            if (path.startsWith(".")) {
                path = String.format("data/%s/%s%s",
                    FileDB.getFolderFromExtension(path), sha1.toString(), path);
            }

            FileEntry entry = new FileEntry(this, path, timestamp, size, sha1, guid);
            
            this.entries.add(entry);
            this.lookup.put(guid, entry);
        }
    }

    @Override public Iterator<FileEntry> iterator() { return this.entries.iterator(); }

    /**
     * Checks if a GUID exists in the database.
     * @param guid GUID to find
     * @return Whether or not the GUID exists.
     */
    public boolean exists(GUID guid) {
        return this.lookup.containsKey(guid);
    }

    /**
     * Checks if a GUID exists in the database.
     * @param guid GUID to find
     * @return Whether or not the GUID exists.
     */
    public boolean exists(long guid) { return this.exists(new GUID(guid)); }

    /**
     * Gets a FileEntry with specified GUID.
     * @param guid GUID to find
     * @return FileEntry with GUID
     */
    public FileEntry get(GUID guid) {
        if (this.lookup.containsKey(guid))
            return this.lookup.get(guid);
        return null;
    }

    /**
     * Gets a FileEntry with specified GUID.
     * @param guid GUID to find
     * @return FileEntry with GUID
     */
    public FileEntry get(long guid) { return this.get(new GUID(guid)); }

    /**
     * Gets a file name by path/name.
     * @param path Path/name of entry to find
     * @return Entry found
     */
    public FileEntry get(String path) {
        if (path == null)
            throw new NullPointerException("Can't find null path!");
        path = path.toLowerCase(); // Ignore cases
        for (FileEntry entry : this.entries)
            if (entry.getPath().toLowerCase().contains(path))
                return entry;
        return null;
    }

    /**
     * Removes an entry in the database by its GUID.
     * @param guid GUID to remove
     */
    public void remove(GUID guid) {
        final FileEntry entry = this.get(guid);
        if (entry == null) return;
        this.lookup.remove(guid);
        this.entries.remove(entry);
    }

    /**
     * Removes an entry in the database by its GUID.
     * @param guid GUID to remove
     */
    public void remove(long guid) { this.remove(new GUID(guid)); }

    /**
     * Updates the lookup tables with entry's new GUID.
     * The GUID is not set in this method.
     * @param oldGUID Old identifier for entry
     * @param newGUID New identifier for resource
     */
    protected void onGUIDChange(GUID oldGUID, GUID newGUID) {
        if (oldGUID.equals(newGUID)) return;
        FileEntry entry = this.get(oldGUID);
        if (entry == null)
            throw new IllegalArgumentException("Entry with GUID does not exist!");
        this.lookup.remove(oldGUID);
        this.lookup.put(newGUID, entry);
    }

    /**
     * Creates a new FileEntry in this database with specified path and GUID
     * @param path Location in database
     * @param guid Unique identifier for entry
     * @return Constructed FileEntry
     */
    public FileEntry newFileEntry(String path, GUID guid) {
        if (this.lookup.containsKey(guid))
            throw new IllegalArgumentException("GUID already exists in database!");
        final FileEntry entry = new FileEntry(this, path, 0, 0, SHA1.empty(), guid);
        this.entries.add(entry);
        this.lookup.put(guid, entry);
        return entry;
    }

    /**
     * Creates a new FileEntry in this database with specified path and next available GUID.
     * @param path Location in database
     * @return Constructed FileEntry
     */
    public FileEntry newFileEntry(String path) {
        return this.newFileEntry(path, this.getNextGUID());
    }

    /**
     * Creates a new FileEntry in this database with specified path and GUID
     * @param path Location in database
     * @param guid Unique identifier for entry
     * @return Constructed FileEntry
     */
    public FileEntry newFileEntry(String path, long guid) {
        return this.newFileEntry(path, new GUID(guid));
    }


    /**
     * Creates a new FileEntry in this database from another entry.
     * @param entry Entry to use as base
     * @return Constructed FileEntry
     */
    public FileEntry newFileEntry(FileEntry entry) {
        FileEntry newEntry = this.newFileEntry(entry.getPath(), entry.getGUID());
        newEntry.setDetails(entry);
        return newEntry;
    }

    /**
     * Patches this database with another.
     * @param patch Update data
     */
    public void patch(FileDB patch) {
        for (FileEntry entry : patch) {
            if (this.exists(entry.getGUID()))
                this.get(entry.getGUID()).setDetails(entry);
            else
                this.newFileEntry(entry);
        }
    }

    /**
     * Gets next available GUID in database above FileDB.MIN_SAFE_GUID.
     * @return Next available GUID
     */
    public GUID getNextGUID() {
        long lastGUID = this.entries
            .stream()
            .mapToLong(entry -> entry.getGUID().getValue())
            .reduce(0, (last, curr) -> {
                if (last < curr)
                    return curr;
                return last;
            });
        if (lastGUID < FileDB.MIN_SAFE_GUID)
            lastGUID = FileDB.MIN_SAFE_GUID;
        return new GUID(++lastGUID);
    }

    /**
     * Gets the number of entries in this database.
     * @return Number of entries
     */
    public int getEntryCount() {
        return this.entries.size();
    }

    /**
     * Saves this database to disk.
     * @param path Path to save file to
     * @return Whether or not this operation was successful
     */
    public boolean save(String path) {
        // Just figure the GUIDs should be in ascending order.
        entries.sort((l, r) -> Long.compare(l.getGUID().getValue(), r.getGUID().getValue()));

        int pathSize = this.entries
            .stream()
            .mapToInt(element -> element.getPath().length())
            .reduce(0, (total, element) -> total + element);
        Output stream = new Output(0x8 + (0x28 * this.entries.size()) + pathSize);
        stream.i32(this.revision);
        stream.i32(this.entries.size());
        boolean isLBP3 = (this.revision >> 0x10) >= 0x148;
        for (FileEntry entry : this.entries) {
            int length = entry.getPath().length();
            if (isLBP3)
                stream.i16((short) length);
            else
                stream.i32(length);
            
            stream.str(entry.getPath(), length);

            if (isLBP3) stream.u32(entry.getLastModified());
            else stream.i64(entry.getLastModified());

            stream.u32(entry.getSize());
            stream.sha1(entry.getSHA1());
            stream.guid(entry.getGUID());
        }
        // Shrink since we may have overestimated the size.
        stream.shrink();
        return FileIO.write(stream.buffer, path);
    }

    /**
     * Saves this database to disk.
     * @return Whether or not this operation was successful
     */
    public boolean save() {
        if (this.file == null)
            throw new IllegalStateException("Can't save to non-existent file!");
        return this.save(this.file.getAbsolutePath());
    }

    /**
     * Determines what folder a resource should go into based on its extension
     * @param extension Extension of resource
     * @return Resource folder string
     */
    public static final String getFolderFromExtension(String extension) {
        switch (extension.toLowerCase()) {
            case ".slt": return "slots/";
            case ".tex": return "textures/";
            case ".bpr": case ".ipr":
                return "profiles/";
            case ".mol": case ".msh": return "models/";
            case ".gmat": case ".gmt": return "gfx/";
            case ".mat": return "materials/";
            case ".ff": case ".fsh": return "scripts/";
            case ".plan": case ".pln": return "plans/";
            case ".pal": return "palettes/";
            case ".oft": return "outfits/";
            case ".sph": return "skeletons/";
            case ".bin": case ".lvl": return "levels/";
            case ".vpo": return "shaders/vertex/";
            case ".fpo": return "shaders/fragment/";
            case ".anim": case ".anm": return "animations/";
            case ".bev": return "bevels/";
            case ".smh": return "static_meshes/";
            case ".mus": return "audio/settings/";
            case ".fsb": return "audio/music/";
            case ".txt": return "text/";
            default: return "unknown/";
        }
    }
}
