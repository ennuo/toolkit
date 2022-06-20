package cwlib.types.swing;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class FileModel extends DefaultTreeModel {
    public FileModel(FileNode root) { super(root, false); }

    public Object getChild(Object parent, int index) {
        if (parent instanceof FileNode)
            return ((FileNode) parent).getChildAt(index, true);
        return ((TreeNode) parent).getChildAt(index);
    }

    public int getChildCount(Object parent) {
        if (parent instanceof FileNode)
            return ((FileNode) parent).getChildCount(true, false);
        return ((TreeNode) parent).getChildCount();
    }
}
