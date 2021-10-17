package ennuo.toolkit.functions;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.memory.StringUtils;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.types.BigProfile;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Mod;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
import java.awt.Color;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class DatabaseCallbacks {
    public static void loadFileDB(File file) {
        Toolkit toolkit = Toolkit.instance;
        toolkit.databaseService.submit(() -> {

            toolkit.savedataMenu.setEnabled(false);
            toolkit.newFileDBGroup.setEnabled(false);
            toolkit.loadDB.setEnabled(false);

            FileDB db = new FileDB(file, toolkit.progressBar);

            if (db.isParsed) {

                int dbIndex = toolkit.isDatabaseLoaded(file);
                if (toolkit.isDatabaseLoaded(file) != -1) {

                    Globals.databases.set(dbIndex, db);

                    JTree tree = Toolkit.instance.trees.get(dbIndex);
                    tree.setModel(Globals.databases.get(dbIndex).model);
                    ((FileModel) tree.getModel()).reload();

                    toolkit.fileDataTabs.setSelectedIndex(dbIndex);

                    toolkit.search.setEditable(true);
                    toolkit.search.setFocusable(true);
                    toolkit.search.setText("Search...");
                    toolkit.search.setForeground(Color.GRAY);
                } else toolkit.addTab(db);

                toolkit.updateWorkspace();
            }

            toolkit.savedataMenu.setEnabled(true);
            toolkit.newFileDBGroup.setEnabled(true);
            toolkit.loadDB.setEnabled(true);
        });
    }

    public static void patchDatabase() {
        File file = Toolkit.instance.fileChooser.openFile("brg_patch.map", "map", "FileDB", false);
        if (file == null) return;
        FileDB newDB = new FileDB(file);
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        int added = 0, patched = 0;
        for (FileEntry entry: newDB.entries) {
            int old = db.entries.size();
            db.add(entry);
            if (old == db.entries.size()) patched++;
            else added++;
        }
        Toolkit.instance.updateWorkspace();
        System.out.println(String.format("Succesfully updated FileDB (added = %d, patched = %d)", added, patched));
        ((FileModel) Toolkit.instance.getCurrentTree().getModel()).reload();
    }

    public static void dumpRLST() {
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        String str = db.toRLST();

        File file = Toolkit.instance.fileChooser.openFile("poppet_inventory_empty.rlst", "rlst", "RLST", true);
        if (file == null) return;

        FileIO.write(str.getBytes(), file.getAbsolutePath());
    }

    public static void dumpHashes() {
        File file = Toolkit.instance.fileChooser.openFile("hashes.txt", "txt", "Text File", true);
        if (file == null) return;
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        StringBuilder builder = new StringBuilder(0x100 * db.entries.size());
        for (FileEntry entry: db.entries)
            builder.append(Bytes.toHex(entry.SHA1) + '\n');
        FileIO.write(builder.toString().getBytes(), file.getAbsolutePath());
    }
    
    public static void zero() {                                            
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        int zero = 0;
        for (FileNode node: Globals.entries) {
            if (node.entry != null) {
                db.zero(node.entry);
                zero++;
            }
        }
        Toolkit.instance.updateWorkspace();
        System.out.println("Successfuly zeroed " + zero + " entries.");
    }
    
    public static void newItem() {                                               
        String file = JOptionPane.showInputDialog(Toolkit.instance, "New Item", "");
        if (file == null) return;
        
        JTree tree = Toolkit.instance.getCurrentTree();

        FileData db = Toolkit.instance.getCurrentDB();
        
        long nextGUID = db.lastGUID + 1;
        
        String GUID = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", "g" + nextGUID);
        if (GUID == null) return;
        GUID = GUID.replaceAll("\\s", "");
        
        long integer;
        try {
            if (GUID.toLowerCase().startsWith("0x"))
                integer = Long.parseLong(GUID.substring(2), 16);
            else if (GUID.toLowerCase().startsWith("g"))
                integer = Long.parseLong(GUID.substring(1));
            else
                integer = Long.parseLong(GUID);
        } catch (NumberFormatException e) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }
        
        if (integer == nextGUID) db.lastGUID++;
        
        FileEntry entry = new FileEntry(Globals.lastSelected.path + Globals.lastSelected.header + "/" + file, integer);

        if (Globals.currentWorkspace == Globals.WorkspaceType.MOD)
            ((Mod) db).add(entry);
        else((FileDB) db).add(entry);

        db.shouldSave = true;

        TreePath treePath = new TreePath(db.addNode(entry).getPath());

        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());

        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);

        Toolkit.instance.updateWorkspace();

        System.out.println("Added entry! -> " + entry.path);

    } 
    
    public static void newFolder() {                                                 
        String folder = (String) JOptionPane.showInputDialog("Please input a name for the folder.");
        if (folder == null || folder.equals("")) return;

        TreePath treePath = null;
        if (Globals.lastSelected == null)
            treePath = new TreePath(Toolkit.instance.getCurrentDB().addNode(folder).getPath());
        else if (Globals.lastSelected.entry == null)
            treePath = new TreePath(Toolkit.instance.getCurrentDB().addNode(Globals.lastSelected.path + Globals.lastSelected.header + "/" + folder).getPath());

        JTree tree = Toolkit.instance.getCurrentTree();

        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());

        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
    }
    
    public static void changeHash() {
        FileNode node = Globals.lastSelected;
        FileEntry entry = node.entry;
        
        String SHA1 = JOptionPane.showInputDialog(Toolkit.instance, "File Hash", "h" + Bytes.toHex(entry.SHA1).toLowerCase());
        if (SHA1 == null) return;
        SHA1 = SHA1.replaceAll("\\s", "");
        
        byte[] hash;
        
        if (SHA1.startsWith("h"))
            SHA1 = SHA1.substring(1);
        hash = Bytes.toBytes(StringUtils.leftPad(SHA1, 40));
        
        entry.SHA1 = hash;
        
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        db.shouldSave = true;
        
        Toolkit.instance.updateWorkspace();
        Toolkit.instance.setEditorPanel(node);
    }
    
    public static void changeGUID() {
        FileNode node = Globals.lastSelected;
        FileEntry entry = node.entry;
        
        String GUID = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", "g" + entry.GUID);
        if (GUID == null) return;
        
        long parsedGUID = StringUtils.getLong(GUID);
        if (parsedGUID == -1) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }
        
        ((FileDB)Toolkit.instance.getCurrentDB()).edit(entry, parsedGUID);
        
        Toolkit.instance.updateWorkspace();
        Toolkit.instance.setEditorPanel(node);
    }
    
    public static void renameItem() {        
        FileNode node = Globals.lastSelected;
        FileEntry entry = node.entry;
        String path = (String) JOptionPane.showInputDialog(Toolkit.instance, "Rename", entry.path);
        if (path == null) return;
       
        
        JTree tree = Toolkit.instance.getCurrentTree();
        
        TreePath[] paths = tree.getSelectionPaths();
        TreeSelectionModel model = tree.getSelectionModel();
        int[] rows = tree.getSelectionRows();
        
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        
        db.rename(entry, path);
        node.removeFromParent();
        
        db.addNode(entry);
        
        ((FileModel) tree.getModel()).reload();

        db.shouldSave = true;
        
        Toolkit.instance.updateWorkspace();

        tree.setSelectionPaths(paths);
        tree.setSelectionRows(rows);
        tree.setSelectionModel(model);
    }
    
    public static void duplicateItem() {                                                 
        FileEntry entry = Globals.lastSelected.entry;
        
        String path = (String) JOptionPane.showInputDialog(Toolkit.instance, "Duplicate", entry.path);
        if (path == null) return;

        JTree tree = Toolkit.instance.getCurrentTree();

        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        FileEntry duplicate = new FileEntry(entry);
        duplicate.path = path;
        
        long nextGUID = db.lastGUID + 1;
        
        String GUID = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", "g" + entry.GUID);
        if (GUID == null) return;
        
        long parsedGUID = StringUtils.getLong(GUID);
        if (parsedGUID == -1) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }
        
        if (parsedGUID > nextGUID) db.lastGUID = parsedGUID;
        
        duplicate.GUID = parsedGUID;

        db.add(duplicate);
        TreePath treePath = new TreePath(db.addNode(duplicate).getPath());

        byte[] data = Globals.extractFile(entry.GUID);
        if (data != null) {
            Resource resource = new Resource(data);
            if (resource.magic.equals("PLNb")) {
                resource.getDependencies(entry);
                resource.removePlanDescriptors(entry.GUID, true);
                Globals.addFile(resource.data);
                duplicate.SHA1 = Bytes.SHA1(resource.data);
            }
        }

        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());

        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);

        Toolkit.instance.updateWorkspace();

        System.out.println("Duplicated entry!");
        System.out.println(entry.path + " -> " + duplicate.path);
    }
    
    public static void delete() {                                              
        JTree tree = Toolkit.instance.getCurrentTree();

        TreePath[] paths = tree.getSelectionPaths();
        TreeSelectionModel model = tree.getSelectionModel();
        int[] rows = tree.getSelectionRows();

        if (Globals.currentWorkspace != Globals.WorkspaceType.PROFILE) {
            FileData db = Toolkit.instance.getCurrentDB();
            for (FileNode node: Globals.entries) {
                FileEntry entry = node.entry;
                node.removeFromParent();
                if (node.entry == null) continue;
                if (Globals.currentWorkspace == Globals.WorkspaceType.MOD)((Mod) db).remove(entry);
                else((FileDB) db).remove(entry);
            }
        }

        if (Globals.currentWorkspace == Globals.WorkspaceType.PROFILE) {
            BigProfile profile = (BigProfile) Toolkit.instance.getCurrentDB();
            for (FileNode node: Globals.entries) {
                FileEntry entry = node.entry;
                node.removeFromParent();
                if (entry == null) continue;
                if (entry.slot != null) profile.slots.remove(entry.slot);
                if (entry.profileItem != null) profile.inventoryCollection.remove(entry.profileItem);
                profile.entries.remove(entry);
                profile.shouldSave = true;
            }
        }

        ((FileModel) tree.getModel()).reload();

        Toolkit.instance.updateWorkspace();

        tree.setSelectionPaths(paths);
        tree.setSelectionRows(rows);
        tree.setSelectionModel(model);
    }
    
    public static void newFileDB(int header) {
        Output output = new Output(0x8);
        output.i32(header);
        output.i32(0);
        File file = Toolkit.instance.fileChooser.openFile("blurayguids.map", "map", "FileDB", true);
        if (file == null) return;
        if (Toolkit.instance.confirmOverwrite(file)) {
            FileIO.write(output.buffer, file.getAbsolutePath());
            DatabaseCallbacks.loadFileDB(file);
        }
    }
}
