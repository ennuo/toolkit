package ennuo.craftworld.types.indev;

import ennuo.craftworld.swing.FileData;
import java.io.File;

public class FileSave extends FileData {
    public int revision;
    public File directory;
    public boolean isVita = false;
    
    public FileSave(File folder) {
        this.name = "Savedata";
        this.type = "File Save";
        this.directory = folder;
        if (!folder.exists()) return;
        
        this.getLocalSave();
        this.getBigSave();
        this.getMoonSlots();
        this.getDownloadSlots();
    }
    
    private void getLocalSave() {
        File[] localSaves = this.directory.listFiles((dir, name) -> name.matches("littlefart\\d+"));
        for (File file : localSaves)
            System.out.println(String.format("Found local save file: %s", file.getName()));
    }
    
    private void getBigSave() {
        File[] bigSaves = this.directory.listFiles((dir, name) -> name.matches("bigfart\\d+"));
        for (File file : bigSaves)
            System.out.println(String.format("Found big save file: %s", file.getName()));
    }
    
    private void getMoonSlots() {
        File[] moonSaves = this.directory.listFiles((dir, name) -> name.matches("moon\\d+_\\d+"));
        for (File file : moonSaves)
            System.out.println(String.format("Found moon save file: %s", file.getName()));
    }
    
    private void getDownloadSlots() {
        File[] downloadSaves = this.directory.listFiles((dir, name) -> name.matches("slot\\d+_\\d+"));
        for (File file : downloadSaves)
            System.out.println(String.format("Found download save file: %s", file.getName()));
    }
}
