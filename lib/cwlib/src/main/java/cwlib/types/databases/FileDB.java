package cwlib.types.databases;

import cwlib.enums.DatabaseType;
import cwlib.enums.ResourceType;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.resources.RPalette;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileNode;
import cwlib.util.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The FileDB is a resource used by the game for assigning
 * paths and unique identifiers to resources stored in the
 * associated archives.
 */
public class FileDB extends FileData implements Iterable<FileDBRow>
{
    /**
     * Default capacity for entry array when none is provided.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Minimum set of GUIDs not used by any of the LittleBigPlanet games.
     */
    private static final long MIN_SAFE_GUID = 0x00180000;

    private int revision;
    protected ArrayList<FileDBRow> entries;

    protected HashMap<GUID, FileDBRow> lookup;

    /**
     * For databases that inherit FileDB
     */
    protected FileDB(File file, DatabaseType type)
    {
        super(file, type);
    }

    /**
     * For databases that inherit FileDB
     */
    protected FileDB(File file, DatabaseType type, int revision)
    {
        super(file, type);
        this.revision = revision;
        this.entries = new ArrayList<>(DEFAULT_CAPACITY);
        this.lookup = new HashMap<>(DEFAULT_CAPACITY);
    }

    /**
     * Creates a FileDB with specified version and capacity.
     *
     * @param revision Game database version
     * @param capacity Capacity of underlying entry array
     */
    public FileDB(int revision, int capacity)
    {
        super(null, DatabaseType.FILE_DATABASE);
        this.revision = revision;
        if (capacity < 0)
            throw new IllegalArgumentException("Cannot allocate entry array with negative " +
                                               "count!");
        this.entries = new ArrayList<>(capacity);
        this.lookup = new HashMap<>(capacity);
    }

    /**
     * Creates a FileDB with specified version.
     *
     * @param revision Game database version
     */
    public FileDB(int revision)
    {
        this(revision, FileDB.DEFAULT_CAPACITY);
    }

    /**
     * Reads a FileDB from a file.
     *
     * @param file FileDB source file
     */
    public FileDB(File file)
    {
        super(file, DatabaseType.FILE_DATABASE);
        this.process(new MemoryInputStream(file.getAbsolutePath()));
    }

    /**
     * Reads a FileDB from a byte array.
     *
     * @param data FileDB source buffer
     */
    public FileDB(byte[] data)
    {
        super(null, DatabaseType.FILE_DATABASE);
        this.process(new MemoryInputStream(data));
    }

    /**
     * Reads a FileDB from path.
     *
     * @param path FileDB source path
     */
    public FileDB(String path)
    {
        this(new File(path));
    }

    protected void process(MemoryInputStream stream)
    {
        this.revision = stream.i32();
        boolean isLBP3 = (this.revision >> 0x10) >= 0x148;
        int count = stream.i32();
        this.entries = new ArrayList<>(count);
        this.lookup = new HashMap<>(count);

        for (int i = 0; i < count; ++i)
        {
            String path = stream.str(isLBP3 ? stream.i16() : stream.i32());
            long timestamp = isLBP3 ? stream.u32() : stream.s64();
            long size = stream.u32();
            SHA1 sha1 = stream.sha1();
            GUID guid = stream.guid();

            // If a GUID is duplicated, skip it
            if (this.lookup.containsKey(guid))
                continue;

            /* 	In LittleBigPlanet Vita, some versions of the databases don't store any filenames,
                only the extensions, so we'll use the hash of the resource in place of a name. */
            if (path.startsWith("."))
            {
                path = String.format("data/%s%s%s",
                    FileDB.getFolderFromExtension(path), sha1, path);
            }

            FileDBRow entry = new FileDBRow(this, path, timestamp, size, sha1, guid);

            this.entries.add(entry);
            this.lookup.put(guid, entry);
        }
    }

    @Override
    public Iterator<FileDBRow> iterator()
    {
        return this.entries.iterator();
    }

    /**
     * Checks if a GUID exists in the database.
     *
     * @param guid GUID to find
     * @return Whether or not the GUID exists.
     */
    public boolean exists(GUID guid)
    {
        return this.lookup.containsKey(guid);
    }

    /**
     * Checks if a GUID exists in the database.
     *
     * @param guid GUID to find
     * @return Whether or not the GUID exists.
     */
    public boolean exists(long guid)
    {
        return this.exists(new GUID(guid));
    }

