package toolkit.functions;

import cwlib.enums.DatabaseType;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.io.streams.MemoryInputStream;
import cwlib.singleton.ResourceSystem;
import cwlib.util.Compressor;
import cwlib.util.FileIO;
import cwlib.types.Resource;
import cwlib.types.archives.Fart;
import cwlib.types.archives.FileArchive;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileNode;
import cwlib.types.databases.FileEntry;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

public class ArchiveCallbacks {
    public static void loadFileArchive(File file) {
        int index = Toolkit.INSTANCE.isArchiveLoaded(file);
        FileArchive archive = null;
        try { archive = new FileArchive(file); }
        catch (SerializationException ex) {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, ex.getMessage(), "An error occurred", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (index != -1) ResourceSystem.getArchives().set(index, archive);
        else ResourceSystem.getArchives().add(archive);
        
        Toolkit.INSTANCE.updateWorkspace();
    }
    
    public static void newFileArchive() {
        File file = FileChooser.openFile("data.farc", "farc", true);
        if (file == null) return;
        if (Toolkit.INSTANCE.confirmOverwrite(file)) {
            FileIO.write(new byte[] {
                0,
                0,
                0,
                0,
                0x46,
                0x41,
                0x52,
                0x43
            }, file.getAbsolutePath());
            ArchiveCallbacks.loadFileArchive(file);
        }
    }
    
    public static void integrityCheck() {
        File file = FileChooser.openFile("data.farc", "farc", false);
        if (file == null) return;
        FileArchive archive = new FileArchive(file);
        int missing = archive.validate();
        if (missing != 0)
            archive.save();
        System.out.println(String.format("%d files failed integrity check and were removed.", missing));
    }
    
    public static void addFile() {
        if (!ResourceSystem.canExtract()) return;                           

        File[] files = FileChooser.openFiles(null);
        if (files == null) return;

        Fart[] archives = null;

        FileData database = ResourceSystem.getSelectedDatabase();
        DatabaseType type = database.getType();

        if (!type.containsData()) {
            archives = Toolkit.INSTANCE.getSelectedArchives();
            if (archives == null) return;
        }
        
        for (File file: files) {
            byte[] data = FileIO.read(file.getAbsolutePath());
            if (data == null) return;

            if (type.containsData()) database.add(data);
            else ResourceSystem.add(data, archives);
        }

        Toolkit.INSTANCE.updateWorkspace();

        if (type.containsData()) 
            ResourceSystem.reloadModel(database);

        System.out.println("Added file(s) to queue, make sure to save your workspace!");
    }
    
    public static void addFolder() {                                        
        if (ResourceSystem.getArchives().size() == 0) return;

        String directory = FileChooser.openDirectory();
        if (directory == null || directory.isEmpty()) return;
        
        Fart[] archives = Toolkit.INSTANCE.getSelectedArchives();
        if (archives == null) return;
        
        try (Stream<Path> stream = Files.walk(Paths.get(directory))) {
           List<String> collect = stream
                   .map(String::valueOf)
                   .sorted()
                   .collect(Collectors.toList());
           collect.forEach(path -> {
               File file = new File(path);
               if (file.isFile()) {
                byte[] data = FileIO.read(file.getAbsolutePath());
                if (data != null)
                    ResourceSystem.add(data, archives);
               } 
           });
                   
        } catch (IOException ex) { Logger.getLogger(ArchiveCallbacks.class.getName()).log(Level.SEVERE, null, ex); }

        Toolkit.INSTANCE.updateWorkspace();

        if (ResourceSystem.getDatabaseType() == DatabaseType.BIGFART)
            ResourceSystem.reloadSelectedModel();

        System.out.println("Added files to queue, make sure to save your workspace!");
    }   

    private static byte[] tryDecompress(String name, byte[] data) {
        if (name.endsWith(".vpo") || name.endsWith(".gpo") || name.endsWith(".fpo") || name.endsWith(".sbu")) {
            try {
                return Compressor.decompressData(new MemoryInputStream(data), data.length);
            } catch (Exception ex) {
                // Resource essentially failed to extract if it
                // can't be decompressed.
                return null;
            }
        } else {
            ResourceType type = ResourceType.fromMagic(new String(new byte[] { data[0], data[1], data[2] }));
            SerializationType method = SerializationType.fromValue(Character.toString((char) data[3]));
            if (type == ResourceType.INVALID || type == ResourceType.FONTFACE || method == SerializationType.UNKNOWN) return null;
            try {
                return new Resource(data).getStream().getBuffer();
            } catch (Exception ex) {
                // Some error occurred while decompressing resource
                return null;
            }
        }
    }

    public static void extract(boolean decompress) {
        FileNode[] selected = ResourceSystem.getAllSelected();
        if (selected.length == 0) return;
        if (selected.length > 1) {
            int success = 0;
            int total = 0;
            String path = FileChooser.openDirectory();
            if (path == null) return;
            ResourceSystem.DISABLE_LOGS = true;
            for (int i = 0; i < selected.length; ++i) {
                FileNode node = selected[i];
                FileEntry entry = node.getEntry();
                if (entry != null) {
                    total++;
                    byte[] data = ResourceSystem.extract(node.getEntry());
                    if (data == null) continue;
                    if (decompress) {
                        data = tryDecompress(node.getName(), data);
                        if (data == null) continue;
                    }
                    String output = Paths.get(path, node.getFilePath(), node.getName()).toString();
                    File file = new File(output);
                    if (file.getParentFile() != null)
                        file.getParentFile().mkdirs();
                    if (FileIO.write(data, output))
                        success++;
                }
            }
            ResourceSystem.DISABLE_LOGS = false;
            System.out.println("Finished extracting " + success + "/" + total + " entries.");
            return;
        }

        FileNode node = selected[0];
        FileEntry entry = node.getEntry();
        if (entry == null) return;
        byte[] data = ResourceSystem.extract(entry);
        if (data == null) {
            ResourceSystem.println("Failed to extract entry");
            return;
        }
        if (decompress) {
            data = tryDecompress(node.getName(), data);
            if (data == null) {
                ResourceSystem.println("Data failed to decompress!");
                return;
            }
        }

        File file = FileChooser.openFile(node.getName(), null, true);
        if (file == null) return;
        if (FileIO.write(data, file.getAbsolutePath()))
            ResourceSystem.println("Successfully extracted entry!");

    }
}