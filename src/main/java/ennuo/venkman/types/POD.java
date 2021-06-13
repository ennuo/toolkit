package ennuo.venkman.types;

import ennuo.craftworld.types.FileEntry;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class POD {
    
    public File file;
    
    public ArrayList<FileEntry> entries = new ArrayList<FileEntry>();
    
    public POD(File file) {
        this.file = file;
        this.process();
    }
    
    private void process() {
        System.out.println("Started processing POD located at: " + this.file.getAbsolutePath());
        long begin = System.currentTimeMillis();
        
        int entryCount = 0;
        byte[] table = null;
        try {
            RandomAccessFile pod = new RandomAccessFile(this.file.getAbsolutePath(), "r");
            
            byte[] magicBytes = new byte[4];
            pod.readFully(magicBytes);
            String magic = new String(magicBytes, StandardCharsets.UTF_8);
            
            if (!magic.equals("POD6")) {
                System.err.println("This is not an acceptable file! Expected magic is POD6");
                return;
            }
            
            pod.seek(4);
            entryCount = pod.readInt();
            
            pod.seek(12);
            int tableOffset = pod.readInt();
            pod.read(table, tableOffset, (int) (this.file.length() - tableOffset));
            
            pod.close();
        } catch (IOException ex) {
            System.err.println("There was an error processing the POD file!");
            return;
        }
        
    long end = System.currentTimeMillis();
    System.out.println("Finished processing POD! (" + ((end - begin) / 1000L) + "s : " + (end - begin) + "ms)");
        
        
    }
    
    
}
