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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;

public class FileArchive {

    public static enum ArchiveType {
        FARC,
        FAR4,
        FAR5
    }

    public ArchiveType archiveType = ArchiveType.FARC;

    public File file;

    public boolean isParsed = false;
    public boolean shouldSave = false;

    public byte[] fat;
    public byte[] hashinate = new byte[0x14];

    public byte[] hashTable;
    public long tableOffset;

    public ArrayList<FileEntry> entries = new ArrayList<FileEntry>();
    public ArrayList<FileEntry> queue = new ArrayList<FileEntry>();
    
    public HashMap<String, FileEntry> lookup = new HashMap<String, FileEntry>();
    
    public int queueSize = 0;

    public FileArchive(File file) {
        this.file = file;
        process();
    }
    
    public void refresh() {
        this.hashTable = null;
        this.entries = new ArrayList<FileEntry>();
        this.queue = new ArrayList<FileEntry>();
        this.queueSize = 0;
        this.isParsed = false;
    }

    public void process() {
        System.out.println("Started processing FileArchive located at: " + this.file.getAbsolutePath());
        long begin = System.currentTimeMillis();
        
        this.refresh();
        
        boolean shouldPreload = false;
        byte[] preload = null;
        
        int entryCount = 0;
        try {
            RandomAccessFile fishArchive = new RandomAccessFile(this.file.getAbsolutePath(), "rw");
            if (fishArchive.length() < 8) {
                System.out.println("This is not a FileArchive.");
                fishArchive.close();
                this.isParsed = false;
                return;
            }
            fishArchive.seek(this.file.length() - 8);
            entryCount = fishArchive.readInt();

            byte[] magicBytes = new byte[4];
            fishArchive.readFully(magicBytes);
            String magic = new String(magicBytes, StandardCharsets.UTF_8);

            try {
                this.archiveType = ArchiveType.valueOf(magic);
            } catch (Exception e) {
                System.out.println(magic + " is not a valid FileArchive type.");
                fishArchive.close();
                this.isParsed = false;
                return;
            }

            System.out.println("Entry Count: " + entryCount);
            switch (this.archiveType) {
                case FARC:
                    this.tableOffset = this.file.length() - 0x8 - (entryCount * 0x1C);
                    break;
                case FAR4:
                    this.tableOffset = this.file.length() - 0x1C - (entryCount * 0x1C);
                    shouldPreload = true;
                    break;
                case FAR5:
                    this.tableOffset = this.file.length() - 0x20 - (entryCount * 0x1C);
                    shouldPreload = true;
                    break;
            }
           
            if (shouldPreload) {
                preload = new byte[(int) this.tableOffset];
                fishArchive.seek(0);
                fishArchive.readFully(preload, 0, preload.length);
            }
            
            this.hashTable = new byte[entryCount * 0x1C];

            fishArchive.seek(this.tableOffset);
            fishArchive.read(this.hashTable);

            if (this.archiveType != ArchiveType.FARC) {
                int fatSize = this.archiveType == ArchiveType.FAR4 ?
                    0x84 : 0xAC;
                fishArchive.seek(this.tableOffset - fatSize);
                this.fat = new byte[fatSize];
                fishArchive.read(this.fat);

                if (this.archiveType == ArchiveType.FAR4)
                    fishArchive.seek(this.file.length() - 0x1c);
                else
                    fishArchive.seek(this.file.length() - 0x20);

                fishArchive.read(this.hashinate);
            }

            fishArchive.close();
        } catch (IOException ex) {
            System.err.println(String.format("There was an error processing the %s file!", this.archiveType.name()));
            isParsed = false;
            return;
        }
        Data table = new Data(this.hashTable);
        this.entries = new ArrayList<FileEntry>(entryCount);
        this.lookup = new HashMap<String, FileEntry>(entryCount);
        for (int i = 0; i < entryCount; i++) {
            FileEntry entry = new FileEntry(table
                .bytes(20), table
                .uint32(), table
                .int32(), null);
            if (shouldPreload)
                entry.data = Arrays.copyOfRange(preload, (int) entry.offset, ((int) (entry.offset + entry.size)));
            this.entries.add(entry);
            this.lookup.put(Bytes.toHex(entry.hash), entry);
        }
        
        long end = System.currentTimeMillis();
        System.out.println(
            String.format("Finished processing %s! (%s s %s ms)",
                this.archiveType.name(),
                ((end - begin) / 1000),
                (end - begin))
        );
        this.isParsed = true;
    }

    public FileEntry find(byte[] hash) { return find(hash, false); }
    public FileEntry find(byte[] hash, boolean log) {
        String hashKey = Bytes.toHex(hash);
        if (this.lookup.containsKey(hashKey))
            return this.lookup.get(hashKey);
        if (log)
            System.out.println("Could not find entry with SHA1: " + Bytes.toHex(hash));
        return null;
    }

    public void add(byte[] data) {
        byte[] hash = Bytes.SHA1(data);
        if (find(hash, false) != null) return;
        
        this.queueSize += (0x1C + data.length);

        FileEntry entry = new FileEntry(data, hash);
        this.entries.add(entry);

        this.queue.add(new FileEntry(data, hash));
        this.lookup.put(Bytes.toHex(hash), entry);
        
        this.shouldSave = true;
    }

