package ennuo.craftworld.types;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JProgressBar;

public class FileDB extends FileData {
  public boolean isLBP3;
  public int entryCount;
  public int header;
  
  public boolean isParsed = false;
  
  private int pathSize = 0;
  
  public ArrayList<FileEntry> entries;
  
  public FileDB(File file, JProgressBar progress) {
 
      File parent = file.getParentFile().getParentFile();
      if (parent.getName().toUpperCase().equals("USRDIR"))
        USRDIR = parent.getAbsolutePath() + "\\";
      
      this.path = file.getAbsolutePath();
      this.name = file.getName();
      type = "FileDB";
      process(new Data(path), progress);
  }
  
  
  public FileDB(File file) {
    this.path = file.getAbsolutePath(); 
    this.name = file.getName();
    type = "FileDB";
    process(new Data(path), null);
  }
  
  public void process(Data data, JProgressBar bar) {
    System.out.println("Started processing FileDB located at: " + this.path);
    long begin = System.currentTimeMillis();
    header = data.int32();
    isLBP3 = isLBP3(this.header);
    entryCount = data.int32();
    if (bar != null) {
        bar.setVisible(true);
        bar.setMaximum(entryCount);
        bar.setValue(0);
    }
    System.out.println("Entry Count: " + entryCount);
    entries = new ArrayList<>(entryCount);
    model = new FileModel(new FileNode("FILEDB", null, null));
    root = (FileNode)model.getRoot();
    for (int i = 0; i < entryCount; i++) {
      String path = data.str(isLBP3 ? data.int16() : data.int32());
      pathSize += path.length();
      if (!isLBP3)
        data.forward(4); 
      int timestamp = data.int32();
      int size = data.int32();
      byte[] hash = data.bytes(20);
      long guid = data.uint32();
      if (path.startsWith(".")) {
          String newPath = "data/";
          switch (path.toLowerCase()) {
              case ".slt": newPath += "slots/"; break;
              case ".tex": newPath += "textures/"; break;
              case ".bpr": case ".ipr":
                  newPath += "profiles/"; break;
              case ".mol": newPath += "models/"; break;
              case ".gmat": newPath += "gfx/"; break;
              case ".mat": newPath += "materials/"; break;
              case ".ff": newPath += "scripts/"; break;
              case ".plan": newPath += "plans/"; break;
              case ".pal": newPath += "palettes/"; break;
              case ".oft": newPath += "outfits/"; break;
              case ".sph": newPath += "skeletons/"; break;
              case ".bin": newPath += "levels/"; break;
              case ".vpo": newPath += "shaders/vertex/"; break;
              case ".fpo": newPath += "shaders/fragment/"; break;
              case ".anim": newPath += "animations/"; break;
              case ".bev": newPath += "bevels/"; break;
              case ".smh": newPath += "static_meshes/"; break;
              case ".mus": newPath += "audio/settings/"; break;
              case ".fsb": newPath += "audio/music/"; break;
              case ".txt": newPath += "text/"; break;
          }
          newPath += Bytes.toHex(hash).toLowerCase() + path;
          pathSize += newPath.length();
          path = newPath;
      }
      if (guid > lastGUID && guid < 0x00180000)
          lastGUID = guid;
      entries.add(new FileEntry(path, timestamp, size, hash, guid));
      if (!isHidden(path))
        addNode(entries.get(i)); 
      if (bar != null) bar.setValue(i + 1);
    } 
    if (bar != null) {
        bar.setValue(0); bar.setMaximum(0);
        bar.setVisible(false);
    }
    long end = System.currentTimeMillis();
    System.out.println("Finished processing FileDB! (" + ((end - begin) / 1000L) + "s : " + (end - begin) + "ms)");
    isParsed = true;
  }
  
  public FileEntry find(long guid) {
    for (int i = 0; i < entries.size(); i++) {
      if (((FileEntry)entries.get(i)).GUID == guid)
        return entries.get(i); 
    } 
    return null;
  }
  
  public void rename(FileEntry entry, String name) {
      pathSize -= entry.path.length();
      entry.path = name;
      pathSize += name.length();
  }
  
  public FileEntry[] findAll(byte[] hash) {
      ArrayList<FileEntry> results = new ArrayList<FileEntry>();
      for (int i = 0; i < entries.size(); ++i)
        if (Arrays.equals(((FileEntry)entries.get(i)).hash, hash))
            results.add(entries.get(i));
      return (FileEntry[]) results.toArray(new FileEntry[results.size()]);
  }
  
