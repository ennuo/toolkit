package ennuo.craftworld.swing;

import ennuo.craftworld.types.FileEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class Nodes {
  public static int childIndex(FileNode node, String term) {
    Enumeration<TreeNode> children = node.children();
    DefaultMutableTreeNode child = null;
    int index = -1;
    while (children.hasMoreElements() && index < 0) {
      child = (DefaultMutableTreeNode)children.nextElement();
      if (child.getUserObject() != null && term.equals(child.getUserObject()))
        index = node.getIndex(child); 
    } 
    return index;
  }
  
  public static void loadChildren(ArrayList<FileNode> nodes, FileNode fishNode, boolean isFiltered) {
    if (fishNode.getChildCount(isFiltered, false) >= 0)
      for (Enumeration<TreeNode> e = fishNode.children(); e.hasMoreElements(); ) {
        FileNode node = (FileNode)e.nextElement();
        if (!node.isVisible && isFiltered)
          continue; 
        nodes.add(node);
        loadChildren(nodes, node, isFiltered);
      }  
  }
  
  public static int filter(FileNode root, SearchParameters params) {
      int visibleCount = 0;
      if (root.getChildCount(false, false) >= 0)
        for (Enumeration<TreeNode> e = root.children(); e.hasMoreElements(); ) {
            FileNode node = (FileNode) e.nextElement();
            boolean isVisible = false;
            if (node.entry != null) {
                if (params.pointer != null) {
                    if (params.pointer.hash != null) isVisible = Arrays.equals(node.entry.hash, params.pointer.hash);
                    else isVisible = node.entry.GUID == params.pointer.GUID;
                }
                else if (node.entry.path.contains(params.path)) 
                    isVisible = true;
                node.isVisible = isVisible;  
                if (isVisible)
                    visibleCount++;
            } else if (filter(node, params) == 0) node.isVisible = false;
            else { node.isVisible = true; visibleCount++; }
        }
      
        if (visibleCount == 0) root.isVisible = false;
        else root.isVisible = true;
        
        return visibleCount;
  }
  
  public static FileNode addNode(FileNode node, FileEntry entry) { return Nodes.addNode(node, entry, null); }
  public static FileNode addNode(FileNode node, FileEntry entry, String override) {
    String[] strings;
    if (entry != null)
        strings = entry.path.split("/");
    else
        strings = override.split("/");
    String relativePath = "";
    for (int i = 0; i < strings.length; i++) {
      int index = Nodes.childIndex(node, strings[i]);
      if (index == -1) {
        FileNode child = new FileNode(strings[i], relativePath, null);
        if (i + 1 == strings.length)
          child.entry = entry; 
        node.insert(child, node.getChildCount());
        node = child;
      } else {
        if (i + 1 == strings.length) {
            FileNode child = new FileNode(strings[i], relativePath, null);
            child.entry = entry; 
            node.insert(child, node.getChildCount());
        } else 
            node = (FileNode)node.getChildAt(index);   
      }
      relativePath = relativePath + (strings[i] + "/");
    } 
    return node;
  }
}
