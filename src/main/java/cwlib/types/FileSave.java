package cwlib.types;

import cwlib.resources.RLocalProfile;
import cwlib.resources.RBigProfile;
import cwlib.ex.SerializationException;
import cwlib.util.Bytes;
import toolkit.utilities.Globals;
import cwlib.types.Resource;
import cwlib.enums.InventoryObjectType;
import cwlib.util.FileIO;
import cwlib.structs.profile.InventoryItem;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.structs.slot.Slot;
import cwlib.io.serializer.Serializer;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.util.Nodes;
import cwlib.types.FileArchive;
import cwlib.types.FileArchive.ArchiveType;
import cwlib.types.FileEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * A save manager for both Big and Local Profiles, as well as downloaded levels.
 */
public class FileSave extends FileData {
    public Revision revision;
    public File directory;
    
    public RBigProfile bigProfile;
    public RLocalProfile localProfile;
    
    public ArrayList<FileEntry> userCreatedEntries = new ArrayList<FileEntry>();
    public HashMap<SHA1, byte[]> resources = new HashMap<SHA1, byte[]>();
    
    public FileSave(File folder) {
        this.name = "Savedata";
        this.type = "File Save";
        this.model = new FileModel(new FileNode("SAVE", null, null));
        this.root = (FileNode) this.model.getRoot();
        this.directory = folder;
        if (!folder.exists()) return;
        
        this.getLocalSave();
        this.getBigSaves();
        
        // NOTE(Aidan): We check if RBigProfile exists because both of these save types,
        // while technically isolated, are dependant on the existence of the Big Profile.
        
        if (this.bigProfile != null && this.revision.isVita()) {
            this.getMoonSlots();
            this.getDownloadSlots();   
        }
        
        this.generateNodes();
        this.build();
    }
    
    private void generateNodes() {
        if (this.bigProfile != null) {
            for (Slot slot : this.bigProfile.myMoonSlots.values()) {
                String title = slot.title.isEmpty() ? "Unnamed Level" : slot.title;
                FileEntry entry = new FileEntry("slots/" + title);
                entry.timestamp = -1;
                if (slot.root != null) {
                    if (slot.root.GUID != -1) {
                        entry.GUID = slot.root.GUID;
                        entry.size = -1;
                    }
                    else if (slot.root.hash != null) {
                        entry.hash = slot.root.hash;
                        byte[] data = this.extract(entry.hash);
                        if (data != null)
                            entry.size = data.length;
                    }
                }
                entry.setResource("slot", slot);
                Nodes.addNode(this.root, entry);
            }
            for (InventoryItem item : this.bigProfile.inventory) {
                String title = "Some kind of object";
                if (item.details.userCreatedDetails != null && !item.details.userCreatedDetails.title.isEmpty())
                    title = item.details.userCreatedDetails.title;
                else if (item.details.titleKey != 0 && Globals.LAMS != null)
                    title = Globals.LAMS.translate(item.details.titleKey);
                if (title.isEmpty()) title = "Some kind of object";
                
                
                String type = InventoryObjectType.getPrimaryName(item.details.type);
                String folder = type.toLowerCase() + "/";
                /*
                if (item.details.type == ItemType.USER_COSTUMES || item.details.type == ItemType.COSTUMES)
                    folder += item.details.subType.name().toLowerCase() + "/";
                */
                
                FileEntry entry = new FileEntry("ugc/items/" + folder + title);
                
                entry.timestamp = -1;
                if (item.plan != null) {
                    if (item.plan.GUID != -1) {
                        entry.GUID = item.plan.GUID;
                        entry.size = -1;
                    }
                    else if (item.plan.hash != null) {
                        entry.hash = item.plan.hash;
                        byte[] data = this.extract(entry.hash);
                        if (data != null)
                            entry.size = data.length;
                    }
                }
                entry.setResource("cachedItemDetails", item);
                Nodes.addNode(this.root, entry);
            }
        }
        
        if (this.localProfile != null) {
            for (InventoryItem item : this.localProfile.inventory) {
                String title = "Some kind of object";
                if (item.details.userCreatedDetails != null && !item.details.userCreatedDetails.title.isEmpty())
                    title = item.details.userCreatedDetails.title;
                else if (item.details.titleKey != 0 && Globals.LAMS != null)
                    title = Globals.LAMS.translate(item.details.titleKey);
                if (title.isEmpty()) title = "Some kind of object";
                
                String type = InventoryObjectType.getPrimaryName(item.details.type);
                String folder = type.toLowerCase() + "/";
                if (item.details.categoryIndex != -1) {
                    int index = this.localProfile.stringTable.rawIndexToSortedIndex[item.details.categoryIndex];
                    String category = this.localProfile.stringTable.stringList.get(index).string;
                    if (!category.isEmpty())
                        folder += category + "/";
                }
                /*
                if (item.details.type == ItemType.USER_COSTUMES || item.details.type == ItemType.COSTUMES)
                    folder += item.details.subType.name().toLowerCase() + "/";
                */
                
                FileEntry entry = new FileEntry("items/" + folder + title);
                entry.timestamp = -1; entry.size = -1;
                if (item.plan != null) {
                    if (item.plan.GUID != -1)
                        entry.GUID = item.plan.GUID;
                    else if (item.plan.hash != null)
                        entry.hash = item.plan.hash;
                    byte[] data = Globals.extractFile(item.plan);
                    if (data != null) {
                        entry.hash = SHA1.fromBuffer(data);
                        entry.size = data.length;
                        entry.data = data;
                    }
                }
                entry.setResource("cachedItemDetails", item);
                Nodes.addNode(this.root, entry);
            }
        }
    }
    
