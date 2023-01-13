package toolkit.functions;

import configurations.Config;
import cwlib.singleton.ResourceSystem;
import cwlib.util.FileIO;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.archives.Fart;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileNode;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import toolkit.dialogues.EntryDialogue;

public class DatabaseCallbacks {
    public static void loadFileDB(File file) {
        Toolkit toolkit = Toolkit.INSTANCE;
        ResourceSystem.getDatabaseService().submit(() -> {
            
            JProgressBar bar = Toolkit.INSTANCE.progressBar;
            bar.setVisible(true);
            bar.setIndeterminate(true);
            
            FileDB database = null;
            try { database = new FileDB(file); }
            catch (Exception ex) { 
                bar.setVisible(false);
                return; 
            }
            
            bar.setVisible(false);
            

            int loadedIndex = ResourceSystem.getLoadedDatabase(file);
            if (loadedIndex != -1) {
                ResourceSystem.getDatabases().set(loadedIndex, database);

                toolkit.fileDataTabs.setSelectedIndex(loadedIndex);

                toolkit.search.setEditable(true);
                toolkit.search.setFocusable(true);
                toolkit.search.setText("Search...");
                toolkit.search.setForeground(Color.GRAY);
            } else toolkit.addTab(database);
            
            toolkit.updateWorkspace();
        });
    }

    public static void patchDatabase() {
        File file = FileChooser.openFile("brg_patch.map", "map", false);
        if (file == null) return;
        FileDB base = ResourceSystem.getSelectedDatabase();
        base.patch(new FileDB(file));
        Toolkit.INSTANCE.updateWorkspace();
        base.getModel().reload();
    }

    public static void dumpRLST() {
        FileDB database = ResourceSystem.getSelectedDatabase();
        String rlst = database.toRLST();

        File file = FileChooser.openFile("poppet_inventory_empty.rlst", "rlst", true);
        if (file == null) return;

        FileIO.write(rlst.getBytes(), file.getAbsolutePath());
    }
    
    public static void zero() {
        if (Config.instance.displayWarningOnZeroEntry) {
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to zero this?", "Confirm erase", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) return;
        }
        
        int zeroed = 0;
        for (FileNode node : ResourceSystem.getAllSelected()) {
            FileEntry entry = node.getEntry();
            if (entry != null) {
                entry.setDetails((byte[]) null);
                zeroed++;
            }
        }
        
        ResourceSystem.getSelectedDatabase().setHasChanges();
        Toolkit.INSTANCE.updateWorkspace();
        
        System.out.println("Successfuly zeroed " + zeroed + " entries.");
    }
    
    public static FileEntry newEntry(byte[] data) {  
        FileData database = ResourceSystem.getSelectedDatabase();
        if (!database.getType().hasGUIDs()) {
            if (data != null && database.getType().containsData()) {
                database.add(data);
                database.setHasChanges();
                ResourceSystem.reloadModel(database);
                Toolkit.INSTANCE.updateWorkspace();
            }
            return null;
        }
        
        FileNode node = ResourceSystem.getSelected();
        String path = node == null ? "" : node.getFilePath() + node.getName() + "/";
        
        EntryDialogue dialogue = new EntryDialogue(Toolkit.INSTANCE, (FileDB) database, path, null);
        if (!dialogue.wasSubmitted()) return null;
        path = dialogue.getPath();
        GUID guid = dialogue.getGUID();
        
        FileEntry entry = null;
        if (database instanceof Mod) entry = ((Mod)database).add(path, data, guid);
        else {
            if (data != null)
                ResourceSystem.add(data);
            entry = ((FileDB)database).newFileDBRow(path, guid);
            entry.setDetails(data);
        }

        database.setHasChanges();
        ResourceSystem.reloadModel(database);
        Toolkit.INSTANCE.updateWorkspace();

        TreePath treePath = new TreePath(entry.getNode().getPath());
        JTree tree = database.getTree();
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);

        System.out.println("Added entry! -> " + entry.getPath());
        
