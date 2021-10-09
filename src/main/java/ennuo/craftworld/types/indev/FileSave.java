package ennuo.craftworld.types.indev;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileEntry;
import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;

public class FileSave extends FileData {
    public int revision;
    public File directory;
    public boolean isVita = false;
    
    public BigProfile bigProfile;
    public LocalProfile localProfile;
    
    public HashMap<String, byte[]> resources = new HashMap<String, byte[]>();
    
    public FileSave(File folder) {
        this.name = "Savedata";
        this.type = "File Save";
        this.directory = folder;
        if (!folder.exists()) return;
        
        this.getLocalSave();
        this.getBigSave();
        if (this.bigProfile != null) {
            this.getMoonSlots();
            this.getDownloadSlots();   
        }
    }
    
    private String appendArchive(File file) {
        FileArchive archive = new FileArchive(file);
        if (archive.isParsed) {
            for (FileEntry entry : archive.entries)
                this.resources.put(Bytes.toHex(entry.hash), entry.data);
            return archive.getFatDataSource();
        }
        return null;
    }
    
    private void getLocalSave() {
        Pattern regex = Pattern.compile("littlefart\\d+");
        File[] localSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        if (localSaves.length == 0) {
            System.out.println("Couldn't find any local profiles in this directory!");
            return;
        } else if (localSaves.length != 1) 
            System.out.println(String.format("Multiple local profiles found, defaulting to %s", localSaves[0].getName()));
        else System.out.println(String.format("Found local save file: %s", localSaves[0].getName())); 
        String localProfileHash = this.appendArchive(localSaves[0]);
    }
    
    private void getBigSave() {
        Pattern regex = Pattern.compile("bigfart\\d+");
        File[] bigSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : bigSaves) {
            FileArchive archive = new FileArchive(file);
            System.out.println(String.format("Found big save file: %s", file.getName()));   
        }
    }
    
    private void getMoonSlots() {
        Pattern regex = Pattern.compile("moon\\d+_\\d+");
        File[] moonSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : moonSaves) {
            FileArchive archive = new FileArchive(file);
            System.out.println(String.format("Found moon save file: %s", file.getName()));   
        }
    }
    
    private void getDownloadSlots() {
        Pattern regex = Pattern.compile("slot\\d+_\\d+");
        File[] downloadSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : downloadSaves) {
            FileArchive archive = new FileArchive(file);
            System.out.println(String.format("Found download save file: %s", file.getName()));   
        }
    }
}