    public void add(byte[] data) {
        if (data == null) return;
        this.resources.put(SHA1.fromBuffer(data), data);
    }
    
    public byte[] extract(SHA1 hash) {
        if (this.resources.containsKey(hash))
            return this.resources.get(hash);
        return null;
    }
    
    /**
     * Preloads the FAR4/5 archive and appends the loaded resources to the
     * current save manager's resources map
     * @param save The path of the FAR4/5 archive.
     * @return The fat data source of the FAR4/5 archive.
     */
    private Resource addProfileData(File save) {
        FileArchive archive = null;
        try { archive = new FileArchive(save); }
        catch (SerializationException ex) { return null; }
        for (FileEntry entry : archive.entries)
            this.resources.put(entry.hash, entry.data);
        SHA1 dataSourceHash = archive.getSaveKeyRoot();
        if (this.resources.containsKey(dataSourceHash)) {
            Resource source = new Resource(this.resources.get(dataSourceHash));
            this.resources.remove(dataSourceHash);
            return source;
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
        this.revision = localProfile.revision;
        if (localProfile == null) {
            System.out.println("Local save did not contain an instance of RLocalProfile!");
            return;
        }
        
        // TODO(Aidan): I don't have the entire structure for RLocalProfile right now,
        // it also changes a lot between games naturally, due to the nature of the type of
        // resource it is, for now we'll just ignore it, not write to it. I'll later come back to
        // it and create basic editor applications, for say, popit colors or disabling the 
        // copied from another user error.
        
        try { this.localProfile = new Serializer(localProfile.handle).struct(null, RLocalProfile.class); }
        catch (Exception e) { System.err.println("An error occurred while processing RLocalProfile"); }
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
        
        // TODO(Aidan): We're assuming isVita has been set from the local profile check,
        // but what if there were no local profiles? Will account for this later.
        
        if (false) {
            // TODO(Aidan): Add support for Vita multi-profile saves.
        } else {
            
            // NOTE(Aidan): The mainline LBP games should never have more than one big profile,
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
            
            try { this.bigProfile = new Serializer(bigProfile.handle).struct(null, RBigProfile.class); }
            catch (Exception e) { System.err.println("An error occurred while processing RBigProfile"); }
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
    
    public byte[] build() {
        FileArchive archive = new FileArchive();
        if (this.revision.isVita())
            archive.archiveType = ArchiveType.FAR5;
        for (SHA1 hash : this.resources.keySet()) {
            FileEntry entry = new FileEntry();
            byte[] data = this.resources.get(hash);
            entry.hash = hash;
            entry.size = data.length;
            entry.data = data;
            archive.add(entry);
        }
        byte[] profile = this.bigProfile.build(this.revision, (byte) 0);
        
        archive.add(profile);
        archive.setFatRevision(revision);
        archive.setFatDataSource(SHA1.fromBuffer(profile));
        
        byte[] output = archive.build();
        FileIO.write(output, "C:/Users/Aidan/Desktop/bigfart");
        
        return null;
    }
}
