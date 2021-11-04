package ennuo.craftworld.types;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.resources.structs.ProfileItem;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.swing.FileModel;
import java.util.HashMap;

public class FileEntry {
    public int revision;
    public String path;
    public long timestamp = 0;
    public int size = 0;
    public long offset = -1;
    public byte[] SHA1 = new byte[20];
    public long GUID = -1;
    public byte[] data = null;

    public FileModel dependencyModel;
    public FileEntry[] dependencies;
    
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
  
    public FileEntry(byte[] data, byte[] hash) {
        this.data = data;
        this.size = data.length;
        this.SHA1 = hash;
        this.updateTimestamp();
    }
  
    public FileEntry(String path, int timestamp, int size, byte[] hash, long guid, byte[] data) {
        this.path = path;
        this.timestamp = timestamp;
        this.size = size;
        this.SHA1 = hash;
        this.GUID = guid;
        this.data = data;
    }
  
    public FileEntry(String path, int timestamp, int size, byte[] hash, long guid) {
        this.path = path;
        this.timestamp = timestamp;
        this.size = size;
        this.SHA1 = hash;
        this.GUID = guid;
    }
  
    public FileEntry(byte[] hash, long offset, int size, byte[] data) {
        this.SHA1 = hash;
        this.offset = offset;
        this.size = size;
        this.data = data;
        this.updateTimestamp();
    }
  
    public FileEntry(FileEntry entry) {
        this.path = entry.path;
        if (entry.SHA1 != null)
          this.SHA1 = entry.SHA1.clone();
        this.offset = entry.offset;
        this.size = entry.size;
        if (entry.data != null)
            this.data = entry.data.clone();
        this.updateTimestamp();
        this.GUID = entry.GUID;
    }
  
    public void setData(FileEntry entry) {
        this.SHA1 = entry.SHA1.clone();
        this.size = entry.size;
        this.data = entry.data;
        this.timestamp = entry.timestamp;
        this.path = entry.path;
        this.resetResources();
    }
    
    public void setData(byte[] buffer) {
        if (buffer == null) {
            this.SHA1 = new byte[20];
            this.size = 0;
            this.data = null;
            this.updateTimestamp();
            this.resetResources();
            return;
        }
        
        this.updateTimestamp();
        this.resetResources();
        this.SHA1 = Bytes.SHA1(buffer);
        this.size = buffer.length;
        this.data = buffer;
    }
    
    public void updateTimestamp() { this.timestamp = System.currentTimeMillis() / 1000; }
  
    @Override
    public String toString() {
        return String.format("FileEntry (%s g%d)", path, GUID);
    }
}
