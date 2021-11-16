package ennuo.craftworld.swing;

import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.types.FileEntry;

public abstract class FileData {
    public String path;
    public String name;

    public String query;

    public String USRDIR;

    public String type;

    public FileModel model;
    public FileNode root;

    public long lastGUID = 0x00160000L;

    public boolean shouldSave = false;
    public FileNode addNode(FileEntry entry) { return Nodes.addNode(this.root, entry); }
    public FileNode addNode(String string) { return Nodes.addNode(this.root, null, string); }
    public boolean save(String path) { return false; }
    public boolean edit(FileEntry entry, byte[] buffer) { System.out.println("Not implemented!"); return false; };
    public FileEntry find(SHA1 hash) { return null; }
    public long getNextGUID() { lastGUID++; return lastGUID; }
  
}
