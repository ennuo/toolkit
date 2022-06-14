package toolkit.functions;

import cwlib.resources.RPlan;
import cwlib.util.FileIO;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.utilities.Globals;
import toolkit.windows.Toolkit;
import cwlib.structs.profile.InventoryItem;
import cwlib.types.data.SHA1;
import cwlib.structs.slot.Slot;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.types.BigSave;
import cwlib.types.FileDB;
import cwlib.types.FileEntry;
import cwlib.types.mods.Mod;

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
        File file = FileChooser.openFile("brg_patch.map", "map", false);
        if (file == null) return;
        FileDB newDB = new FileDB(file);
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        int added = 0, patched = 0;
        for (FileEntry entry: newDB.entries) {
            if (db.add(entry) && !FileDB.isHidden(entry.path)) {
                db.addNode(entry);
                added++;
            } else patched++;
        }
        Toolkit.instance.updateWorkspace();
        System.out.println(String.format("Succesfully updated FileDB (added = %d, patched = %d)", added, patched));
        ((FileModel) Toolkit.instance.getCurrentTree().getModel()).reload();
    }

    public static void dumpRLST() {
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        String str = db.toRLST();

        File file = FileChooser.openFile("poppet_inventory_empty.rlst", "rlst", true);
        if (file == null) return;

        FileIO.write(str.getBytes(), file.getAbsolutePath());
    }

    public static void dumpHashes() {
        File file = FileChooser.openFile("hashes.txt", "txt", true);
        if (file == null) return;
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        StringBuilder builder = new StringBuilder(0x100 * db.entries.size());
        for (FileEntry entry: db.entries)
            builder.append(entry.hash.toString() + '\n');
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
        
        
        long parsedGUID = Strings.getLong(GUID);
        if (parsedGUID == -1) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }
        
        boolean alreadyExists = false;
        if (Globals.currentWorkspace == Globals.WorkspaceType.MOD)
            alreadyExists = ((Mod) db).find(parsedGUID) != null;
        else alreadyExists = ((FileDB) db).find(parsedGUID) != null;
        
        if (alreadyExists) {
            System.err.println("This GUID already exists!");
            return;
        }
        
        if (parsedGUID > nextGUID) db.lastGUID = parsedGUID;
        else if (parsedGUID == nextGUID) db.lastGUID++;
        
        FileEntry entry = new FileEntry(Globals.lastSelected.path + Globals.lastSelected.header + "/" + file, parsedGUID);

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
        
        String hash = JOptionPane.showInputDialog(Toolkit.instance, "File Hash", "h" + entry.hash.toString().toLowerCase());
        if (hash == null) return;
        hash = hash.replaceAll("\\s", "");
        if (hash.startsWith("h"))
            hash = hash.substring(1);
        entry.hash = new SHA1(hash);
        
        FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
        db.shouldSave = true;
        
        Toolkit.instance.updateWorkspace();
        Toolkit.instance.setEditorPanel(node);
    }
    
    public static void changeGUID() {
        FileNode node = Globals.lastSelected;
        FileEntry entry = node.entry;
        FileData db = Toolkit.instance.getCurrentDB();
        
        String GUID = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", "g" + entry.GUID);
        if (GUID == null) return;
        
        long parsedGUID = Strings.getLong(GUID);
        if (parsedGUID == -1) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }
        
        if (parsedGUID == entry.GUID) {
            System.err.println("The GUID is unchanged!");
            return;
        }
        
        boolean alreadyExists = false;
        if (Globals.currentWorkspace == Globals.WorkspaceType.MOD)
            alreadyExists = ((Mod) db).find(parsedGUID) != null;
        else alreadyExists = ((FileDB) db).find(parsedGUID) != null;
        
        
        if (alreadyExists) {
            System.err.println("This GUID already exists!");
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

        FileData db = Toolkit.instance.getCurrentDB();
        FileEntry duplicate = new FileEntry(entry);
        duplicate.path = path;
        
        long nextGUID = db.lastGUID + 1;
        
        String GUID = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", "g" + nextGUID);
        if (GUID == null) return;
        
        long parsedGUID = Strings.getLong(GUID);
        if (parsedGUID == -1) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }
        
        boolean alreadyExists = false;
        if (Globals.currentWorkspace == Globals.WorkspaceType.MOD)
            alreadyExists = ((Mod) db).find(parsedGUID) != null;
        else alreadyExists = ((FileDB) db).find(parsedGUID) != null;
        
        if (alreadyExists) {
            System.err.println("This GUID already exists!");
            return;
        }
        
        if (parsedGUID > nextGUID) db.lastGUID = parsedGUID;
        else if (parsedGUID == nextGUID) db.lastGUID++;
        
        duplicate.GUID = parsedGUID;

        if (Globals.currentWorkspace == Globals.WorkspaceType.MOD)
            ((Mod)db).add(duplicate);
        else
            ((FileDB)db).add(duplicate);
        
        TreePath treePath = new TreePath(db.addNode(duplicate).getPath());

        byte[] data = Globals.extractFile(entry.GUID);
        if (data != null) {
            Resource resource = new Resource(data);
            if (resource.type == ResourceType.PLAN) {
                RPlan.removePlanDescriptors(resource, entry.GUID);
                data = resource.compressToResource();
                Globals.addFile(data);
                duplicate.hash = SHA1.fromBuffer(data);
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
        if (Globals.currentWorkspace == Globals.WorkspaceType.NONE)
            return;
        JTree tree = Toolkit.instance.getCurrentTree();

        TreePath[] paths = tree.getSelectionPaths();
        TreeSelectionModel model = tree.getSelectionModel();
        int[] rows = tree.getSelectionRows();
        
        if (rows == null || rows.length == 0)
            return;

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
            BigSave profile = (BigSave) Toolkit.instance.getCurrentDB();
            for (FileNode node: Globals.entries) {
                FileEntry entry = node.entry;
                node.removeFromParent();
                if (entry == null) continue;
                Slot slot = entry.getResource("slot");
                if (slot != null) profile.bigProfile.myMoonSlots.remove(slot.id);
                InventoryItem item = entry.getResource("profileItem");
                if (item != null) profile.bigProfile.inventory.remove(item);
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
        MemoryOutputStream output = new MemoryOutputStream(0x8);
        output.i32(header);
        output.i32(0);
        File file = FileChooser.openFile("blurayguids.map", "map", true);
        if (file == null) return;
        if (Toolkit.instance.confirmOverwrite(file)) {
            FileIO.write(output.buffer, file.getAbsolutePath());
            DatabaseCallbacks.loadFileDB(file);
        }
    }
}
