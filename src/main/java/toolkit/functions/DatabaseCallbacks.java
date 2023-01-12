package toolkit.functions;

import cwlib.singleton.ResourceSystem;
import cwlib.util.FileIO;
import cwlib.io.streams.MemoryOutputStream;
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
        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to zero this?", "Confirm erase", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION) return;
        
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
        String file = JOptionPane.showInputDialog(Toolkit.INSTANCE, "New Entry", "");
        if (file == null || file.isEmpty()) return null;
            
        FileData database = ResourceSystem.getSelectedDatabase();

        if (!database.getType().hasGUIDs()) return null;
        
        long nextGUID = database.getNextGUID().getValue();
        
        String input = JOptionPane.showInputDialog(Toolkit.INSTANCE, "File GUID", "g" + nextGUID);
        if (input == null) return null;
        input = input.replaceAll("\\s", "");
        
        
        GUID guid = Strings.getGUID(input);
        if (guid == null) {
            System.err.println("You inputted an invalid GUID!");
            return null;
        }

        if (database.get(guid) != null) {
            System.err.println("This GUID already exists!");
            return null;
        }

        FileNode node = ResourceSystem.getSelected();
        String path;
        if (node == null) path = file;
        else path = node.getFilePath() + node.getName() + "/" + file;
        
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

        boolean forceOverwrite = false;
        boolean forceSkip = false;

        String[] options = new String[] { "Overwrite", "Skip", "Overwrite All", "Skip All" };

        for (FileNode node : nodes) {
            FileDBRow entry = (FileDBRow) node.getEntry();
            if (entry == null) continue;

            FileDBRow copy = destination.get(entry.getGUID());
            if (copy != null) {

                if (forceSkip) continue;
                if (!forceOverwrite) {
                    int response = JOptionPane.showOptionDialog(
                        Toolkit.INSTANCE,
                        entry.getName() + " already exists, what do you want to do?",
                        "Conflict",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]
                    );

                    if (response == 1) continue;
                    else if (response == 2) forceOverwrite = true;
                    else if (response == 3) {
                        forceSkip = true;
                        continue;
                    }
                }
                
                copy.setDetails(entry);
            }
            else destination.newFileDBRow(entry);
        }

        destination.setHasChanges();
        ResourceSystem.reloadModel(destination);
        Toolkit.INSTANCE.updateWorkspace();
    }
    
    public static void duplicateItem() {                          
        FileData database = ResourceSystem.getSelectedDatabase();
        if (!database.getType().hasGUIDs()) return;
        FileEntry source = ResourceSystem.getSelected().getEntry();

        String path = JOptionPane.showInputDialog(Toolkit.INSTANCE, "Duplicate", source.getPath());
        if (path == null) return;

        long nextGUID = database.getNextGUID().getValue();
        String input = JOptionPane.showInputDialog(Toolkit.INSTANCE, "File GUID", "g" + nextGUID);
        if (input == null) return;
        input = input.replaceAll("\\s", "");
        
        GUID guid = Strings.getGUID(input);
        if (guid == null) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }

        if (database.get(guid) != null) {
            System.err.println("This GUID already exists!");
            return;
        }
        
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
        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this?", "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION) return;
        
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
