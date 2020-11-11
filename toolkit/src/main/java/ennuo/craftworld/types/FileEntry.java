package ennuo.craftworld.types;

import ennuo.craftworld.resources.Mesh;
import ennuo.craftworld.resources.Pack;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.structs.ProfileItem;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.things.InventoryItem;
import java.util.ArrayList;
import java.util.Date;

public class FileEntry {
  public static byte[] EMPTY_HASH = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    
    
  public String path;
  public long timestamp = 0;
  public int size = 0;
  public long offset = -1;
  public byte[] hash = EMPTY_HASH;
  public long GUID = -1;
  public byte[] data = null;
  
  public int revision = 0;
  
  public Mesh mesh;
  public Texture texture;
  public InventoryItem item;
  
  public Slot slot;
  public Pack pack;
  public ArrayList<Slot> slots;
  public ArrayList<ProfileItem> items;
  
  public ProfileItem profileItem;
  
  public FileModel dependencyModel;
  public FileEntry[] dependencies;
  public boolean missingDependencies = true;
  
  public boolean canReplaceDecompressed = false;
  
  public void resetResources() { resetResources(true); }
  public void resetResources(boolean resetBigProfile) {
      dependencyModel = null;
      dependencies = null;
      missingDependencies = true;
      items = null;
      slots = null;
      
      if (resetBigProfile) {
        slot = null;
        profileItem = null;
      }
      
      pack = null;
      mesh = null;
      texture = null;
      item = null;
  }
  
  public FileEntry(String path) {
      this.path = path;
      this.timestamp = System.currentTimeMillis() / 1000L;
  }
  
  public FileEntry(String path, long GUID) {
      this.path = path;
      this.GUID = GUID;
      this.timestamp = System.currentTimeMillis() / 1000L;
  }
  
  
  public FileEntry(String path, int size, long GUID) {
      this.path = path;
      this.size = size;
      this.GUID = GUID;
      this.timestamp = System.currentTimeMillis() / 1000L;
  }
  
  public FileEntry(byte[] data, byte[] hash) {
      this.data = data;
      this.size = data.length;
      this.hash = hash;
      this.timestamp = System.currentTimeMillis() / 1000L;
  }
  
  public FileEntry(String path, int timestamp, int size, byte[] hash, long guid, byte[] data) {
    this.path = path;
    this.timestamp = timestamp;
    this.size = size;
    this.hash = hash;
    this.GUID = guid;
    this.data = data;
  }
  
  public FileEntry(String path, int timestamp, int size, byte[] hash, long guid) {
    this.path = path;
    this.timestamp = timestamp;
    this.size = size;
    this.hash = hash;
    this.GUID = guid;
  }
  
  public FileEntry(byte[] hash, long offset, int size, byte[] data) {
    this.hash = hash;
    this.offset = offset;
    this.size = size;
    this.data = data;
    this.timestamp = System.currentTimeMillis() / 1000L;
  }
  
  public FileEntry(FileEntry entry) {
    this.path = entry.path;
    if (entry.hash != null)
      this.hash = entry.hash.clone();
    this.offset = entry.offset;
    this.size = entry.size;
    if (entry.data != null)
        this.data = entry.data.clone();
    this.timestamp = System.currentTimeMillis() / 1000L;
    this.GUID = entry.GUID;
  }
  
  @Override
  public String toString() {
      return String.format("FileEntry (%s g%d)", path, GUID);
  }
  
}
