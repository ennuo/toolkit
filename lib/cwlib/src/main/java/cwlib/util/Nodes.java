package cwlib.util;

import cwlib.types.data.ResourceDescriptor;
import cwlib.types.databases.FileEntry;
import cwlib.types.swing.FileNode;
import cwlib.types.swing.SearchParameters;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;

public class Nodes
{
    public static int childIndex(FileNode node, String header)
    {
        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements())
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            if (header.equals(child.getUserObject()))
                return node.getIndex(child);
        }

        return -1;
    }

    public static void loadChildren(ArrayList<FileNode> nodes, FileNode node, boolean isFiltered)
    {
        if (node.getChildCount(isFiltered, false) == 0)
            return;
        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements())
        {
            FileNode child = (FileNode) children.nextElement();
            if (!child.isVisible() && isFiltered)
                continue;
            nodes.add(child);
            Nodes.loadChildren(nodes, child, isFiltered);
        }
    }

    public static int filter(FileNode root, SearchParameters params)
    {
        int visibleCount = 0;
        if (root.getChildCount(false, false) >= 0)
            for (Enumeration<TreeNode> e = root.children(); e.hasMoreElements(); )
            {
                FileNode node = (FileNode) e.nextElement();
                boolean isVisible = false;
                FileEntry entry = node.getEntry();
                if (entry != null)
                {
                    ResourceDescriptor resource = params.getResource();
                    if (resource != null)
                    {
                        if (resource.isHash())
                            isVisible = entry.getSHA1().equals(resource.getSHA1());
                        else if (resource.isGUID())
                        {
                            isVisible = entry.getKey().equals(resource.getGUID());
                        }
                    }
                    else if (entry.getPath().contains(params.getPath()))
                        isVisible = true;
                    node.setVisible(isVisible);
                    if (isVisible)
                        visibleCount++;
                }
                else if (filter(node, params) == 0)
                    node.setVisible(false);
                else
                {
                    node.setVisible(true);
                    visibleCount++;
                }
            }

        root.setVisible((visibleCount != 0));

        return visibleCount;
    }

    public static FileNode addFolder(FileNode root, String path)
    {
        path = Strings.cleanupPath(path);
        if (path == null || path.isEmpty()) return root;

        String[] components = path.split("/");
        String parent = "";
        for (String component : components)
        {
            int index = Nodes.childIndex(root, component);
            if (index == -1)
            {
                FileNode child = new FileNode(component, parent, null, root.getSource());
                root.insert(child, root.getChildCount());
                root = child;
            }
            else root = (FileNode) root.getChildAt(index);
            parent += (component + "/");
        }
        return root;
    }

    public static FileNode addNode(FileNode node, FileEntry entry)
    {
        return Nodes.addNode(node, entry, null);
    }

    public static FileNode addNode(FileNode node, FileEntry entry, String override)
    {
        String[] strings;
        if (entry != null)
            strings = entry.getPath().split("/");
        else
            strings = override.split("/");
        
        String relativePath = "";
        for (int i = 0; i < strings.length - 1; i++)
        {
            int index = Nodes.childIndex(node, strings[i]);
            if (index == -1)
            {
                FileNode child = new FileNode(strings[i], relativePath, null, node.getSource());
                node.add(child);
                node = child;
            }
            else node = (FileNode) node.getChildAt(index);
            relativePath = relativePath + (strings[i] + "/");
        }

        FileNode child = new FileNode(strings[strings.length - 1], relativePath, entry, node.getSource());
        node.add(child);
        return child;
    }
}
