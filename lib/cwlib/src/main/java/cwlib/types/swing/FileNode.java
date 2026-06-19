package cwlib.types.swing;

import cwlib.types.data.GUID;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class FileNode extends DefaultMutableTreeNode
{
    /**
     * Associated file database.
     */
    private final FileData source;

    /**
     * Associated file entry
     */
    private final FileEntry entry;

    /**
     * Path of node in tree.
     */
    private final String path;

    /**
     * Whether or not the node is currently visible in the tree.
     */
    private boolean visible = true;
    public boolean wasOpen = false;

    public FileNode(String name, String path, FileEntry entry, FileData source)
    {
        super(name);
        this.entry = entry;
        this.path = path;
        this.source = source;
    }

    public FileNode getChildAt(int index, boolean isFiltered)
    {
        if (!isFiltered)
            return (FileNode) super.getChildAt(index);
        if (this.children == null)
            throw new ArrayIndexOutOfBoundsException("FileNode has no children!");
        int realIndex = -1;
        int visibleIndex = -1;
        Enumeration<TreeNode> e = this.children.elements();
        while (e.hasMoreElements())
        {
            FileNode node = (FileNode) e.nextElement();
            if (node.visible) visibleIndex++;
            realIndex++;
            if (visibleIndex == index)
                return (FileNode) this.children.elementAt(realIndex);
        }
        throw new ArrayIndexOutOfBoundsException("Index unmatched!");
    }

    public void removeAnyEmptyNodes()
    {
        if (this.entry != null) return;
        FileNode[] nodes = this.children.toArray(FileNode[]::new);
        for (FileNode node : nodes)
            node.removeAnyEmptyNodes();
        if (this.children.size() == 0)
            this.removeFromParent();
    }

    public int getChildCount(boolean isFiltered, boolean noFolders)
    {
        if (!isFiltered) return getChildCount();
        if (this.children == null) return 0;
        int count = 0;
        Enumeration<TreeNode> e = this.children.elements();
        while (e.hasMoreElements())
        {
            FileNode node = (FileNode) e.nextElement();
            if (node.visible && (!noFolders || (noFolders && node.entry != null)))
                count++;
        }
        return count;
    }

    private static int sortByPath(TreeNode a, TreeNode z)
    {
        FileNode nodeA = (FileNode)a;
        FileNode nodeB = (FileNode)z;

        boolean aIsFolder = nodeA.entry == null;
        boolean bIsFolder = nodeB.entry == null;

        if (aIsFolder != bIsFolder)
            return aIsFolder ? -1 : 1;

        return nodeA.getName().compareTo(nodeB.getName());
    }

    private static int sortByTimestamp(TreeNode a, TreeNode z)
    {
        FileNode nodeA = (FileNode)a;
        FileNode nodeB = (FileNode)z;

        boolean aIsFolder = nodeA.entry == null;
        boolean bIsFolder = nodeB.entry == null;

        if (aIsFolder != bIsFolder)
            return aIsFolder ? -1 : 1;
        
        if (!aIsFolder)
            return Long.compareUnsigned(((FileDBRow)nodeA.getEntry()).getDate(), ((FileDBRow)nodeB.getEntry()).getDate());
        
        return nodeA.getName().compareTo(nodeB.getName());
    }

    private static int sortByKey(TreeNode a, TreeNode z)
    {
        FileNode nodeA = (FileNode)a;
        FileNode nodeB = (FileNode)z;

        boolean aIsFolder = nodeA.entry == null;
        boolean bIsFolder = nodeB.entry == null;

        if (aIsFolder != bIsFolder)
            return aIsFolder ? -1 : 1;
        
        if (!aIsFolder)
            return Long.compareUnsigned(((GUID)nodeA.getEntry().getKey()).getValue(), ((GUID)nodeB.getEntry().getKey()).getValue());
        
        return nodeA.getName().compareTo(nodeB.getName());
    }

    public void sort()
    {
        if (children == null || children.size() == 0) return;

        if (this.source instanceof FileDB || this.source instanceof Mod)
        {
            children.sort(FileNode::sortByKey);

            for (TreeNode node : children)
            {
                if (((FileNode)node).entry == null)
                    ((FileNode)node).sort();
            }
        }
    }

    public void delete()
    {
        if (this.parent != null)
            this.parent.remove(this);
    }

    public FileData getSource()
    {
        return this.source;
    }

    public FileEntry getEntry()
    {
        return this.entry;
    }

    public String getFilePath()
    {
        return this.path;
    }

    public String getName()
    {
        return (String) this.userObject;
    }

    public void setName(String name)
    {
        this.userObject = name;
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }
}
