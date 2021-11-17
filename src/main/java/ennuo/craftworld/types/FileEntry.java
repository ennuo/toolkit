package ennuo.craftworld.types;

import ennuo.craftworld.resources.structs.ProfileItem;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.types.data.ResourceDescriptor;
import java.util.HashMap;

public class FileEntry {
    public int revision;
    public String path = "Unresolved Path";
    public long timestamp = 0;
    public int size = 0;
    public long offset = -1;
    public SHA1 hash = new SHA1();
    public long GUID = -1;
    
    public byte[] data = null;

    public FileModel dependencyModel;
    public ResourceDescriptor[] dependencies;
    
    public boolean hasMissingDependencies = true;
    public boolean canReplaceDecompressed = false;
    
    public HashMap<String, Object> resources = new HashMap<String, Object>();
  
    public void setResource(String type, Object resource) {
        this.resources.put(type, resource);
    }
    
    public <T> T getResource(String type) {
        if (this.resources.containsKey(type))
            return (T) this.resources.get(type);
        return null;
    }
    
    public void resetResources() { this.resetResources(true); }
    public void resetResources(boolean resetBigProfile) {
        this.dependencyModel = null;
        this.dependencies = null;
        this.hasMissingDependencies = true;
        
        if (!resetBigProfile) {
            Slot slot = this.getResource("slot");
            ProfileItem item = this.getResource("profileItem");
            this.resources.clear();
            this.setResource("slot", slot);
            this.setResource("profileItem", item);
            return;
        }
        
        this.resources.clear();
    }
  
    public FileEntry(String path) {
        this.path = path;
        this.updateTimestamp();
    }
  
    public FileEntry(String path, long GUID) {
        this.path = path;
        this.GUID = GUID;
        this.updateTimestamp();
    }
  
    public FileEntry(String path, int size, long GUID) {
        this.path = path;
        this.size = size;
        this.GUID = GUID;
        this.updateTimestamp();
    }
  
    public FileEntry(byte[] data, SHA1 hash) {
        this.data = data;
        this.size = data.length;
        this.hash = hash;
        this.updateTimestamp();
    }
  
    public FileEntry(String path, int timestamp, int size, SHA1 hash, long guid, byte[] data) {
        this.path = path;
        this.timestamp = timestamp;
        this.size = size;
        this.hash = hash;
        this.GUID = guid;
        this.data = data;
    }
  
    public FileEntry(String path, int timestamp, int size, SHA1 hash, long guid) {
        this.path = path;
        this.timestamp = timestamp;
        this.size = size;
        this.hash = hash;
        this.GUID = guid;
    }
  
    public FileEntry(SHA1 hash, long offset, int size, byte[] data) {
        this.hash = hash;
        this.offset = offset;
        this.size = size;
        this.data = data;
        this.updateTimestamp();
    }
  
    public FileEntry(FileEntry entry) {
        this.path = entry.path;
        this.hash = entry.hash;
        this.offset = entry.offset;
        this.size = entry.size;
        if (entry.data != null)
            this.data = entry.data.clone();
        this.updateTimestamp();
        this.GUID = entry.GUID;
    }
  
    public void setData(FileEntry entry) {
        this.hash = entry.hash;
        this.size = entry.size;
        this.data = entry.data;
        this.timestamp = entry.timestamp;
        this.path = entry.path;
        this.resetResources();
    }
    
    public void setData(byte[] buffer) {
        if (buffer == null) {
            this.hash = new SHA1();
            this.size = 0;
            this.data = null;
            this.updateTimestamp();
            this.resetResources();
            return;
        }
        
        this.updateTimestamp();
        this.resetResources();
        this.hash = SHA1.fromBuffer(buffer);
        this.size = buffer.length;
        this.data = buffer;
    }
    
    public void updateTimestamp() { this.timestamp = System.currentTimeMillis() / 1000; }
  
    @Override
    public String toString() {
        return String.format("FileEntry (%s g%d)", path, GUID);
    }
}
