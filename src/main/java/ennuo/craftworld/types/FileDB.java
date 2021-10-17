package ennuo.craftworld.types;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JProgressBar;

public class FileDB extends FileData {
    public boolean isLBP3;
    public int header;
  
    public boolean isParsed = false;
  
    public ArrayList<FileEntry> entries;
  
    public HashMap<String, FileEntry> SHA1Lookup;
    public HashMap<Long, FileEntry> GUIDLookup;
    
    /**
     * Creates a FileDB from file and generates nodes
     * @param file Path of FileDB resource
     * @param progress Progress bar to update
     */
    public FileDB(File file, JProgressBar progress) {
        this.type = "FileDB";
        this.path = file.getAbsolutePath();
        this.name = file.getName();
      
        this.checkGameDirectory(file);
        this.process(new Data(this.path), progress);
    }
  
    /**
     * Creates a FileDB from file.
     * @param file Path of FileDB resource
     */
    public FileDB(File file) {
        this.type = "FileDB";
        this.path = file.getAbsolutePath(); 
        this.name = file.getName();
        
        this.checkGameDirectory(file);
        this.process(new Data(this.path), null);
    }
    
    /**
     * Checks if the FileDB is in a game install folder and sets the USRDIR
     * @param file Path of FileDB resource
     */
    private void checkGameDirectory(File file) {
        // NOTE(Jun): Check if this map is from a game install
        // useful for loading assets that are only meant for
        // being streamed from disk.
      
        File parent = file.getParentFile().getParentFile();
        if (parent.getName().toUpperCase().equals("USRDIR"))
            USRDIR = parent.getAbsolutePath() + "\\";
    }
  
    /**
     * Processes a FileDB from data instance.
     * @param data FileDB data source
     * @param bar Progress bar to update
     */
    private void process(Data data, JProgressBar bar) {
        System.out.println("Started processing FileDB located at: " + this.path);
        long begin = System.currentTimeMillis();

        this.header = data.i32();
        this.isLBP3 = isLBP3(this.header);
        int count = data.i32();

        if (bar != null) {
            bar.setVisible(true);
            bar.setMaximum(count);
            bar.setValue(0);
        }

        System.out.println("Entry Count: " + count);

        this.entries = new ArrayList<FileEntry>(count);
        this.SHA1Lookup = new HashMap<String, FileEntry>(count);
        this.GUIDLookup = new HashMap<Long, FileEntry>(count);

        if (bar != null) {
            this.model = new FileModel(new FileNode("FILEDB", null, null));
            this.root = (FileNode) this.model.getRoot();
        }

        for (int i = 0; i < count; i++) {
            if (bar != null) bar.setValue(i);
                        
            String path = data.str(this.isLBP3 ? data.i16() : data.i32());
            if (!this.isLBP3) data.forward(4); 
            int timestamp = data.i32();
            int size = data.i32();
            byte[] SHA1 = data.bytes(20);
            long GUID = data.u32();
            
            if (path.startsWith(".")) 
                path = "data/" + FileDB.getFolderFromExtension(path + Bytes.toHex(SHA1).toLowerCase()) + path;
            if (GUID > lastGUID && GUID < 0x00180000)
                this.lastGUID = GUID;
            
            FileEntry entry = new FileEntry(path, timestamp, size, SHA1, GUID);
            this.GUIDLookup.put(GUID, entry);
            this.SHA1Lookup.put(Bytes.toHex(SHA1), entry);
            this.entries.add(entry);
            
            if (bar != null && !FileDB.isHidden(path))
                this.addNode(entry);
        }
        
        if (bar != null) {
            bar.setValue(0); bar.setMaximum(0);
            bar.setVisible(false);
        }
        
        long end = System.currentTimeMillis();
        System.out.println(
            String.format("Finished processing %s! (%s s %s ms)",
                "FileDB",
                ((end - begin) / 1000),
                (end - begin))
        );
        
        this.isParsed = true;
    }
  
