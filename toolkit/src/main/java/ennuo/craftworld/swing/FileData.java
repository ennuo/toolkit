package ennuo.craftworld.swing;

import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.memory.Data;

public class FileData {
  public String path;
  public String name;
  
  public String query;
  
  public String type;
    
  public FileModel model;
  public FileNode root;
  
  public long lastGUID = 0x00160000L;
  
  public boolean shouldSave = false;
  public FileNode addNode(FileEntry entry) { return Nodes.addNode(this.root, entry); }
  public FileNode addNode(String string) { return Nodes.addNode(this.root, null, string); }
  public boolean save(String path) { return false; }
  public void replace(FileEntry entry, byte[] buffer) { System.out.println("Not implemented!"); };
  public FileEntry find(byte[] hash) { return null; }
  public long getNextGUID() { lastGUID++; return lastGUID; }
  
}