    /**
     * Gets a FileDBRow with a specified SHA1.
     * <p>
     * This method generally should not be used, as it's
     * not guaranteed that this row is unique. It's also
     * slower than GUID lookup, as a HashMap is not maintained.
     *
     * @param sha1 SHA1 to find
     * @return FileDBRow with SHA1
     */
    @Override
    public FileDBRow get(SHA1 sha1)
    {
        for (FileDBRow row : this.entries)
            if (row.getSHA1().equals(sha1))
                return row;
        return null;
    }

    /**
     * Gets a FileDBRow with specified GUID.
     *
     * @param guid GUID to find
     * @return FileDBRow with GUID
     */
    @Override
    public FileDBRow get(GUID guid)
    {
        if (this.lookup.containsKey(guid))
            return this.lookup.get(guid);
        return null;
    }

    /**
     * Gets a FileDBRow with specified GUID.
     *
     * @param guid GUID to find
     * @return FileDBRow with GUID
     */
    @Override
    public FileDBRow get(long guid)
    {
        return this.get(new GUID(guid));
    }

    /**
     * Gets a file name by path/name.
     *
     * @param path Path/name of entry to find
     * @return Entry found
     */
    @Override
    public FileDBRow get(String path)
    {
        if (path == null)
            throw new NullPointerException("Can't find null path!");
        path = path.toLowerCase(); // Ignore cases
        for (FileDBRow entry : this.entries)
            if (entry.getPath().toLowerCase().contains(path))
                return entry;
        return null;
    }

    /**
     * Updates the lookup tables with entry's new GUID.
     * The GUID is not set in this method.
     *
     * @param oldGUID Old identifier for entry
     * @param newGUID New identifier for resource
     */
    protected void onGUIDChange(GUID oldGUID, GUID newGUID)
    {
        if (oldGUID.equals(newGUID)) return;
        FileDBRow entry = this.get(oldGUID);
        if (entry == null)
            throw new IllegalArgumentException("Entry with GUID does not exist!");
        this.lookup.remove(oldGUID);
        this.lookup.put(newGUID, entry);
    }

    /**
     * Creates a new FileDBRow in this database with specified path and GUID
     *
     * @param path Location in database
     * @param guid Unique identifier for entry
     * @return Constructed FileDBRow
     */
    public FileDBRow newFileDBRow(String path, GUID guid)
    {
        if (this.lookup.containsKey(guid))
            throw new IllegalArgumentException("GUID already exists in database!");
        final FileDBRow entry = new FileDBRow(this, path, 0, 0, new SHA1(), guid);
        entry.updateDate();
        this.entries.add(entry);
        this.lookup.put(guid, entry);
        return entry;
    }

    /**
     * Creates a new FileDBRow in this database with specified path and next available GUID.
     *
     * @param path Location in database
     * @return Constructed FileDBRow
     */
    public FileDBRow newFileDBRow(String path)
    {
        return this.newFileDBRow(path, this.getNextGUID());
    }

    /**
     * Creates a new FileDBRow in this database with specified path and GUID
     *
     * @param path Location in database
     * @param guid Unique identifier for entry
     * @return Constructed FileDBRow
     */
    public FileDBRow newFileDBRow(String path, long guid)
    {
        return this.newFileDBRow(path, new GUID(guid));
    }


    /**
     * Creates a new FileDBRow in this database from another entry.
     *
     * @param entry Entry to use as base
     * @return Constructed FileDBRow
     */
    public FileDBRow newFileDBRow(FileDBRow entry)
    {
        FileDBRow newEntry = this.newFileDBRow(entry.getPath(), entry.getGUID());
        newEntry.setDetails(entry);
        return newEntry;
    }

    @Override
    public void remove(FileEntry entry)
    {
        if (entry.getSource() != this)
            throw new IllegalArgumentException("FileDBRow doesn't belong to this database!");
        this.entries.remove(entry);
        this.lookup.remove(entry.getKey());
        FileNode node = entry.getNode();
        if (node != null) node.delete();
    }

    /**
     * Patches this database with another.
     *
     * @param patch Update data
     */
    public void patch(FileDB patch)
    {
        for (FileDBRow entry : patch)
        {
            if (this.exists(entry.getGUID()))
                this.get(entry.getGUID()).setDetails(entry);
            else
                this.newFileDBRow(entry);
        }
    }

