package toolkit.functions;

import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.util.FileIO;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;

import java.awt.Color;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class DatabaseCallbacks {
    public static void loadFileDB(File file) {
        Toolkit toolkit = Toolkit.instance;
        ResourceSystem.getDatabaseService().submit(() -> {
            FileDB database = null;
            try { database = new FileDB(file); }
            catch (Exception ex) { return; }

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
        Toolkit.instance.updateWorkspace();
        base.getModel().reload();
    }

    public static void dumpRLST() {
        FileDB database = ResourceSystem.getSelectedDatabase();
        String rlst = database.toRLST();

        File file = FileChooser.openFile("poppet_inventory_empty.rlst", "rlst", true);
        if (file == null) return;

        FileIO.write(rlst.getBytes(), file.getAbsolutePath());
    }

    public static void dumpHashes() {
        File file = FileChooser.openFile("hashes.txt", "txt", true);
        if (file == null) return;

        FileDB database = ResourceSystem.getSelectedDatabase();
        StringBuilder builder = new StringBuilder((40 + 1) * database.getEntryCount());
        for (FileDBRow row: database)
            builder.append(row.getSHA1().toString() + '\n');
        FileIO.write(builder.toString().getBytes(), file.getAbsolutePath());
    }
    
    public static void zero() {
        int zeroed = 0;
        for (FileNode node : ResourceSystem.getAllSelected()) {
            FileEntry entry = node.getEntry();
            if (entry != null) {
                entry.setDetails((byte[]) null);
                zeroed++;
            }
        }
        Toolkit.instance.updateWorkspace();
        System.out.println("Successfuly zeroed " + zeroed + " entries.");
    }
    
    public static void newItem() {                                               
        String file = JOptionPane.showInputDialog(Toolkit.instance, "New Item", "");
        if (file == null) return;
            
        FileData database = ResourceSystem.getSelectedDatabase();

        if (!database.getType().hasGUIDs()) return;
        
        long nextGUID = database.getNextGUID().getValue();
        
        String input = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", "g" + nextGUID);
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

        String path = ResourceSystem.getSelected().getFilePath() + ResourceSystem.getSelected().getName() + "/" + file;

        FileEntry entry = null;
        if (database instanceof FileDB)
            entry = ((FileDB)database).newFileDBRow(path, guid);
        else
            entry = ((Mod)database).add(path, null, guid);

        database.setHasChanges();
        ResourceSystem.reloadModel(database);
        Toolkit.instance.updateWorkspace();

        TreePath treePath = new TreePath(entry.getNode().getPath());
        JTree tree = database.getTree();
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);

        System.out.println("Added entry! -> " + entry.getPath());
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
        
        String hash = JOptionPane.showInputDialog(Toolkit.instance, "File Hash", "h" + entry.getSHA1().toString().toLowerCase());
        if (hash == null) return;
        hash = hash.replaceAll("\\s", "");
        if (hash.startsWith("h")) hash = hash.substring(1);
        entry.setSHA1(new SHA1(hash));
        
        Toolkit.instance.updateWorkspace();
        Toolkit.instance.setEditorPanel(node);
    }
    
    public static void changeGUID() {
        FileNode node = ResourceSystem.getSelected();
        FileEntry baseEntry = ResourceSystem.getSelected().getEntry();
        if (!baseEntry.getSource().getType().hasGUIDs()) return;
        FileDBRow entry = (FileDBRow) baseEntry;
        if (entry.getGUID() == null) return;

        String input = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", entry.getGUID().toString());
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

        Toolkit.instance.updateWorkspace();
        Toolkit.instance.setEditorPanel(node);
    }
    
    public static void renameItem() {        
        FileEntry entry = ResourceSystem.getSelected().getEntry();

        String path = (String) JOptionPane.showInputDialog(Toolkit.instance, "Rename", entry.getPath());
        if (path == null) return;
        
        entry.setPath(Strings.cleanupPath(path));

        ResourceSystem.reloadModel(entry.getSource());

        TreePath treePath = new TreePath(entry.getNode().getPath());
        JTree tree = entry.getSource().getTree();
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);

        Toolkit.instance.updateWorkspace();
    }
    
    public static void duplicateItem() {                          
        FileData database = ResourceSystem.getSelectedDatabase();
        if (!database.getType().hasGUIDs()) return;
        FileEntry source = ResourceSystem.getSelected().getEntry();

        String path = JOptionPane.showInputDialog(Toolkit.instance, "Duplicate", source.getPath());
        if (path == null) return;

        long nextGUID = database.getNextGUID().getValue();
        String input = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", "g" + nextGUID);
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
        
        FileEntry entry = null;
        if (database instanceof FileDB)
            entry = ((FileDB)database).newFileDBRow(path, guid);
        else
            entry = ((Mod)database).add(path, null, guid);
        entry.setDetails(source);
        
        database.setHasChanges();
        ResourceSystem.reloadModel(database);
        Toolkit.instance.updateWorkspace();
        
        TreePath treePath = new TreePath(entry.getNode().getPath());
        JTree tree = database.getTree();
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
    }
    
    public static void delete() {      
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
        Toolkit.instance.updateWorkspace();
        ResourceSystem.reloadSelectedModel();
    }
    
    public static void newFileDB(int header) {
        MemoryOutputStream output = new MemoryOutputStream(0x8);
        output.i32(header);
        output.i32(0);
        File file = FileChooser.openFile("blurayguids.map", "map", true);
        if (file == null) return;
        if (Toolkit.instance.confirmOverwrite(file)) {
            FileIO.write(output.getBuffer(), file.getAbsolutePath());
            DatabaseCallbacks.loadFileDB(file);
        }
    }
}
