package ennuo.craftworld.types.indev;

import ennuo.craftworld.swing.FileData;
import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;

public class FileSave extends FileData {
    public int revision;
    public File directory;
    public boolean isVita = false;
    
    public BigProfile bigProfile;
    public LocalProfile localProfile;
    
    public HashMap<String, byte[]> resources;
    
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
    
    private void getLocalSave() {
        Pattern regex = Pattern.compile("littlefart\\d+");
        File[] localSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : localSaves)
            System.out.println(String.format("Found local save file: %s", file.getName()));
    }
    
    private void getBigSave() {
        Pattern regex = Pattern.compile("bigfart\\d+");
        File[] bigSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : bigSaves)
            System.out.println(String.format("Found big save file: %s", file.getName()));
    }
    
    private void getMoonSlots() {
        Pattern regex = Pattern.compile("moon\\d+_\\d+");
        File[] moonSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : moonSaves)
            System.out.println(String.format("Found moon save file: %s", file.getName()));
    }
    
    private void getDownloadSlots() {
        Pattern regex = Pattern.compile("slot\\d+_\\d+");
        File[] downloadSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : downloadSaves)
            System.out.println(String.format("Found download save file: %s", file.getName()));
    }
}
