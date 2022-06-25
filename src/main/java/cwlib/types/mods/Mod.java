package cwlib.types.mods;

import cwlib.types.mods.patches.ModPatch;
import cwlib.ex.SerializationException;
import cwlib.util.Images;
import toolkit.utilities.ResourceSystem;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;
import cwlib.enums.DatabaseType;
import cwlib.types.swing.FileData;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.databases.FileEntry;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
    private FileDB database;
    private SaveArchive archive;
    
    private ModInfo config = new ModInfo();
    private ArrayList<ModPatch> patches = new ArrayList<>();
    
    private ImageIcon icon = null;
    
    public Mod() { super(null, DatabaseType.MOD); }
    public Mod(File file) {
        super(null, DatabaseType.MOD);
        this.process(file);
    }
    
    private void process(File file) {
        try (FileSystem fileSystem = FileSystems.newFileSystem(file.toPath(), (java.lang.ClassLoader) null)) {
            Path configPath = fileSystem.getPath("config.json");
            if (!Files.exists(configPath))
                throw new SerializationException("Mod is missing config file!");
            
            
            String config = FileIO.readString(configPath);

            this.config = GsonUtils.fromJSON(config, ModInfo.class);
            
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
            if (!Files.exists(databasePath))
                throw new SerializationException("Mod has no contents! (data.map is missing)");
            
            Path archivePath = fileSystem.getPath("data.farc");
            this.archive = null;
            if (Files.exists(archivePath))
                this.archive = new SaveArchive(Files.readAllBytes(archivePath));

            this.database = new FileDB(Files.readAllBytes(databasePath));

            // Prefer the model of the FileDB
            this.model = this.database.getModel();
            this.root = this.database.getRoot();

            for (FileDBRow entry : database) {
                if (this.archive != null && archive.exists(entry.getSHA1())) continue;
                Path filePath = fileSystem.getPath(entry.getPath());
                if (Files.exists(filePath)) {
                    byte[] fileData = Files.readAllBytes(filePath);
                    entry.setDetails(fileData);
                    this.archive.add(fileData);
                }
            }

            Path patchesPath = fileSystem.getPath("patches.json");
            if (Files.exists(patchesPath)) {
                String patchJSON = FileIO.readString(patchesPath);
                ModPatch[] patches = GsonUtils.fromJSON(patchJSON, ModPatch[].class);
                this.patches = new ArrayList<>(Arrays.asList(patches));
            }
        } catch (IOException ex) {
            throw new SerializationException(ex.getMessage());
        }
    }
    
    public FileDBRow add(String path, byte[] data) { return this.add(path, data, null); }
    public FileDBRow add(String path, byte[] data, GUID guid) {
        if (guid == null) guid = this.database.getNextGUID();
        FileDBRow row = this.database.newFileDBRow(path, guid);
        if (data != null) {
            row.setSHA1(this.archive.add(data));
            row.setSize(data.length);
        }
        return row;
    }

    @Override public void remove(FileEntry entry) { this.database.remove(entry); }
    @Override public FileDBRow get(GUID guid) { return this.database.get(guid); }
    @Override public byte[] extract(SHA1 sha1) { return this.archive.extract(sha1); }
    @Override public boolean save(File file) {
        if (file == null) return false;
        
        byte[] serializedDatabase = this.database.build();
        byte[] serializedArchive = this.archive.build();
        
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

        byte[] config = GsonUtils.toJSON(this.config).getBytes(StandardCharsets.UTF_8);
        
        byte[] patches = null;
        if (this.patches.size() != 0)
            patches = GsonUtils.toJSON(this.patches.toArray(ModPatch[]::new)).getBytes(StandardCharsets.UTF_8);
        
        File workingZip = new File(ResourceSystem.getWorkingDirectory(), "working.mod");
        
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
            Files.deleteIfExists(file.toPath());
            Files.copy(workingZip.toPath(), file.toPath());
        }
        catch (IOException ex) { System.err.println("There was an error copying mod file."); }
        
        try { Files.deleteIfExists(workingZip.toPath()); }
        catch (IOException ex) { System.err.println("There was an error deleting temp file."); }
        
        if (file.equals(this.getFile())) this.hasChanges = false;
        return true;
    }
}
