package ennuo.craftworld.types.mods;

import ennuo.craftworld.types.mods.patches.PatchDeserializer;
import ennuo.craftworld.types.mods.patches.ModPatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ennuo.craftworld.utilities.Bytes;
import ennuo.craftworld.utilities.Images;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileArchive.ArchiveType;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.utilities.Globals;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Mod extends FileData {
    public boolean isParsed = false;
    private HashMap<Long, FileEntry> GUIDLookup = new HashMap<>();
    private HashMap<SHA1, byte[]> SHA1Lookup = new HashMap<>();
    public ArrayList<FileEntry> entries = new ArrayList<>();
    public ModInfo config = new ModInfo();
    public ArrayList<ModPatch> patches = new ArrayList<>();
    public ImageIcon icon = null;
    
    public Mod() {
        this.isParsed = true;
        this.type = "Mod";
        this.model = new FileModel(new FileNode("MOD", null, null)); 
        this.root = (FileNode) this.model.getRoot();
    }
    
    public Mod(File file) {
        this();
        this.path = file.getAbsolutePath();
        this.name = file.getName();
        this.process(file);
    }
    
    private void process(File file) {
        this.isParsed = false;
        try (FileSystem fileSystem = FileSystems.newFileSystem(file.toPath(), null)) {
            Path configPath = fileSystem.getPath("config.json");
            if (!Files.exists(configPath)) {
                System.err.println("Mod is missing config.json!");
                return;
            }
            
            
            String config = FileIO.readString(configPath);
            
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(new TypeToken<ModPatch>(){}.getType(), new PatchDeserializer(ModPatch.class))
                    .create();
            this.config = gson.fromJson(config, ModInfo.class);
            
            Path iconPath = fileSystem.getPath("icon.png");
            if (Files.exists(iconPath)) {
                try {
                    byte[] image = Files.readAllBytes(iconPath);
                    InputStream input = new ByteArrayInputStream(image);
                    BufferedImage bufferedImage = ImageIO.read(input);
                    if (bufferedImage != null)
                        this.icon = Images.getImageIcon(bufferedImage);
                } catch (IOException ex) {
                    Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else System.out.println("Mod has no icon image.");
            
            Path databasePath = fileSystem.getPath("data.map");
            if (!Files.exists(databasePath)) {
                System.out.println("Mod has no contents! (data.map is missing)");
                this.isParsed = true;
                return;
            }
            
            Path archivePath = fileSystem.getPath("data.farc");
            FileArchive archive = null;
            if (Files.exists(archivePath)) {
                File tempArchive = new File(Globals.workingDirectory, this.config.ID + ".farc");
                Files.copy(archivePath, tempArchive.toPath());
                archive = new FileArchive(tempArchive);
                if (archive.archiveType == ArchiveType.FARC)
                    // NOTE(Abz): FAR4 is the default, but if for some reason there's a FARC, 
                    // we need to manually preload it since this isn't the default.
                    archive.preload();
                Files.delete(tempArchive.toPath());
            }

            FileDB database = new FileDB(Files.readAllBytes(databasePath));
            for (FileEntry entry : database.entries) {
                if (entry.GUID > this.lastGUID)
                    this.lastGUID = entry.GUID + 1;
                if (archive != null) entry.data = archive.extract(entry.hash);
                if (entry.data == null) {
                    Path filePath = fileSystem.getPath(entry.path);
                    if (Files.exists(filePath))
                        entry.setData(Files.readAllBytes(filePath));
                }
                if (this.add(entry) && this.model != null)
                    this.addNode(entry);
            }
            this.shouldSave = false; // NOTE(Abz): Calling Mod.add triggers shouldSave, but we haven't actually made any changes, so...
            
            Path patchesPath = fileSystem.getPath("patches.json");
            if (Files.exists(patchesPath)) {
                String patchJSON = FileIO.readString(patchesPath);
                ModPatch[] patches = gson.fromJson(patchJSON, ModPatch[].class);
                this.patches = new ArrayList(Arrays.asList(patches));
            }
            
            this.isParsed = true;
        } catch (IOException ex) {
            Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean add(FileEntry entry) {
        if (entry == null) return false;
        FileEntry existing = this.find(entry.GUID);
        if (existing == null) {
            entry.updateTimestamp();
            this.entries.add(entry);
            this.SHA1Lookup.put(entry.hash, entry.data);
            this.GUIDLookup.put(entry.GUID, entry);
            this.shouldSave = true;
            return true;
        }
        existing.setData(entry);
        if (existing.data == null) {
            existing.data = entry.data;
            this.SHA1Lookup.put(entry.hash, entry.data);
        }
        this.shouldSave = true;
        return false;
    }
    
    public void add(String path, byte[] data) {
        this.add(path, data, this.getNextGUID());
    }
    
    public void add(String path, byte[] data, long GUID) {
        if (GUID == -1) GUID = this.getNextGUID();
        if (data == null) {
            this.add(new FileEntry(path, GUID));
            return;
        }
        FileEntry entry = new FileEntry(data, SHA1.fromBuffer(data));
        entry.path = path; entry.GUID = GUID;
        if (this.add(entry) && this.model != null)
            this.addNode(entry);
    }
    
    public FileEntry find(long GUID) {
        if (this.GUIDLookup.containsKey(GUID))
            return this.GUIDLookup.get(GUID);
        return null;
    }
    
    public boolean remove(FileEntry entry) { 
        if (entry == null) return false;
        this.entries.remove(entry);
        this.GUIDLookup.remove(entry.GUID);
        this.shouldSave = true;
        return true;
    }
    
    public boolean edit(FileEntry entry, byte[] buffer) {
        entry = this.find(entry.GUID);
        if (entry == null) return false;
        entry.setData(buffer);
        this.SHA1Lookup.put(SHA1.fromBuffer(buffer), buffer);
        this.shouldSave = true;
        return true;
    }
    
    public byte[] extract(SHA1 hash) { 
        if (this.SHA1Lookup.containsKey(hash))
            return this.SHA1Lookup.get(hash);
        return null;
    }
    
    public boolean save(String path) {
        if (path == null) return false;
        
        FileDB database = new FileDB();
        database.entries = this.entries;
        FileArchive archive = new FileArchive();
        archive.entries = new ArrayList<FileEntry>(this.entries.size());
        for (FileEntry entry : this.entries)
            if (entry.data != null && archive.find(entry.hash) == null)
                archive.entries.add(entry);
        
        byte[] serializedDatabase = database.build();
        byte[] serializedArchive = archive.build();
        
        byte[] image = null;
        if (this.icon != null) {
            BufferedImage output = new BufferedImage(
                    this.icon.getIconWidth(), 
                    this.icon.getIconHeight(), 
                    BufferedImage.TYPE_4BYTE_ABGR
            );
            
            Graphics g = output.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(output, "png", baos);
                baos.flush();
                image = baos.toByteArray();
            } catch (IOException ex) {
                System.err.println("Failed to write icon.");
            }
        }
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        byte[] config = gson.toJson(this.config, ModInfo.class).getBytes();
        
        byte[] patches = null;
        if (this.patches.size() != 0)
            patches = gson.toJson(this.patches.toArray(), ModInfo[].class).getBytes();
        
        File workingZip = new File(Globals.workingDirectory, "working.mod");
        
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        
        URI uri = URI.create("jar:file:" + Paths.get(workingZip.getAbsolutePath()).toUri().getPath());
        try (FileSystem filesystem = FileSystems.newFileSystem(uri, env, null)) {
            Files.write(filesystem.getPath("config.json"), config);
            if (image != null)
                Files.write(filesystem.getPath("icon.png"), image);
            if (patches != null)
                Files.write(filesystem.getPath("patches.json"), patches);
            Files.write(filesystem.getPath("data.map"), serializedDatabase);
            Files.write(filesystem.getPath("data.farc"), serializedArchive);
        } catch (IOException ex) {
            Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Files.deleteIfExists(new File(path).toPath());
            Files.copy(workingZip.toPath(), new File(path).toPath());
        }
        catch (IOException ex) { System.err.println("There was an error copying mod file."); }
        
        try { Files.deleteIfExists(workingZip.toPath()); }
        catch (IOException ex) { System.err.println("There was an error deleting temp file."); }
        
        if (path.equals(this.path)) this.shouldSave = false;
        return true;
    }
}
