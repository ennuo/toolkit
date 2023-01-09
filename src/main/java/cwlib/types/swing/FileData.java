package cwlib.types.swing;

import java.io.File;

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import cwlib.enums.DatabaseType;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileEntry;
import cwlib.util.Nodes;

public abstract class FileData {
    /**
     * Path of database on disk.
     */
    private final File file;
    
    /**
     * Name of file on disk.
     */
    private final String name;

    /**
     * Cached search query
     */
    private String query;

    /**
     * Path of USRDIR folder or the current folder if it doesn't exist.
     */
    private final File base;

    /**
     * Database type identifier.
     */
    private final DatabaseType type;

    /**
     * Tree swing component for database
     */
    private final JTree tree;

    /**
     * Tree model for database.
     */
    protected FileModel model;
    
    /**
     * Root node in file model.
     */
    protected FileNode root;

    protected boolean hasChanges = false;
    
    protected FileData(File file, DatabaseType type) {
        this.type = type;
        this.file = file;
        if (file == null) {
            this.name = type.name();
            this.base = null;
        }
        else {
            this.name = this.file.getName();

            File base = this.file.getParentFile();
            while (base != null) {
                base = base.getParentFile();
                if (base != null && base.getName().toUpperCase().equals("USRDIR"))
                    break;
            }

            if (base == null) this.base = this.file.getParentFile();
            else this.base = base;
        }
        this.model = new FileModel(new FileNode(type.name(), null, null, this));
        this.root = (FileNode) this.model.getRoot();
        
        JTree tree = new JTree();

        tree.setRootVisible(false);
        tree.setModel(this.model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        this.tree = tree;
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

    public FileEntry get(ResourceDescriptor descriptor) {
        if (descriptor == null) return null;
        if (descriptor.isHash()) return this.get(descriptor.getSHA1());
        if (descriptor.isGUID()) return this.get(descriptor.getGUID());
        return null;
    }

    public FileEntry get(SHA1 sha1) {
        throw new UnsupportedOperationException(String.format("Unable to search for SHA1 on database of type %s", this.type));
    }
    
    public FileEntry get(GUID guid) {
        throw new UnsupportedOperationException(String.format("Unable to search for GUID on database of type %s", this.type));
    }

    public FileEntry get(long guid) {
        return this.get(new GUID(guid));
    }
    
    public FileEntry get(String path) {
        throw new UnsupportedOperationException(String.format("Unable to search for path on database of type %s", this.type));
    }

    public byte[] extract(SHA1 sha1) {
        throw new UnsupportedOperationException(String.format("Unable to extract data on database of type %s", this.type));
    }

    public void add(byte[] data) {
        throw new UnsupportedOperationException(String.format("Unable to add data to database of type %s", this.type));
    }

    public void remove(FileEntry entry) {
        throw new UnsupportedOperationException(String.format("Unable to remove entry on database of type %s", this.type));
    }

    /**
     * Gets next available GUID in database above FileDB.MIN_SAFE_GUID.
     * @return Next available GUID
     */
    public GUID getNextGUID() {
        throw new UnsupportedOperationException(String.format("Unable to get next GUID on database of type %s", this.type));
    }

    public File getFile() { return this.file; }
    public String getName() { return this.name; }
    public File getBase() { return this.base; }
    public DatabaseType getType() { return this.type; }
    public FileModel getModel() { return this.model; }
    public FileNode getRoot() { return this.root; }
    public JTree getTree() { return this.tree; }
    
    public boolean hasChanges() { return this.hasChanges; }
    public void setHasChanges() { this.hasChanges = true; }

    public String getLastSearch() { return this.query; }
    public void setLastSearch(String query) { this.query = query; }
}
