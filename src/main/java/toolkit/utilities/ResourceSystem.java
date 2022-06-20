package toolkit.utilities;

import cwlib.types.data.GUID;
import cwlib.types.data.ResourceReference;
import cwlib.resources.RTranslationTable;
import cwlib.types.data.SHA1;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.types.BigSave;
import cwlib.types.archives.Fart;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;
import toolkit.windows.Toolkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * Global utilities for working with
 * loaded databases.
 */
public class ResourceSystem {
    public static File workingDirectory;
    
    static {
        try {
            ResourceSystem.workingDirectory = Files.createTempDirectory("twd").toFile();
            ResourceSystem.workingDirectory.deleteOnExit();
        } catch (IOException ex) { System.out.println("An error occurred creating temp directory."); }
    }

    public static ArrayList<FileData> databases;
    public static ArrayList<Fart> archives;


    public static RTranslationTable LAMS;

    public static ArrayList<FileNode> entries;
    public static FileNode lastSelected;

    public static WorkspaceType currentWorkspace = WorkspaceType.NONE;
    
    public static void reset() {
        ResourceSystem.currentWorkspace = WorkspaceType.NONE;
        ResourceSystem.entries = new ArrayList<FileNode>();
        ResourceSystem.lastSelected = null;
        ResourceSystem.LAMS = null;
        ResourceSystem.KEYS = null;
        ResourceSystem.archives = new ArrayList<Fart>();
        ResourceSystem.databases = new ArrayList<FileData>();
    }

    public static boolean canExtract() {
        if (currentWorkspace == WorkspaceType.MAP)
            return archives.size() > 0;
        else if (currentWorkspace != WorkspaceType.MAP && currentWorkspace != WorkspaceType.NONE)
            return true;
        return false;
    }

    public static FileEntry findEntry(ResourceReference res) {
        if (res.GUID != -1) return findEntry(res.GUID);
        else if (res.hash != null) return findEntry(res.hash);
        return null;
    }

    public static FileEntry findEntry(long GUID) {
        if (ResourceSystem.databases.size() == 0) return null;
        FileData db = Toolkit.instance.getCurrentDB();
        if (ResourceSystem.currentWorkspace == WorkspaceType.MAP) {
            FileEntry entry = ((FileDB) db).find(GUID);
            if (entry != null)
                return entry;
        } else if (ResourceSystem.currentWorkspace == WorkspaceType.MOD) {
            FileEntry entry = ((Mod) db).find(GUID);
            if (entry != null)
                return entry;
        }
        for (FileData data: ResourceSystem.databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB) data).find(GUID);
                if (entry != null)
                    return entry;
            }
        }
        return null;
    }

    public static FileEntry findEntry(SHA1 hash) {
        if (ResourceSystem.databases.size() == 0) return null;
        FileData db = Toolkit.instance.getCurrentDB();

        FileEntry e = db.find(hash);
        if (e != null)
            return e;

        for (FileData data: ResourceSystem.databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB) data).find(hash);
                if (entry != null)
                    return entry;
            }
        }
        return null;
    }

    public static byte[] extractFile(ResourceReference ref) {
        if (ref == null) return null;
        if (ref.isHash()) return ResourceSystem.extractFile(ref.getSHA1());
        else if (ref.isGUID()) return ResourceSystem.extractFile(ref.getGUID());
        return null;
    }

    public static byte[] extractFile(long guid) { return ResourceSystem.extractFile(new GUID(guid)); }
    public static byte[] extractFile(GUID guid) {
        FileData db = Toolkit.instance.getCurrentDB();


        if (currentWorkspace == WorkspaceType.MAP) {
            FileEntry entry = ((FileDB) db).find(guid);
            if (entry != null)
                return extractFile(entry.hash);
        } else if (currentWorkspace == WorkspaceType.MOD) {
            FileEntry entry = ((Mod) db).find(guid);
            if (entry != null)
                return entry.data;
        }

        for (FileData data: ResourceSystem.databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB) data).find(guid);
                if (entry != null) {
                    byte[] buffer = extractFile(entry.hash);
                    if (buffer != null) return buffer;
                }

            }
        }

        System.out.println("Could not extract g" + guid);

        return null;
    }

    public static byte[] extractFile(SHA1 hash) {
        FileData db = Toolkit.instance.getCurrentDB();
        if (currentWorkspace == WorkspaceType.PROFILE) {
            byte[] data = ((BigSave) db).extract(hash);
            if (data != null) return data;
        } else if (currentWorkspace == WorkspaceType.MOD) {
            byte[] data = ((Mod) db).extract(hash);
            if (data != null) return data;
        }
        for (Fart fart: ResourceSystem.archives) {
            byte[] data = fart.extract(hash);
            if (data != null) return data;
        }
        
        System.out.println("Could not extract h" + hash.toString());
        
        return null;
    }

    public static void replaceEntry(FileEntry entry, byte[] data) {
        if (ResourceSystem.currentWorkspace != WorkspaceType.PROFILE) {
            entry.resetResources();
            if (ResourceSystem.currentWorkspace != WorkspaceType.MOD) {
                if (!ResourceSystem.addFile(data))
                    return; 
            }
        }

        entry.setDetails(data);
        Toolkit.instance.updateWorkspace();

        JTree tree = Toolkit.instance.getCurrentTree();
        TreePath selectionPath = tree.getSelectionPath();
        ((FileModel) tree.getModel()).reload();
        tree.setSelectionPath(selectionPath);
        
    }

    public static boolean addFile(byte[] data) {
        if (ResourceSystem.currentWorkspace == WorkspaceType.PROFILE) {
            ((BigSave) Toolkit.instance.getCurrentDB()).add(data);
            Toolkit.instance.updateWorkspace();
            return true;
        }

        Fart[] archives = Toolkit.instance.getSelectedArchives();
        if (archives == null) return false;

        ResourceSystem.addFile(data, archives);
        return true;
    }

    public static void addFile(byte[] data, Fart[] farts) {
        for (Fart fart: farts)
            fart.add(data);
        Toolkit.instance.updateWorkspace();
        System.out.println("Added file to queue, make sure to save your workspace!");
    }
}