    /**
     * Generates an RPlan resource list from this database
     *
     * @return New line separated resource list
     */
    public String toRLST()
    {
        int pathSize = this.entries
            .stream()
            .mapToInt(element -> element.getPath().length() + 1)
            .reduce(0, (total, element) -> total + element);

        StringBuilder builder = new StringBuilder(pathSize);
        for (FileDBRow entry : this.entries)
        {
            String path = entry.getPath();
            if (path.endsWith(".plan"))
                builder.append(path + '\n');
        }

        return builder.toString();
    }

    /**
     * Generates an RPalette from this database
     *
     * @return RPalette instance
     */
    public RPalette toPalette()
    {
        RPalette palette = new RPalette();
        for (FileDBRow entry : this.entries)
        {
            String path = entry.getPath();
            if (path.endsWith(".plan"))
                palette.planList.add(new ResourceDescriptor(entry.getGUID(),
                    ResourceType.PLAN));
        }
        return palette;
    }

    @Override
    public GUID getNextGUID()
    {
        long lastGUID = FileDB.MIN_SAFE_GUID;
        while (this.lookup.containsKey(new GUID(lastGUID))) lastGUID++;
        return new GUID(lastGUID);
    }

    /**
     * Gets the number of entries in this database.
     *
     * @return Number of entries
     */
    public int getEntryCount()
    {
        return this.entries.size();
    }

    /**
     * Serializes the current state of the FileDB.
     *
     * @return Serialized database
     */
    public byte[] build()
    {
        // Just figure the GUIDs should be in ascending order.
        entries.sort((l, r) -> Long.compareUnsigned(l.getGUID().getValue(),
            r.getGUID().getValue()));

        int pathSize = this.entries
            .stream()
            .mapToInt(element -> element.getPath().length())
            .reduce(0, (total, element) -> total + element);

        boolean isLBP3 = (this.revision >> 0x10) >= 0x148;
        int baseEntrySize = (isLBP3) ? 0x22 : 0x28;
        MemoryOutputStream stream =
            new MemoryOutputStream(0x8 + (baseEntrySize * this.entries.size()) + pathSize);
        stream.i32(this.revision);
        stream.i32(this.entries.size());
        for (FileDBRow entry : this.entries)
        {
            int length = entry.getPath().length();
            if (isLBP3)
                stream.i16((short) length);
            else
                stream.i32(length);

            stream.str(entry.getPath(), length);

            if (isLBP3) stream.u32(entry.getDate());
            else stream.s64(entry.getDate());

            stream.u32(entry.getSize());
            stream.sha1(entry.getSHA1());
            stream.guid(entry.getGUID());
        }
        return stream.getBuffer();
    }

    /**
     * Saves this database to disk.
     *
     * @param file File to save to
     * @return Whether or not this operation was successful
     */
    @Override
    public boolean save(File file)
    {
        if (file == null)
            throw new IllegalStateException("Can't save to non-existent file!");
        boolean success = FileIO.write(this.build(), file.getAbsolutePath());
        if (success) this.hasChanges = false;
        return success;
    }

    /**
     * Determines what folder a resource should go into based on its extension
     *
     * @param extension Extension of resource
     * @return Resource folder string
     */
    public static final String getFolderFromExtension(String extension)
    {
        switch (extension.toLowerCase())
        {
            case ".slt":
                return "slots/";
            case ".tex":
                return "textures/";
            case ".bpr":
            case ".ipr":
                return "profiles/";
            case ".mol":
            case ".msh":
                return "models/";
            case ".gmat":
            case ".gmt":
                return "gfx/";
            case ".mat":
                return "materials/";
            case ".ff":
            case ".fsh":
                return "scripts/";
            case ".plan":
            case ".pln":
                return "plans/";
            case ".pal":
                return "palettes/";
            case ".oft":
                return "outfits/";
            case ".sph":
                return "skeletons/";
            case ".bin":
            case ".lvl":
                return "levels/";
            case ".vpo":
                return "shaders/vertex/";
            case ".fpo":
                return "shaders/fragment/";
            case ".anim":
            case ".anm":
                return "animations/";
            case ".bev":
                return "bevels/";
            case ".smh":
                return "static_meshes/";
            case ".mus":
                return "audio/settings/";
            case ".fsb":
                return "audio/music/";
            case ".txt":
                return "text/";
            default:
                return "unknown/";
        }
    }
}
