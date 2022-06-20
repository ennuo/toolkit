package cwlib.types.swing;

import cwlib.types.databases.FileEntry;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class FileNode extends DefaultMutableTreeNode {
    /**
     * Associated file entry
     */
    public FileEntry entry;

    /**
     * Path of node in tree.
     */
    public String path;

    /**
     * Name of node.
     */
    public String header;

    /**
     * Whether or not the node is currently visible in the tree.
     */
    public boolean isVisible = true;

    public FileNode(Object userObject, String path, FileEntry entry) {
        super(userObject);
        this.entry = entry;
        this.path = path;
        this.header = (String) userObject;
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
            if (node.isVisible) visibleIndex++;
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
            if (node.isVisible && (!noFolders || (noFolders && node.entry != null)))
                count++;
        }
        return count;
    }

    public void delete() {
        if (this.parent != null) 
            this.removeFromParent();
    }
}
