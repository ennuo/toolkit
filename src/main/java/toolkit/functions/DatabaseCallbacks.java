package toolkit.functions;

import cwlib.resources.RPlan;
import cwlib.util.FileIO;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.types.Resource;
import cwlib.enums.ResourceType;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;
import cwlib.structs.profile.InventoryItem;
import cwlib.types.data.GUID;
import cwlib.types.data.SHA1;
import cwlib.structs.slot.Slot;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;
import cwlib.types.save.BigSave;

import java.awt.Color;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
        JTree tree = database.getTree();

        if (!database.getType().hasGUIDs()) return;
        
        long nextGUID = database.getNextGUID().getValue();
        
        String input = JOptionPane.showInputDialog(Toolkit.instance, "File GUID", "g" + nextGUID);
        if (input == null) return;
        input = input.replaceAll("\\s", "");
        
        
        long parsedGUID = Strings.getLong(input);
        if (parsedGUID == -1) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }

        GUID guid = new GUID(parsedGUID);

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

        System.out.println("Added entry! -> " + entry.getPath());

    } 
    
    public static void newFolder() {                                                 
        String folder = (String) JOptionPane.showInputDialog("Please input a name for the folder.");
        if (folder == null || folder.equals("")) return;

        TreePath treePath = null;
        if (ResourceSystem.getSelected() == null)
            treePath = new TreePath(Toolkit.instance.getCurrentDB().addNode(folder).getPath());
        else if (ResourceSystem.getSelected().getEntry() == null)
            treePath = new TreePath(Toolkit.instance.getCurrentDB().addNode(ResourceSystem.getSelected().path + ResourceSystem.getSelected().getName() + "/" + folder).getPath());

        JTree tree = Toolkit.instance.getCurrentTree();

        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());

        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
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
        if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.MOD)
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
        FileEntry entry = ResourceSystem.getSelected().getEntry();

        String path = (String) JOptionPane.showInputDialog(Toolkit.instance, "Rename", entry.getPath());
        if (path == null) return;

        entry.setPath(path);

        Toolkit.instance.updateWorkspace();
    }
    
    public static void duplicateItem() {                                                 
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        
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
        if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.MOD)
            alreadyExists = ((Mod) db).find(parsedGUID) != null;
        else alreadyExists = ((FileDB) db).find(parsedGUID) != null;
        
        if (alreadyExists) {
            System.err.println("This GUID already exists!");
            return;
        }
        
        if (parsedGUID > nextGUID) db.lastGUID = parsedGUID;
        else if (parsedGUID == nextGUID) db.lastGUID++;
        
        duplicate.GUID = parsedGUID;

        if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.MOD)
            ((Mod)db).add(duplicate);
        else
            ((FileDB)db).add(duplicate);
        
        TreePath treePath = new TreePath(db.addNode(duplicate).getPath());

        byte[] data = ResourceSystem.extract(entry.GUID);
        if (data != null) {
            Resource resource = new Resource(data);
            if (resource.type == ResourceType.PLAN) {
                RPlan.removePlanDescriptors(resource, entry.GUID);
                data = resource.compressToResource();
                ResourceSystem.add(data);
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
        if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.NONE)
            return;
        JTree tree = Toolkit.instance.getCurrentTree();

        TreePath[] paths = tree.getSelectionPaths();
        TreeSelectionModel model = tree.getSelectionModel();
        int[] rows = tree.getSelectionRows();
        
        if (rows == null || rows.length == 0)
            return;

        if (ResourceSystem.getDatabaseType() != Globals.ResourceSystem.PROFILE) {
            FileData db = Toolkit.instance.getCurrentDB();
            for (FileNode node: ResourceSystem.selected) {
                FileEntry entry = node.entry;
                node.removeFromParent();
                if (node.entry == null) continue;
                if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.MOD)((Mod) db).remove(entry);
                else((FileDB) db).remove(entry);
            }
        }

        if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.PROFILE) {
            BigSave profile = (BigSave) Toolkit.instance.getCurrentDB();
            for (FileNode node: ResourceSystem.selected) {
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
            FileIO.write(output.getBuffer(), file.getAbsolutePath());
            DatabaseCallbacks.loadFileDB(file);
        }
    }
}
