package ennuo.craftworld.types.indev;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileEntry;
import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * A save manager for both Big and Local Profiles, as well as downloaded levels.
 */
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
        this.getBigSaves();
        
        // NOTE(Jun): We check if RBigProfile exists because both of these save types,
        // while technically isolated, are dependant on the existence of the Big Profile.
        
        if (this.bigProfile != null && this.isVita) {
            this.getMoonSlots();
            this.getDownloadSlots();   
        }
    }
    
    /**
     * Preloads the FAR4/5 archive and appends the loaded resources to the
     * current save manager's resources map
     * @param save The path of the FAR4/5 archive.
     * @return The fat data source of the FAR4/5 archive.
     */
    private Resource addProfileData(File save) {
        FileArchive archive = new FileArchive(save);
        if (archive.isParsed) {
            for (FileEntry entry : archive.entries)
                this.resources.put(Bytes.toHex(entry.SHA1), entry.data);
            String dataSourceHash = archive.getFatDataSource();
            if (this.resources.containsKey(dataSourceHash))
                return new Resource(this.resources.get(dataSourceHash));
        }
        return null;
    }
    
    /**
     * Searches the save directory for the primary local profile save.
     */
    private void getLocalSave() {
        Pattern regex = Pattern.compile("littlefart\\d+");
        File[] localSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        if (localSaves.length == 0) {
            System.out.println("Couldn't find any local profiles in this directory!");
            return;
        } else if (localSaves.length != 1) 
            System.out.println(String.format("Multiple local profiles found, defaulting to %s", localSaves[0].getName()));
        else System.out.println(String.format("Found local save file: %s", localSaves[0].getName())); 
        
        Resource localProfile = this.addProfileData(localSaves[0]);
        if (localProfile == null) {
            System.out.println("Local save did not contain an instance of RLocalProfile!");
            return;
        }
        
        // NOTE(Jun): I don't know if any LBP2 saves use revision 0x3e2, a better way
        // to determine if it's actually from Vita would be to check that the branch
        // descriptor is not equal to 0, but the current resource base does not support
        // that, I will have to remake that later.
        
        if (localProfile.revision == 0x3e2)
            this.isVita = true;
        
        
        // TODO(Jun): I don't have the entire structure for RLocalProfile right now,
        // it also changes a lot between games naturally, due to the nature of the type of
        // resource it is, for now we'll just ignore it, not write to it. I'll later come back to
        // it and create basic editor applications, for say, popit colors or disabling the 
        // copied from another user error.
        
    }
    
    /**
     * Searches the save directory for big profile saves. If the game isn't LBPV,
     * there should be only a single big profile, however Vita contains fragmented
     * saves, as well as backups.
     */
    private void getBigSaves() {
        Pattern regex = Pattern.compile("bigfart\\d+");
        File[] bigSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        
        if (bigSaves.length == 0) {
            System.out.println("Couldn't find any big profiles in this directory!");
            return;
        }
        
        Resource bigProfile = null; 
        
        // TODO(Jun): We're assuming isVita has been set from the local profile check,
        // but what if there were no local profiles? Will account for this later.
        
        if (this.isVita) {
            // TODO(Jun): Add support for Vita multi-profile saves.
        } else {
            
            // NOTE(Jun): The mainline LBP games should never have more than one big profile,
            // only LBPV uses multiple, not accounting for level backups however, but that isn't
            // covered by this manager.
            
            if (bigSaves.length != 1) 
                System.out.println(String.format("Multiple local profiles found, defaulting to %s", bigSaves[0].getName()));
            else System.out.println(String.format("Found local save file: %s", bigSaves[0].getName()));
            
            bigProfile = this.addProfileData(bigSaves[0]);
            if (bigProfile == null) {
                System.out.println("Big save did not contain an instance of RBigProfile!");
                return;
            }
        }
    }
    
    /**
     * Searches the save directory for saves containing data pertaining to levels
     * saved on the user's moon, this should only be used on LBPV saves.
     */
    private void getMoonSlots() {
        Pattern regex = Pattern.compile("moon\\d+_\\d+");
        File[] moonSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : moonSaves) {
            FileArchive archive = new FileArchive(file);
            System.out.println(String.format("Found moon save file: %s", file.getName()));   
        }
    }
    
    /**
     * Searches the save directory for levels downloaded from the community,
     * this should only be used on LBPV saves.
     */
    private void getDownloadSlots() {
        Pattern regex = Pattern.compile("slot\\d+_\\d+");
        File[] downloadSaves = this.directory.listFiles((dir, name) -> regex.matcher(name).matches());
        for (File file : downloadSaves) {
            FileArchive archive = new FileArchive(file);
            System.out.println(String.format("Found download save file: %s", file.getName()));   
        }
    }
}
