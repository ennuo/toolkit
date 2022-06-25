package cwlib.types.swing;

import cwlib.types.databases.FileEntry;
import cwlib.util.Nodes;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class FileNode extends DefaultMutableTreeNode {
    /**
     * Associated file entry
     */
    private final FileEntry entry;

    /**
     * Path of node in tree.
     */
    private String path;

    /**
     * Whether or not the node is currently visible in the tree.
     */
    private boolean visible = true;

    public FileNode(String name, String path, FileEntry entry) {
        super(name);
        this.entry = entry;
        this.path = path;
    }

    public FileNode getChildAt(int index, boolean isFiltered) {
        if (!isFiltered)
            return (FileNode) super.getChildAt(index);
        if (this.children == null)
            throw new ArrayIndexOutOfBoundsException("FileNode has no children!");
        int realIndex = -1;
        int visibleIndex = -1;
        Enumeration<TreeNode> e = this.children.elements();
        while (e.hasMoreElements()) {
            FileNode node = (FileNode) e.nextElement();
            if (node.visible) visibleIndex++;
            realIndex++;
            if (visibleIndex == index)
                return (FileNode) this.children.elementAt(realIndex);
        }
        throw new ArrayIndexOutOfBoundsException("Index unmatched!");
    }

    public int getChildCount(boolean isFiltered, boolean noFolders) {
        if (!isFiltered) return getChildCount();
        if (this.children == null) return 0;
        int count = 0;
        Enumeration<TreeNode> e = this.children.elements();
        while (e.hasMoreElements()) {
            FileNode node = (FileNode) e.nextElement();
            if (node.visible && (!noFolders || (noFolders && node.entry != null)))
                count++;
        }
        return count;
    }

    public void move(String folder) {
        if (folder.endsWith("/")) folder = folder.substring(0, folder.length() - 1);
        FileNode node = Nodes.addFolder((FileNode) this.getRoot(), folder);
        this.removeFromParent();

        this.path = folder;
        if (!folder.endsWith("/")) this.path += "/";

        this.getEntry().setPath(this.path + this.getName());
        
        this.setParent(node);
    }

    public void delete() {
        if (this.parent != null) 
            this.removeFromParent();
    }

    public FileEntry getEntry() { return this.entry; }
    public String getFilePath() { return this.path; }
    public String getName() { return (String) this.userObject; }
    public void setName(String name) { this.userObject = name; }
    public boolean isVisible() { return this.visible; }

    public void setVisible(boolean visible) { this.visible = visible; }
}