        return entry;
    } 
    
    public static void newFolder() {                                                 
        String folder = (String) JOptionPane.showInputDialog("Please input a name for the folder.");
        if (folder == null || folder.equals("")) return;

        FileData database = ResourceSystem.getSelectedDatabase();
        FileNode selected = ResourceSystem.getSelected();

        TreePath path = null;
        if (selected == null)
            path = new TreePath(database.addNode(folder).getPath());
        else if (selected.getEntry() == null)
            path = new TreePath(database.addNode(selected.getFilePath() + selected.getName() + "/" + folder).getPath());
            
        ResourceSystem.reloadSelectedModel();
        JTree tree = database.getTree();
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }
    
    public static void changeHash() {
        FileNode node = ResourceSystem.getSelected();
        FileEntry entry = node.getEntry();
        
        String hash = JOptionPane.showInputDialog(Toolkit.INSTANCE, "File Hash", "h" + entry.getSHA1().toString().toLowerCase());
        if (hash == null) return;
        hash = hash.replaceAll("\\s", "");
        if (hash.startsWith("h")) hash = hash.substring(1);
        entry.setSHA1(new SHA1(hash));
        
        entry.getSource().setHasChanges();
        Toolkit.INSTANCE.updateWorkspace();
        
        Toolkit.INSTANCE.setEditorPanel(node);
    }
    
    public static void changeGUID() {
        FileNode node = ResourceSystem.getSelected();
        FileEntry baseEntry = ResourceSystem.getSelected().getEntry();
        if (!baseEntry.getSource().getType().hasGUIDs()) return;
        FileDBRow entry = (FileDBRow) baseEntry;
        if (entry.getGUID() == null) return;

        String input = JOptionPane.showInputDialog(Toolkit.INSTANCE, "File GUID", entry.getGUID().toString());
        if (input == null) return;

        GUID guid = Strings.getGUID(input);
        if (guid == null) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }

        if (guid.equals(entry.getGUID())) {
            System.err.println("The GUID is unchanged!");
            return;
        }

        if (entry.getFileDB().exists(guid)) {
            System.err.println("This GUID already exists!");
            return;
        }

        entry.setGUID(guid);
        
        entry.getSource().setHasChanges();
        Toolkit.INSTANCE.updateWorkspace();
        
        Toolkit.INSTANCE.setEditorPanel(node);
    }
    
    public static void renameItem() {        
        FileEntry entry = ResourceSystem.getSelected().getEntry();

        String path = (String) JOptionPane.showInputDialog(Toolkit.INSTANCE, "Rename", entry.getPath());
        if (path == null) return;
        
        entry.setPath(Strings.cleanupPath(path));

        ResourceSystem.reloadModel(entry.getSource());

        TreePath treePath = new TreePath(entry.getNode().getPath());
        JTree tree = entry.getSource().getTree();
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);

        entry.getSource().setHasChanges();
        Toolkit.INSTANCE.updateWorkspace();
    }

    public static void copyItems(FileDB destination) {
        FileNode[] nodes = ResourceSystem.getAllSelected();
        SHA1[] hashes = new SHA1[nodes.length];

        boolean forceOverwrite = false;
        boolean forceSkip = false;

        String[] options = new String[] { "Overwrite", "Skip", "Duplicate", "Overwrite All", "Skip All" };
        
        int OPTION_OVERWRITE = 0;
        int OPTION_SKIP = 1;
        int OPTION_DUPLICATE = 2;
        int OPTION_OVERWRITE_ALL = 3;
        int OPTION_SKIP_ALL = 4;
        
        for (int i = 0; i < nodes.length; ++i) {
            FileNode node = nodes[i];
            FileDBRow entry = (FileDBRow) node.getEntry();
            if (entry == null) continue;

            FileDBRow copy = destination.get(entry.getGUID());
            if (copy != null) {

                if (forceSkip) continue;
                if (!forceOverwrite) {
                    String message;
                    if (copy.getPath().equals(entry.getPath()))
                        message = String.format("Path: %s\nGUID: %s\nThis entry already exists in the database, what do you want to do?", entry.getPath(), entry.getGUID());
                    else
                        message = String.format("Source Path: %s\nDestination Path: %s\nGUID: %s\nThis entry already exists in the database, what do you want to do?", entry.getPath(), copy.getPath(), entry.getGUID());
                    
                    int response = JOptionPane.showOptionDialog(
                        Toolkit.INSTANCE,
                        message,
                        "Conflict",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]
                    );
                    
                    if (response == -1 || response == OPTION_SKIP) continue; // Treat closing dialog as skip
                    else if (response == OPTION_DUPLICATE) {
                        EntryDialogue dialogue = new EntryDialogue(Toolkit.INSTANCE, (FileDB) destination, entry.getPath(), null);
                        if (!dialogue.wasSubmitted()) {
                            i--;
                            continue;
                        }
                        
                        String path = dialogue.getPath();
                        GUID guid = dialogue.getGUID();
                        
                        FileDBRow row = destination.newFileDBRow(path, guid);
                        row.setDate(entry.getDate());
                        row.setSize(entry.getSize());
                        row.setSHA1(entry.getSHA1());
                        
                        hashes[i] = entry.getSHA1();
                        
                        continue;
                    }
                    else if (response == OPTION_OVERWRITE_ALL) forceOverwrite = true;
                    else if (response == OPTION_SKIP_ALL) {
                        forceSkip = true;
                        continue;
                    } else if (response != OPTION_OVERWRITE) continue;
                }
                
                copy.setDetails(entry);
            }
            else destination.newFileDBRow(entry);
            
            hashes[i] = entry.getSHA1();
            
        }
        
        if (Config.instance.addToArchiveOnCopy && ResourceSystem.getArchives().size() > 1) {
            boolean canCopy = false;
            boolean existsInAllArchives = true;
            for (SHA1 sha1 : hashes) {
                if (sha1 == null) continue;
                for (Fart fart : ResourceSystem.getArchives()) {
                    if (fart.exists(sha1)) canCopy = true;
                    else existsInAllArchives = false;
                }
            }
            
            if (canCopy && !existsInAllArchives) {
                int result = JOptionPane.showConfirmDialog(null, "Do you also want to copy file data to another archive?", "Copy", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    Fart[] archives = Toolkit.INSTANCE.getSelectedArchives();
                    if (archives != null) {
                        for (SHA1 sha1 : hashes) {
                            if (sha1 == null) continue;
                            byte[] data = ResourceSystem.extract(sha1);
                            if (data == null) continue;
                            ResourceSystem.add(data, archives);
                        }
                    }
                }
            }
        }

        destination.setHasChanges();
        ResourceSystem.reloadModel(destination);
        Toolkit.INSTANCE.updateWorkspace();
    }
    
    public static void duplicateItem() {                          
        FileData database = ResourceSystem.getSelectedDatabase();
        if (!database.getType().hasGUIDs()) return;
        FileEntry source = ResourceSystem.getSelected().getEntry();
        
        EntryDialogue dialogue = new EntryDialogue(Toolkit.INSTANCE, (FileDB) database, source.getPath(), null);
        if (!dialogue.wasSubmitted()) return;
        String path = dialogue.getPath();
        GUID guid = dialogue.getGUID();

        FileDBRow entry = ((FileDB)database).newFileDBRow(path, guid);
        entry.setSHA1(source.getSHA1());
        entry.setSize(source.getSize());
        entry.updateDate();
        
        database.setHasChanges();
        ResourceSystem.reloadModel(database);
        Toolkit.INSTANCE.updateWorkspace();
        
        TreePath treePath = new TreePath(entry.getNode().getPath());
        JTree tree = database.getTree();
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
    }
    
    public static void delete() {
        if (Config.instance.displayWarningOnDeletingEntry) {
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this?", "Confirm deletion", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) return;
        }
        
        FileData database = ResourceSystem.getSelectedDatabase();
        FileNode[] selections = ResourceSystem.getAllSelected();
        
        for (FileNode node : selections) {
            FileEntry entry = node.getEntry();
            if (entry == null) {
                node.delete();
                continue;
            }
            database.remove(entry);
        }
        database.setHasChanges();
        Toolkit.INSTANCE.updateWorkspace();
        ResourceSystem.reloadSelectedModel();
    }
    
    public static void newFileDB(int header) {
        MemoryOutputStream output = new MemoryOutputStream(0x8);
        output.i32(header);
        output.i32(0);
        File file = FileChooser.openFile("blurayguids.map", "map", true);
        if (file == null) return;
        if (Toolkit.INSTANCE.confirmOverwrite(file)) {
            FileIO.write(output.getBuffer(), file.getAbsolutePath());
            DatabaseCallbacks.loadFileDB(file);
        }
    }
}