    /**
     * Finds FileEntry via GUID.
     * @param GUID GUID to find in FileDB
     * @return Entry found in FileDB
     */
    public FileEntry find(long GUID) {
        if (this.GUIDLookup.containsKey(GUID))
            return this.GUIDLookup.get(GUID);
        return null;
    }
  
    /**
     * Finds FileEntry via SHA1.
     * @param SHA1 SHA1 to find in the FileDB
     * @return Entry found in FileDB
     */
    public FileEntry find(byte[] SHA1) { return this.find(Bytes.toHex(SHA1)); }
    
    /**
     * Finds FileEntry via SHA1.
     * @param SHA1 SHA1 to find in the FileDB
     * @return Entry found in FileDB
     */
    public FileEntry find(String SHA1) {
        if (this.SHA1Lookup.containsKey(SHA1))
            return this.SHA1Lookup.get(SHA1);
        return null;
    }
    
    /**
     * Finds all entries in the FileDB that have specified SHA1.
     * @param SHA1 SHA1 to find in the FileDB
     * @return Array of FileEntrys that match SHA1
     */
    public FileEntry[] findAll(byte[] SHA1) {
        ArrayList<FileEntry> results = new ArrayList<FileEntry>(this.entries.size());
        for (FileEntry entry : this.entries)
            if (Arrays.equals(entry.SHA1, SHA1))
                results.add(entry);
        return (FileEntry[]) results.toArray(new FileEntry[results.size()]);
    }
  
    /**
     * Gets the next available GUID in the FileDB.
     * @return Next available GUID
     */
    public long getNextGUID() { return ++this.lastGUID; }

    /**
     * Adds a new entry to the FileDB
     * @param entry Entry to be added
     * @return True if an entry was added to the FileDB, false if it already existed.
     */
    public boolean add(FileEntry entry) {
        FileEntry existing = this.find(entry.GUID);
        this.shouldSave = true;
        
        if (existing == null) {
            entry.updateTimestamp();
            this.entries.add(entry);
            this.SHA1Lookup.put(Bytes.toHex(entry.SHA1), entry);
            this.GUIDLookup.put(entry.GUID, entry);
            return true;
        }
        
        existing.setData(entry);
        
        return false;
    }
  
    /**
     * Removes a FileEntry via GUID.
     * @param GUID GUID of the FileEntry to remove
     * @return Whether or not the operation was successful.
     */
    public boolean remove(long GUID) { return this.remove(this.find(GUID)); }
  
    /**
     * Removes a FileEntry from the FileDB.
     * @param entry Entry to remove from the FileDB
     * @return Whether or not the operation was successful.
     */
    public boolean remove(FileEntry entry) {
        if (entry == null) return false;
        this.entries.remove(entry);
        this.GUIDLookup.remove(entry.GUID);
        this.SHA1Lookup.remove(Bytes.toHex(entry.SHA1));
        this.shouldSave = true;
        return true;
    }
    
    /**
     * Renames a FileEntry in the FileDB.
     * @param entry Entry to rename
     * @param path Path to set the entry to
     * @return Whether or not the operation was successful
     */
    public boolean rename(FileEntry entry, String path) {
        FileEntry lookup = this.find(entry.GUID);
        if (lookup != null) {
            this.shouldSave = true;
            entry.path = path;
            return true;
        }
        return false;
    }
    
    /**
     * Edits the GUID of the FileEntry.
     * @param entry Entry to edit
     * @param GUID GUID to set
     * @return Whether or not the operation was successful.
     */
    public boolean edit(FileEntry entry, long GUID) {
        FileEntry lookup = this.find(entry.GUID);
        if (lookup != null) {
            entry.GUID = GUID;
            this.shouldSave = true;
            return true;
        }
        return false;
    }
  
    /**
     * Replaces the buffer contained by the FileEntry.
     * @param entry Entry to edit
     * @param buffer Buffer to set
     * @return Whether or not the operation was successful.
     */
    public boolean edit(FileEntry entry, byte[] buffer) {
        FileEntry lookup = this.find(entry.GUID);
        if (lookup != null) {
            entry.setData(buffer);
            this.shouldSave = true;
            return true;
        }
        return false;
    }
    
