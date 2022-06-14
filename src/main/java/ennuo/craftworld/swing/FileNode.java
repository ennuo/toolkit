package ennuo.craftworld.swing;

import ennuo.craftworld.types.databases.FileEntry;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class FileNode extends DefaultMutableTreeNode {
  public FileEntry entry;
  
  public String path;
  
  public String header;
  
  public boolean isVisible = true;
  
  public FileNode(Object userObject, String path, FileEntry entry) {
    super(userObject);
    this.entry = entry;
    this.path = path;
    this.header = (String)userObject;
  }
  
  public FileNode getChildAt(int index, boolean isFiltered) {
    if (!isFiltered)
      return (FileNode)getChildAt(index); 
    if (this.children == null)
      throw new ArrayIndexOutOfBoundsException("No children!"); 
    int realIndex = -1;
    int visibleIndex = -1;
    Enumeration<TreeNode> e = this.children.elements();
    while (e.hasMoreElements()) {
      FileNode node = (FileNode)e.nextElement();
      if (node.isVisible)
        visibleIndex++; 
      realIndex++;
      if (visibleIndex == index)
        return (FileNode)this.children.elementAt(realIndex); 
    } 
    throw new ArrayIndexOutOfBoundsException("Index unmatched!");
  }
  
  public int getChildCount(boolean isFiltered, boolean noFolders) {
    if (!isFiltered)
      return getChildCount(); 
    if (this.children == null)
      return 0; 
    int count = 0;
    Enumeration<TreeNode> e = this.children.elements();
    while (e.hasMoreElements()) {
      FileNode node = (FileNode)e.nextElement();
      if (node.isVisible && !noFolders)
        count++; 
      if (node.isVisible && noFolders && 
        node.entry != null)
        count++; 
    } 
    return count;
  }
  
  public void delete() {
    if (this.parent != null) {
        FileNode parent = (FileNode) this.parent;
        removeFromParent();    
    }
  }
}
