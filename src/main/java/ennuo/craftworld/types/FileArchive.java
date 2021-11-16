package ennuo.craftworld.types;

import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.Revision;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.serializer.Output;
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
    public static byte[] HASHINATE_KEY = {
        0x2A, (byte) 0xFD, (byte) 0xA3, (byte) 0xCA, (byte) 0x86, 0x02, 0x19, (byte) 0xB3, (byte) 0xE6, (byte) 0x8A, (byte) 0xFF, (byte) 0xCC, (byte) 0x82, (byte) 0xC7, 0x6B, (byte) 0x8A,
        (byte) 0xFE, 0x0A, (byte) 0xD8, 0x13, 0x5F, 0x60, 0x47, 0x5B, (byte) 0xDF, 0x5D, 0x37, (byte) 0xBC, 0x57, 0x1C, (byte) 0xB5, (byte) 0xE7, 
        (byte) 0x96, (byte) 0x75, (byte) 0xD5, 0x28, (byte) 0xA2, (byte) 0xFA, (byte) 0x90, (byte) 0xED, (byte) 0xDF, (byte) 0xA3, 0x45, (byte) 0xB4, 0x1F, (byte) 0xF9, 0x1F, 0x25,
        (byte) 0xE7, 0x42, 0x45, 0x3B, 0x2B, (byte) 0xB5, 0x3E, 0x16, (byte) 0xC9, 0x58, 0x19, 0x7B, (byte) 0xE7, 0x18, (byte) 0xC0, (byte) 0x80
    };
    
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

    public byte[] hashTable;
    public long tableOffset;

    public ArrayList<FileEntry> entries = new ArrayList<FileEntry>();
    public ArrayList<FileEntry> queue = new ArrayList<FileEntry>();
    
    public HashMap<SHA1, FileEntry> lookup = new HashMap<SHA1, FileEntry>();
    
    public int queueSize = 0;

    public FileArchive() {
        this.archiveType = ArchiveType.FAR4;
        this.refresh();
        this.isParsed = true;
    }
    
    public FileArchive(File file) {
        this.file = file;
        process();
    }
    
    public void refresh() {
        this.hashTable = new byte[0];
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
                    System.out.println("FAR5 has been temporarily diabled.");
                    fishArchive.close();
                    this.isParsed = false;
                    return;
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
            }

            fishArchive.close();
        } catch (IOException ex) {
            System.err.println(String.format("There was an error processing the %s file!", this.archiveType.name()));
            isParsed = false;
            return;
        }
        Data table = new Data(this.hashTable);
        this.entries = new ArrayList<FileEntry>(entryCount);
        this.lookup = new HashMap<SHA1, FileEntry>(entryCount);
        for (int i = 0; i < entryCount; i++) {
            FileEntry entry = new FileEntry(
                 table.sha1(), 
                 table.u32(), 
                 table.i32(), 
                 null);
            if (shouldPreload)
                entry.data = Arrays.copyOfRange(preload, (int) entry.offset, ((int) (entry.offset + entry.size)));
            this.entries.add(entry);
            this.lookup.put(entry.hash, entry);
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

    public FileEntry find(SHA1 hash) { return find(hash, false); }
    public FileEntry find(SHA1 hash, boolean log) {
        if (this.lookup.containsKey(hash))
            return this.lookup.get(hash);
        if (log)
            System.out.println("Could not find entry with SHA1: " + hash.toString());
        return null;
    }

    public void add(byte[] data) {
        SHA1 hash = SHA1.fromBuffer(data);
        if (this.find(hash, false) != null) return;
        
        this.queueSize += (0x1C + data.length);

        FileEntry entry = new FileEntry(data, hash);
        this.entries.add(entry);

        this.queue.add(entry);
        this.lookup.put(hash, entry);
        
        this.shouldSave = true;
    }
    
    public void add(FileEntry entry) {
        if (entry.data == null) return;
        SHA1 hash = SHA1.fromBuffer(entry.data);
        entry.hash = hash;
        entry.hash = hash;
        if (this.find(hash, false) != null) return;
        this.entries.add(entry);
        this.queueSize += (0x1C + entry.data.length);
        this.queue.add(entry);
        this.lookup.put(hash, entry);
        this.shouldSave = true;
    }

    public byte[] extract(SHA1 hash) { return this.extract(this.find(hash)); }
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
    
    public void preload() {
        byte[] preload = null;
        try (RandomAccessFile archive = new RandomAccessFile(this.file.getAbsolutePath(), "r")) {
            preload = new byte[(int) this.tableOffset];
            archive.seek(0);
            archive.readFully(preload, 0, preload.length);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileArchive.class.getName()).log(Level.SEVERE, (String) null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileArchive.class.getName()).log(Level.SEVERE, (String) null, ex);
        }
        if (preload != null)
            for (FileEntry entry : this.entries)
                entry.data = Arrays.copyOfRange(preload, (int) entry.offset, ((int) (entry.offset + entry.size)));
    }
    
    public void setFatResourceType(ResourceType type) {
        if (this.fat == null) this.setFatDataSource(new SHA1());
        System.arraycopy(Bytes.toBytes(type.value), 0, this.fat, this.fat.length - 0x4c, 4);
    }
    
    public void setFatRevision(Revision revision) {
        if (this.fat == null) this.setFatDataSource(new SHA1());
        boolean isVita = revision.isVita();
        byte[] head = (isVita) ? Bytes.toBytesLE(revision.head) : Bytes.toBytes(revision.head);
        int branchDescription = revision.branchID << 0x10 | revision.branchRevision;
        byte[] branch = (isVita) ? Bytes.toBytesLE(branchDescription) : Bytes.toBytes(branchDescription);
        System.arraycopy(head, 0, this.fat, 0, 4);
        System.arraycopy(branch, 0, this.fat, 4, 4);
    }
    
    public void setFatDataSource(SHA1 hash) {
        if (this.archiveType == ArchiveType.FARC || hash == null) return;
        if (this.fat == null)
            this.fat = new byte[(this.archiveType == ArchiveType.FAR4) ? 0x84 : 0xAC];
        int start = this.fat.length - 0x3C;
        byte[] hashBuffer = hash.getHash();
        for (int i = start; i < start + 0x14; ++i)
            this.fat[i] = hashBuffer[i - start];
    }
    
    public void swapFatEndianness() {
        if (this.archiveType != ArchiveType.FAR4) return;
        Bytes.swap32(this.fat, 0x00);
        Bytes.swap32(this.fat, 0x34);
        Bytes.swap32(this.fat, 0x38);
    }
    
    public SHA1 getFatDataSource() {
        if (this.archiveType == ArchiveType.FARC || this.fat == null) return null;
        int start = this.fat.length - 0x3C;
        byte[] hash = new byte[0x14];
        for (int i = start; i < start + 0x14; ++i)
            hash[i - start] = this.fat[i];
        return new SHA1(hash);
    }
    
    public byte[] build() {
        if (this.archiveType == ArchiveType.FARC) {
            System.out.println("FileArchive of type FARC shouldn't be built due to its size.");
            return null;
        }
        
        if (this.fat == null) this.setFatDataSource(new SHA1());
        
        int dataSize = this.entries
                .stream()
                .mapToInt(element -> element.size)
                .reduce(0, (total, element) -> total + element);
        
        FileEntry[] entries = new FileEntry[this.entries.size()];
        entries = this.entries.toArray(entries);
        Arrays.sort(entries, (e1, e2) -> e1.hash.toString().compareTo(e2.hash.toString()));
        
        Output output = new Output(dataSize + (0x1C * this.entries.size()) + this.fat.length + 0x80);
        
        for (FileEntry entry : entries)
            output.bytes(entry.data);
        
        if (output.offset % 4 != 0)
            output.pad(4 - (output.offset % 4)); // padding for xxtea encryption
        
        output.bytes(this.fat);
        
        int lastBufferOffset = 0;
        for (FileEntry entry : entries) {
            output.sha1(entry.hash);
            output.i32(lastBufferOffset);
            output.i32(entry.size);
            lastBufferOffset += entry.size;
        }
        
        int hashinateOffset = output.offset;
        output.bytes(new byte[0x14]); // Leaving this hash null until we calculate it
        if (this.archiveType == ArchiveType.FAR5)
            output.i32(0); // no idea what this is
        
        output.i32(entries.length);
        output.str(this.archiveType.name());
        
        this.queue.clear();
        this.queueSize = 0;
        
        output.shrink();
        
        byte[] SHA1 = Bytes.computeSignature(output.buffer, FileArchive.HASHINATE_KEY);
        System.arraycopy(SHA1, 0, output.buffer, hashinateOffset, 0x14);
        
        return output.buffer;
    }
    
    public boolean save() { return this.save(null, false); }
    public boolean save(JProgressBar bar) { return this.save(bar, false); }
    public boolean save(JProgressBar bar, boolean force) {
        if (this.file == null) return false;
        try {
            if (this.queue.size() == 0 && !force) {
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
                if (this.fat == null) this.setFatDataSource(new SHA1());
                offset -= this.fat.length;
                if ((offset + output.offset) % 4 != 0)
                    output.pad(4 - (((int) offset + output.offset) % 4)); // padding for xxtea encryption
                output.bytes(this.fat);
            }

            int bufferOffset = (int) this.tableOffset;
            for (int i = 0; i < this.queue.size(); ++i) {
                FileEntry entry = this.queue.get(i);
                output.sha1(entry.hash);
                output.i32(bufferOffset);
                output.i32(entry.size);
                bufferOffset += entry.size;
                if (bar != null) bar.setValue(i + 1);
            }

            output.bytes(this.hashTable);

            if (this.archiveType != ArchiveType.FARC)
                output.bytes(new byte[0x14]); // Hashinate, the profiles generally only need this when they call build

            if (this.archiveType == ArchiveType.FAR5)
                output.i32(8); // unsure what this is

            output.i32(this.entries.size());
            output.str(this.archiveType.toString());
            output.shrink();

            RandomAccessFile fileArchive = new RandomAccessFile(this.file.getAbsolutePath(), "rw");
            fileArchive.seek(offset);
            fileArchive.write(output.buffer);
            fileArchive.setLength(offset + output.buffer.length);
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