    /**
     * Replaces the FileEntry via GUID
     * @param entry FileEntry to use as a base
     * @param GUID GUID of FileEntry to replace
     * @return Whether or not the operation was successful.
     */
    public boolean replace(FileEntry entry, long GUID) {
        FileEntry lookup = this.find(GUID);
        if (lookup == null) return false;
        lookup.setData(entry);
        this.shouldSave = true;
        return true;
    }
  
    /**
     * Nulls the buffer the FileEntry contains.
     * @param entry FileEntry to be zeroed
     * @return Whether or not the operation was successful.
     */
    public boolean zero(FileEntry entry) {
        FileEntry lookup = this.find(entry.GUID);
        if (lookup != null) {
            entry.setData((byte[]) null);
            this.shouldSave = true;
            return true;
        }
        return false;
    }
  
    /**
     * Creates an RLST from FileDB plan entries.
     * @return String buffer containing list of paths to plan entries.
     */
    public String toRLST() {
        StringBuilder builder = new StringBuilder();
        for (FileEntry entry : this.entries)
            if (entry.path.contains(".plan"))
                builder.append(entry.path + "\n");
        return builder.toString();
    }
  
    /**
     * Saves the FileDB to a file.
     * @return Whether or not the operation was successful 
     */
    public boolean save() { return this.save(this.path); }
    
    /**
     * Saves the FileDB to a specified file.
     * @param path Path to save FileDB to
     * @return Whether or not the operation was successful.
     */
    public boolean save(String path) {
        int pathSize = this.entries
            .stream()
            .mapToInt(element -> element.path.length())
            .reduce(0, (total, element) -> total + element);
        Output output = new Output(0x8 + (0x28 * this.entries.size()) + pathSize);
        output.i32(this.header);
        output.i32(this.entries.size());
        for (FileEntry entry : this.entries) {
            if (this.isLBP3) output.i16((short) entry.path.length());
            else output.i32(entry.path.length());
            output.str(entry.path);
            if (!this.isLBP3) output.i32(0);
            output.u32(entry.timestamp);
            output.i32(entry.size);
            output.bytes(entry.SHA1);
            output.u32(entry.GUID);
        }
        output.shrink();
        if (FileIO.write(output.buffer, path)) {
            if (path.equals(this.path))
                this.shouldSave = false;
            return true;
        }
        return false;
    }
  
    /**
     * Checks if the FileDB is from LBP3 based on the magic header.
     * @param header FileDB magic header
     * @return Whether or not the magic dictates that the RFileDB is LBP3
     */
    public static final boolean isLBP3(int header) {
        switch (header) {
            case 256:
                System.out.println("Detected: LBP1/2 .MAP File");
                return false;
            case 21496064:
                System.out.println("Detected: LBP3 .MAP File");
                return true;
            case 936:
                System.out.println("Detected LBP Vita .MAP File");
                return false;
        } 
        System.out.println("Detected Unknown .MAP File");
        return false;
    }
  
    /**
     * Checks if the path should be hidden from the tree.
     * @param path Path of entry
     * @return Whether or not the path should be hidden
     */
    public static final boolean isHidden(String path) {
        return (path.contains(".farc") 
                || path.contains(".sdat") 
                || path.contains(".sdat") 
                || path.equals("") 
                || path.contains("empty_video.bik"));
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
            case ".bpr": case "ipr":
                return "profiles/";
            case ".mol": return "models/";
            case ".gmat": return "gfx/";
            case ".mat": return "materials/";
            case ".ff": return "scripts/";
            case ".plan": return "plans/";
            case ".pal": return "palettes/";
            case ".oft": return "outfits/";
            case ".sph": return "skeletons/";
            case ".bin": return "levels/";
            case ".vpo": return "shaders/vertex/";
            case ".fpo": return "shaders/fragment/";
            case ".anim": return "animations/";
            case ".bev": return "bevels/";
            case ".smh": return "static_meshes/";
            case ".mus": return "audio/settings/";
            case ".fsb": return "audio/music/";
            case ".txt": return "text/";
            default: return "unknown/";
        }
    }
}
