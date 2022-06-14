package ennuo.craftworld.swing;

import ennuo.craftworld.resources.structs.GUID;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.types.databases.FileEntry;

import java.io.File;

public abstract class FileData {
    /**
     * Path of database on disk.
     */
    public final File file;
    
    /**
     * Name of file on disk.
     */
    public final String name;

    /**
     * Cached search query
     */
    public String query;

    /**
     * Path of USRDIR folder if database is in game directory.
     */
    public String USRDIR;

    /**
     * Database type identifier.
     */
    public DatabaseType type;

    /**
     * Tree model for database.
     */
    public final FileModel model;
    
    /**
     * Root node in file model.
     */
    public final FileNode root;

    protected boolean hasChanges = false;
    
    protected FileData(File file, DatabaseType type) {
        this.type = type;
        this.file = file;
        if (file == null)
            this.name = type.name();
        else
            this.name = file.getName();
        this.model = new FileModel(new FileNode(type, null, null));
        this.root = (FileNode) this.model.getRoot();
    }
    
    /**
     * Adds a node to associated model, node path is used for
     * @param entry
     * @return 
     */
    public final FileNode addNode(FileEntry entry) { return Nodes.addNode(this.root, entry); }
    
    /**
     * Adds a node with no associated entry to associated model, generally used for folders.
     * @param path Path of node
     * @return Created node
     */
    public final FileNode addNode(String path) { return Nodes.addNode(this.root, null, path); }
    
    public boolean save(File file) {
        throw new UnsupportedOperationException(String.format("Saving is unimplemented for database of type %s", this.type));
    }
    
    public final boolean save() { 
        if (this.file == null)
            throw new IllegalStateException("Can't save to non-existent file!");
        boolean success = this.save(this.file);
        if (success) this.hasChanges = false;
        return success;
    }
    
    public FileEntry get(SHA1 hash) {
        throw new UnsupportedOperationException(String.format("Unable to search for SHA1 on database of type %s", this.type));
    }
    
    public FileEntry get(GUID guid) {
        throw new UnsupportedOperationException(String.format("Unable to search for GUID on database of type %s", this.type));
    }
    
    public FileEntry get(long guid) { return this.get(new GUID(guid)); }
    
    public FileEntry get(String path) {
        throw new UnsupportedOperationException(String.format("Unable to search for path on database of type %s", this.type));
    }
    
    public boolean hasChanges() { return this.hasChanges; }
    public void setHasChanges() { this.hasChanges = true; }
}
