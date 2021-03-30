package ennuo.craftworld.types;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Data;
import ennuo.craftworld.memory.Output;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;

public class FileArchive {
    
  public enum ArchiveType {
      FARC,
      FAR4,
      FAR5
  } 
   
  public File file;
  
  public boolean isParsed = false;
  public boolean shouldSave = false;
  
  public byte[] hashTable;
  public long tableOffset;
  
  public ArrayList<FileEntry> entries = new ArrayList<FileEntry>();
  public ArrayList<FileEntry> queue = new ArrayList<FileEntry>();
  public int queueSize = 0;
  
  public FileArchive(File file) {
    this.file = file;
    process();
  }
  
  public void process() {
    System.out.println("Started processing FARC located at: " + this.file.getAbsolutePath());
    long begin = System.currentTimeMillis();
    hashTable = null; entries = new ArrayList<FileEntry>();
    queue = new ArrayList<FileEntry>(); queueSize = 0; isParsed = false;
    int entryCount = 0;
    try {
      RandomAccessFile fishArchive = new RandomAccessFile(this.file.getAbsolutePath(), "rw");
      if (fishArchive.length() < 8) {
          System.out.println("This is not a FARC file.");
          fishArchive.close();
          isParsed = false;
          return;
      }
      fishArchive.seek(this.file.length() - 8L);
      entryCount = fishArchive.readInt();

      byte[] magicBytes = new byte[4];
      fishArchive.readFully(magicBytes);
      String magic = new String(magicBytes, StandardCharsets.UTF_8);
      
      ArchiveType type;
      try { type = ArchiveType.valueOf(magic); }
      catch (Exception e) {
          System.out.println(magic + " is not a valid FileArchive type.");
          fishArchive.close();
          isParsed = false;
          return;
      }
      
        if (type != ArchiveType.FARC) {
          System.out.println(magic + " is not a valid FileArchive type.");
          fishArchive.close();
          isParsed = false;
          return;
        }
      
      System.out.println("Entry Count: " + entryCount);
      this.tableOffset = this.file.length() - 8L - (entryCount * 28);
      this.hashTable = new byte[(int)(this.file.length() - this.tableOffset - 8L)];
      fishArchive.seek(this.tableOffset);
      fishArchive.read(this.hashTable);
      fishArchive.close();
    } catch (IOException ex) {
      System.err.println("There was an error processing the FARC file!");
      isParsed = false;
      return;
    }
    Data table = new Data(this.hashTable);
    this.entries = new ArrayList<FileEntry>(entryCount);
    for (int i = 0; i < entryCount; i++)
      this.entries.add(new FileEntry(table
          .bytes(20), table
          .uint32(), table
          .int32(), null)); 
    long end = System.currentTimeMillis();
    System.out.println("Finished processing FARC! (" + ((end - begin) / 1000L) + "s : " + (end - begin) + "ms)");
    isParsed = true;
  }
  
  public FileEntry find(byte[] hash) { return find(hash, false); }
  public FileEntry find(byte[] hash, boolean log) {
    for (int i = 0; i < this.entries.size(); i++) {
      if (Arrays.equals(hash, (this.entries.get(i)).hash))
        return this.entries.get(i); 
    } 
    if (log)
        System.out.println("Could not find entry with SHA1: " + Bytes.toHex(hash));
    return null;
  }
  
  public void add(byte[] data) {
      byte[] hash = Bytes.SHA1(data);
      if (find(hash, false) != null) return;
      queueSize += (0x1C + data.length);
      
      FileEntry entry = new FileEntry(data, hash);
      this.entries.add(entry);
      
      queue.add(new FileEntry(data, hash));
      shouldSave = true;
  }
  
  public byte[] extract(byte[] hash) {
    return extract(find(hash));
  }
  
  public byte[] extract(FileEntry entry) {
    if (entry == null)
      return null; 
    if (entry.data != null) return entry.data;
    try {
      RandomAccessFile fishArchive = new RandomAccessFile(this.file.getAbsolutePath(), "rw");
      fishArchive.seek(entry.offset);
      byte[] buffer = new byte[entry.size];
      fishArchive.read(buffer);
      fishArchive.close();
      entry.data = buffer;
      return buffer;
    } catch (FileNotFoundException ex) {
      Logger.getLogger(FileArchive.class.getName()).log(Level.SEVERE, (String)null, ex);
    } catch (IOException ex) {
      Logger.getLogger(FileArchive.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    return null;
  }
  
  public boolean save() { return save(null); }
  public boolean save(JProgressBar bar) {
    try {
      if (queue.size() == 0) {
          System.out.println("FileArchive has no items in queue, skipping save.");
          return true;
      }
      
      System.out.println("Saving FileArchive at " + file.getAbsolutePath());
      
      if (bar != null) {
        bar.setVisible(true);
        bar.setMaximum(queue.size());
        bar.setValue(0);
      }
      
      long offset = this.tableOffset;
      Output output = new Output(queueSize + this.hashTable.length + 0x8, 0);
      for (int i = 0; i < queue.size(); ++i) {
          output.bytes(queue.get(i).data);
          if (bar != null) bar.setValue(i + 1);
      }
      for (int i = 0; i < queue.size(); ++i) {
        FileEntry entry = queue.get(i);
        output.bytes(entry.hash);
        output.int32((int) offset);
        output.int32(entry.size);
        offset += entry.size;
        if (bar != null) bar.setValue(i + 1);
      } 
      output.bytes(this.hashTable);
      output.int32(this.entries.size());
      output.string("FARC");
      
      RandomAccessFile fileArchive = new RandomAccessFile(this.file.getAbsolutePath(), "rw");
      fileArchive.seek(tableOffset);
      fileArchive.write(output.buffer);
      fileArchive.close();
      
      shouldSave = false;
      
      System.out.println("Successfully saved " + queue.size() + " entries to the FileArchive.");
      queue.clear(); queueSize = 0;
      
      if (bar != null) {
        bar.setValue(0); bar.setMaximum(0);
        bar.setVisible(false);
      }
      
      process();
      
    } catch (IOException ex) {
      System.err.println("There was an error saving the FileArchive.");
      Logger.getLogger(FileArchive.class.getName()).log(Level.SEVERE, (String)null, ex);
      return false;
    } 
    return true;
  }
}