    public byte[] extract(byte[] hash) { return extract(find(hash)); }
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
            Logger.getLogger(FileArchive.class.getName()).log(Level.SEVERE, (String) null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileArchive.class.getName()).log(Level.SEVERE, (String) null, ex);
        }
        return null;
    }
    
    public void setFatDataSource(byte[] hash) {
        if (this.archiveType == ArchiveType.FARC || hash == null || hash.length != 0x14) return;
        if (this.fat == null)
            this.fat = new byte[(this.archiveType == ArchiveType.FAR4) ? 0x84 : 0xAC];
        int start = this.fat.length - 0x3C;
        for (int i = start; i < start + 0x14; ++i)
            this.fat[i] = hash[i - start];
    }
    
    public String getFatDataSource() {
        if (this.archiveType == ArchiveType.FARC || this.fat == null) return null;
        int start = this.fat.length - 0x3C;
        byte[] hash = new byte[0x14];
        for (int i = start; i < start + 0x14; ++i)
            hash[i - start] = this.fat[i];
        return Bytes.toHex(hash);
    }
    
    public byte[] build() {
        if (this.archiveType == ArchiveType.FARC) {
            System.out.println("FileArchive of type FARC shouldn't be built due to its size.");
            return null;
        }
        
        int dataSize = this.entries
                .stream()
                .mapToInt(element -> element.size)
                .reduce(0, (total, element) -> total + element);
        
        FileEntry[] entries = new FileEntry[this.entries.size()];
        entries = this.entries.toArray(entries);
        Arrays.sort(entries, (e1, e2) -> Bytes.toHex(e1.hash).compareTo(Bytes.toHex(e2.hash)));
        
        Output output = new Output(dataSize + (0x1C * this.entries.size()) + this.fat.length + 0x80);
        
        for (FileEntry entry : entries)
            output.bytes(entry.data);
        
        if (output.offset % 4 != 0)
            output.pad(4 - (output.offset % 4)); // padding for xxtea encryption
        
        output.bytes(this.fat);
        
        int lastBufferOffset = 0;
        for (FileEntry entry : entries) {
            output.bytes(entry.hash);
            output.int32(lastBufferOffset);
            output.int32(entry.size);
            lastBufferOffset += entry.size;
        }
        
        output.bytes(this.hashinate);
        if (this.archiveType == ArchiveType.FAR5)
            output.int32(0); // no idea what this is
        
        output.int32(entries.length);
        output.string(this.archiveType.name());
        
        this.queue.clear();
        this.queueSize = 0;
        
        output.shrinkToFit();
        return output.buffer;
    }
    
    public boolean save() { return this.save(null); }
    public boolean save(JProgressBar bar) {
        try {
            if (this.queue.size() == 0) {
                System.out.println("FileArchive has no items in queue, skipping save.");
                return true;
            }

            System.out.println("Saving FileArchive at " + this.file.getAbsolutePath());

            if (bar != null) {
                bar.setVisible(true);
                bar.setMaximum(this.queue.size());
                bar.setValue(0);
            }

            long offset = this.tableOffset;
            Output output = new Output(this.queueSize + this.hashTable.length + 0xFF, 0);

            for (int i = 0; i < this.queue.size(); ++i) {
                output.bytes(this.queue.get(i).data);
                if (bar != null) bar.setValue(i + 1);
            }

            if (this.archiveType != ArchiveType.FARC) {
                offset -= this.fat.length;
                if ((offset + output.offset) % 4 != 0)
                    output.pad(4 - (((int) offset + output.offset) % 4)); // padding for xxtea encryption
                output.bytes(this.fat);
            }

            for (int i = 0; i < this.queue.size(); ++i) {
                FileEntry entry = this.queue.get(i);
                output.bytes(entry.hash);
                output.int32((int) offset);
                output.int32(entry.size);
                if (bar != null) bar.setValue(i + 1);
            }

            output.bytes(this.hashTable);

            if (this.archiveType != ArchiveType.FARC)
                output.bytes(this.hashinate);

            if (this.archiveType == ArchiveType.FAR5)
                output.int32(0); // unsure what this is

            output.int32(this.entries.size());
            output.string(this.archiveType.toString());
            output.shrinkToFit();

            RandomAccessFile fileArchive = new RandomAccessFile(this.file.getAbsolutePath(), "rw");
            fileArchive.seek(offset);
            fileArchive.write(output.buffer);
            fileArchive.close();

            shouldSave = false;

            System.out.println("Successfully saved " + this.queue.size() + " entries to the FileArchive.");
            this.queue.clear();
            this.queueSize = 0;

            if (bar != null) {
                bar.setValue(0);
                bar.setMaximum(0);
                bar.setVisible(false);
            }

            process();

        } catch (IOException ex) {
            System.err.println("There was an error saving the FileArchive.");
            Logger.getLogger(FileArchive.class.getName()).log(Level.SEVERE, (String) null, ex);
            return false;
        }
        return true;
    }
}