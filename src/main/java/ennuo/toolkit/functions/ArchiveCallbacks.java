package ennuo.toolkit.functions;

import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.types.BigProfile;
import ennuo.craftworld.types.FileArchive;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
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
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class ArchiveCallbacks {
    public static void loadFileArchive(File file) {
        int index = Toolkit.instance.isArchiveLoaded(file);
        if (index == -1) {
            FileArchive archive = new FileArchive(file);
            if (archive.isParsed)
                Globals.archives.add(archive);
        } else Globals.archives.get(index).process();
        Toolkit.instance.updateWorkspace();
    }
    
    public static void newFileArchive() {
        File file = Toolkit.instance.fileChooser.openFile("data.farc", "farc", "File Archive", true);
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
    
    public static void addFile() {                                        
        if (Globals.archives.size() == 0 && Globals.currentWorkspace != Globals.WorkspaceType.PROFILE) return;

        File[] files = Toolkit.instance.fileChooser.openFiles("data.bin", "");
        if (files == null) return;

        for (File file: files) {
            byte[] data = FileIO.read(file.getAbsolutePath());
            if (data == null) return;

            if (Globals.currentWorkspace == Globals.WorkspaceType.PROFILE)
                ((BigProfile) Toolkit.instance.getCurrentDB()).add(data);
            else Globals.addFile(data);
        }

        Toolkit.instance.updateWorkspace();

        if (Globals.currentWorkspace == Globals.WorkspaceType.PROFILE) {
            JTree tree = Toolkit.instance.getCurrentTree();
            TreePath selectionPath = tree.getSelectionPath();
            ((FileModel) tree.getModel()).reload();
            tree.setSelectionPath(selectionPath);
        }

        System.out.println("Added file to queue, make sure to save your workspace!");
    }
    
    public static void addFolder() {                                        
        if (Globals.archives.size() == 0) return;

        String directory = Toolkit.instance.fileChooser.openDirectory();
        if (directory == null || directory.isEmpty()) return;
        directory = directory.substring(0, directory.length() - 1); // shit fix for fnf on linux
        
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
                    Globals.addFile(data, archives);
               } 
           });
                   
        } catch (IOException ex) { Logger.getLogger(ArchiveCallbacks.class.getName()).log(Level.SEVERE, null, ex); }

        Toolkit.instance.updateWorkspace();

        if (Globals.currentWorkspace == Globals.WorkspaceType.PROFILE) {
            JTree tree = Toolkit.instance.getCurrentTree();
            TreePath selectionPath = tree.getSelectionPath();
            ((FileModel) tree.getModel()).reload();
            tree.setSelectionPath(selectionPath);
        }

        System.out.println("Added files to queue, make sure to save your workspace!");
    }   

    public static void extract(boolean decompress) {
        if (Globals.entries.size() == 0) {
            System.out.println("You need to select files to extract.");
            return;
        }
        if (Globals.entries.size() != 1) {
            int success = 0;
            int total = 0;
            String path = Toolkit.instance.fileChooser.openDirectory();
            if (path == null) return;
            for (int i = 0; i < Globals.entries.size(); ++i) {
                FileNode node = Globals.entries.get(i);
                if (node.entry != null) {
                    total++;
                    byte[] data = Globals.extractFile(node.entry.SHA1);
                    if (data != null) {
                        Resource resource = new Resource(data);
                        if (decompress) resource.decompress(true);
                        String output = Paths.get(path, node.path, node.header).toString();
                        File file = new File(output);
                        if (file.getParentFile() != null)
                            file.getParentFile().mkdirs();
                        if (FileIO.write(resource.data, output))
                            success++;
                    }
                }
            }
            System.out.println("Finished extracting " + success + "/" + total + " entries.");
        } else {
            FileNode node = Globals.entries.get(0);
            if (node.entry != null) {
                byte[] data = Globals.extractFile(node.entry.SHA1);
                if (data != null) {
                    Resource resource = new Resource(data);
                    if (decompress) resource.decompress(true);
                    File file = Toolkit.instance.fileChooser.openFile(node.header, "", "", true);
                    if (file != null)
                        if (FileIO.write(resource.data, file.getAbsolutePath()))
                            System.out.println("Successfully extracted entry!");
                        else System.err.println("Failed to extract entry.");
                } else System.err.println("Could not extract! Entry is missing data!");
            } else System.err.println("Node is missing an entry! Can't extract!");
        }

    }
}