  public FileEntry find(byte[] hash) {
    for (int i = 0; i < entries.size(); i++) {
      if (Arrays.equals(((FileEntry)entries.get(i)).hash, hash))
        return entries.get(i); 
    } 
    return null;
  }
  
  public int indexOf(long guid) {
    for (int i = 0; i < entries.size(); i++) {
      if (((FileEntry)entries.get(i)).GUID == guid)
        return i; 
    } 
    return -1;
  }
  
  public long getNextGUID() {
      lastGUID++;
      return lastGUID;
  }
  
  public boolean add(FileEntry entry) {
    entry.timestamp = System.currentTimeMillis() / 1000L;
    pathSize += entry.path.length();
    int index = indexOf(entry.GUID);
    if (index == -1) {
      entries.add(entry);
      entryCount++;
      shouldSave = true;
      return true;
    }
    FileEntry old = entries.get(index);
    old.hash = entry.hash;
    old.size = entry.size;
    old.data = entry.data;
    old.timestamp = entry.timestamp;
    old.path = entry.path;
    old.resetResources();
    shouldSave = true;
    return false;
  }
  
  public boolean remove(long guid) {
    try {
      int entry = indexOf(guid);
      entries.remove(entry);
      shouldSave = true;
      entryCount--;
    } catch (IndexOutOfBoundsException ex) {
      return false;
    } 
    return true;
  }
  
  public boolean remove(FileEntry entry) {
    try {
      entries.remove(entry);
      shouldSave = true;
      entryCount--;
    } catch (IndexOutOfBoundsException ex) {
      return false;
    } 
    return true;
  }
  
  public void replace(FileEntry entry, byte[] buffer) {
    shouldSave = true;
    entry.hash = Bytes.SHA1(buffer);
    entry.size = buffer.length;
    entry.data = buffer;
    entry.resetResources();
    entry.timestamp = System.currentTimeMillis() / 1000L;
  }
  
  public void zero(FileEntry entry) {
    shouldSave = true;
    entry.hash = new byte[20];
  }
  
  public boolean replace(FileEntry entry, long guid) {
    int index = indexOf(guid);
    if (index != -1) {
      //FileEntry selected = entries.get(index);
      entries.set(index, entry);
      shouldSave = true;
      return true;
    } 
    return false;
  }
  
  public String toRLST() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < entryCount; i++) {
      if (((FileEntry)entries.get(i)).path.contains(".plan"))
        builder.append(((FileEntry)entries.get(i)).path + "\n"); 
    } 
    return builder.toString();
  }
  
  @Override
  public boolean save(String path) {
      Output output = new Output(0x8 + (0x28 * entries.size()) + pathSize, 0);
      output.int32(header);
      output.int32(entries.size());
      for (int i = 0; i < entries.size(); ++i) {
          FileEntry entry = entries.get(i);
          if (isLBP3) output.int16((short) entry.path.length());
          else output.int32(entry.path.length());
          output.string(entry.path);
          if (!isLBP3) output.int32(0);
          output.uint32(entry.timestamp);
          output.int32(entry.size);
          output.bytes(entry.hash);
          output.uint32(entry.GUID);
      }
      output.shrinkToFit();
      if (FileIO.write(output.buffer, path)) {
          if (path.equals(this.path))
            shouldSave = false;
          return true;
      }
      return false;
  }
  
  public static boolean isLBP3(int header) {
    switch (header) {
      case 256:
        System.out.println("Detected: LBP1/2 .MAP File");
        return false;
      case 21496064:
        System.out.println("Detected: LBP3 .MAP File");
        return true;
      case 936:
        System.out.println("Detected LBP Vita .MAP File");
        return false;
    } 
    System.out.println("Detected Unknown .MAP File");
    return false;
  }
  
  public static boolean isHidden(String entry) {
    /*
    return (entry.contains(".farc") || entry.contains(".sdat") || entry
      .contains(".edat") || entry.contains(".bik") || entry
      .contains(".fnt") || entry.contains(".fev") || entry
      .equals("")|| entry.contains(".fsb"));
    */
    
    return (entry.contains(".farc") || entry.contains(".sdat") || entry.contains(".sdat") || entry.equals("") || entry.contains("empty_video.bik"));
  }
}
