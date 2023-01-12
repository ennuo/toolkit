package cwlib.types.archives;

import cwlib.enums.ArchiveType;
import cwlib.ex.SerializationException;
import cwlib.types.data.SHA1;
import cwlib.io.streams.MemoryInputStream;
import cwlib.util.Bytes;
import cwlib.util.FileIO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileArchive extends Fart {
    public FileArchive(File file) {
        super(file, ArchiveType.FARC);
        byte[] fatTable = null;

        if (!this.file.exists())
            FileIO.write(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x46, 0x41, 0x52, 0x43 }, this.file.getAbsolutePath());

        try (RandomAccessFile archive = new RandomAccessFile(this.file.getAbsolutePath(), "r")) {
            if (archive.length() < 0x8) {
                archive.close();
                throw new SerializationException("Invalid FARC, size is less than minimum of 8 bytes!");
            }

            archive.seek(archive.length() - 0x8); // Seek to the bottom of the archive to read entry count and magic.

            int entryCount = archive.readInt();
            this.entries = new Fat[entryCount];
            if (archive.readInt() != 0x46415243 /* FARC */)
                throw new SerializationException("Invalid FARC, magic does not match!");
            this.fatOffset = archive.length() - 0x8 - (entryCount * 0x1c);

            fatTable = new byte[entryCount * 0x1c];
            
            archive.seek(this.fatOffset);
            archive.read(fatTable);
        } catch (IOException ex) {
            throw new SerializationException("An I/O error occurred while reading the FARC.");
        }

        // Faster to read the fat table in-memory since it's small.
        MemoryInputStream stream = new MemoryInputStream(fatTable);
        for (int i = 0; i < this.entries.length; ++i) {
            Fat fat = new Fat(this, stream.sha1(), stream.u32(), stream.i32());
            this.entries[i] = fat;
            this.lookup.put(fat.getSHA1(), fat);
        }
    }
    
    public FileArchive(String path) { this(new File(path)); }

    @Override public boolean save() {
        // The FARC is way too massive to build every time
        // you save, so we only ever save when there's
        // data that needs to be added.
        
        long size = this.getQueueSize();

        // This usually shouldn't be an issue, but since
        // we're using a stream to write file data, we should
        // check if we have enough space to actually write it.
        long neededSpace = size + this.queue.size() * 0x1c;
        if (this.file.getFreeSpace() < neededSpace)
            return false;

        Fat[] fat = new Fat[this.entries.length + this.queue.size()];

        SHA1[] hashes = this.queue.keySet().toArray(SHA1[]::new);
        byte[][] buffers = new byte[hashes.length][];

        // Create a new FAT table with these new entries
        // appended at the end.
        long offset = this.fatOffset;
        for (int i = 0; i < this.entries.length; ++i)
            fat[i] = this.entries[i];
        for (int i = this.entries.length, j = 0; i < fat.length; ++i, ++j) {
            buffers[j] = this.queue.get(hashes[j]);
            fat[i] = new Fat(this, hashes[j], offset, buffers[j].length);
            offset += buffers[j].length;
        }

        byte[] table = Fart.generateFAT(fat);
        try (RandomAccessFile archive = new RandomAccessFile(this.file.getAbsolutePath(), "rw")) {
            archive.seek(this.fatOffset);

            for (byte[] buffer : buffers)
                archive.write(buffer);
            archive.write(table);

            // Footer
            archive.write(Bytes.toBytesBE(fat.length));
            archive.write(new byte[] { 0x46, 0x41, 0x52, 0x43 }); // FARC

            archive.setLength(archive.getFilePointer());
        } catch (IOException ex) { return false; }
        

        // Update state of the archive in memory.
        this.entries = fat;
        this.queue.clear();
        this.lookup.clear();
        for (Fat row : this.entries)
            this.lookup.put(row.getSHA1(), row);
        this.fatOffset = offset;
        this.lastModified = this.file.lastModified();

        return true;
    }
    
}
