package toolkit.functions;

import cwlib.ex.SerializationException;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.util.FileIO;
import cwlib.types.Resource;
import cwlib.types.archives.Fart;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.SHA1;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.types.databases.FileEntry;
import cwlib.types.save.BigSave;
import toolkit.utilities.FileChooser;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class ArchiveCallbacks {
    public static void loadFileArchive(File file) {
        int index = Toolkit.instance.isArchiveLoaded(file);
        FileArchive archive = null;
        try { archive = new FileArchive(file); }
        catch (SerializationException ex) {
            System.err.println(ex.getMessage());
            return;
        }
        if (index != -1) ResourceSystem.getArchives().set(index, archive);
        else ResourceSystem.getArchives().add(archive);
        
        Toolkit.instance.updateWorkspace();
    }
    
    public static void newFileArchive() {
        File file = FileChooser.openFile("data.farc", "farc", true);
        if (file == null) return;
        if (Toolkit.instance.confirmOverwrite(file)) {
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
        int count = 0;
        MemoryOutputStream table = new MemoryOutputStream(archive.entries.size() * 0x1c);
        ArrayList<FileEntry> toRemove = new ArrayList<FileEntry>(archive.entries.size());
        for (int i = 0; i < archive.entries.size(); ++i) {
            FileEntry entry = archive.entries.get(i);
            byte[] data = archive.extract(entry);
            if (data == null) continue;
            String realSHA1 = SHA1.fromBuffer(data).toString();
            String storedSHA1 = entry.hash.toString();
            if (realSHA1.equals(storedSHA1)) {
                table.sha1(entry.hash);
                table.i32f((int) entry.offset);
                table.i32f(entry.size);
            } else {
                toRemove.add(entry);
                archive.lookup.remove(storedSHA1);
                count++;
            }
        }
        for (FileEntry entry : toRemove)
            archive.entries.remove(entry);
        table.shrink();
        archive.hashTable = table.buffer;
        archive.save(null, true);
        System.out.println(String.format("%d files failed integrity check and were removed.", count));
    }
    
    public static void addFile() {
        if (!ResourceSystem.canExtract()) return;                           

        File[] files = FileChooser.openFiles(null);
        if (files == null) return;

        FileData database = ResourceSystem.getSelectedDatabase();
        
        Fart[] archives = null;
        if (database.getType().containsData()) {
            
        }

        if (ResourceSystem.getDatabaseType() != Globals.ResourceSystem.PROFILE) {
            archives = Toolkit.instance.getSelectedArchives();
            if (archives == null) return;
        }
        
        for (File file: files) {
            byte[] data = FileIO.read(file.getAbsolutePath());
            if (data == null) return;

            if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.PROFILE)
                ((BigSave) Toolkit.instance.getCurrentDB()).add(data);
            else ResourceSystem.add(data, archives);
        }

        Toolkit.instance.updateWorkspace();

        if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.PROFILE) {
            JTree tree = Toolkit.instance.getCurrentTree();
            TreePath selectionPath = tree.getSelectionPath();
            ((FileModel) tree.getModel()).reload();
            tree.setSelectionPath(selectionPath);
        }

        System.out.println("Added file to queue, make sure to save your workspace!");
    }
    
    public static void addFolder() {                                        
        if (ResourceSystem.getArchives().size() == 0) return;

        String directory = FileChooser.openDirectory();
        if (directory == null || directory.isEmpty()) return;
        
        FileArchive[] archives = Toolkit.instance.getSelectedArchives();
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

        Toolkit.instance.updateWorkspace();

        if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.PROFILE) {
            JTree tree = Toolkit.instance.getCurrentTree();
            TreePath selectionPath = tree.getSelectionPath();
            ((FileModel) tree.getModel()).reload();
            tree.setSelectionPath(selectionPath);
        }

        System.out.println("Added files to queue, make sure to save your workspace!");
    }   

    public static void extract(boolean decompress) {
        if (ResourceSystem.selected.size() == 0) {
            System.out.println("You need to select files to extract.");
            return;
        }
        if (ResourceSystem.selected.size() != 1) {
            int success = 0;
            int total = 0;
            String path = FileChooser.openDirectory();
            if (path == null) return;
            for (int i = 0; i < ResourceSystem.selected.size(); ++i) {
                FileNode node = ResourceSystem.selected.get(i);
                if (node.entry != null) {
                    total++;
                    byte[] data;
                    if (node.entry.data == null)
                        data = ResourceSystem.extract(node.entry.hash);
                    else
                        data = node.entry.data;
                    if (data != null) {
                        data = (decompress) ? new Resource(data).handle.data : data;
                        String output = Paths.get(path, node.path, node.header).toString();
                        File file = new File(output);
                        if (file.getParentFile() != null)
                            file.getParentFile().mkdirs();
                        if (FileIO.write(data, output))
                            success++;
                    }
                }
            }
            System.out.println("Finished extracting " + success + "/" + total + " entries.");
        } else {
            FileNode node = ResourceSystem.selected.get(0);
            if (node.entry != null) {
                byte[] data = node.entry.data;
                if (data == null)
                    data = ResourceSystem.extract(node.entry.hash);
                if (data != null) {
                    data = (decompress) ? new Resource(data).handle.data : data;
                    File file = FileChooser.openFile(node.header, null, true);
                    if (file != null)
                        if (FileIO.write(data, file.getAbsolutePath()))
                            System.out.println("Successfully extracted entry!");
                        else System.err.println("Failed to extract entry.");
                } else System.err.println("Could not extract! Entry is missing data!");
            } else System.err.println("Node is missing an entry! Can't extract!");
        }

    }
}