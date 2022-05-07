package ennuo.toolkit.utilities;

import ennuo.craftworld.types.data.ResourceDescriptor;
import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.types.BigStreamingFart;
import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.mods.Mod;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class Globals {
    public enum WorkspaceType {
        NONE,
        MAP,
        PROFILE,
        MOD,
        SAVE,
        
    }
    
    public static File workingDirectory;
    
    static {
        try {
            Globals.workingDirectory = Files.createTempDirectory("twd").toFile();
            Globals.workingDirectory.deleteOnExit();
        } catch (IOException ex) { System.out.println("An error occurred creating temp directory."); }
    }

    public static ArrayList<FileData> databases;
    public static ArrayList<FileArchive> archives;
    public static TranslationTable LAMS;
    private static TranslationTable KEYS;

    public static ArrayList<FileNode> entries;
    public static FileNode lastSelected;

    public static WorkspaceType currentWorkspace = WorkspaceType.NONE;
    
    public static void reset() {
        Globals.currentWorkspace = WorkspaceType.NONE;
        Globals.entries = new ArrayList<FileNode>();
        Globals.lastSelected = null;
        Globals.LAMS = null;
        Globals.KEYS = null;
        Globals.archives = new ArrayList<FileArchive>();
        Globals.databases = new ArrayList<FileData>();
    }

    public static boolean canExtract() {
        if (currentWorkspace == WorkspaceType.MAP)
            return archives.size() > 0;
        else if (currentWorkspace != WorkspaceType.MAP && currentWorkspace != WorkspaceType.NONE)
            return true;
        return false;
    }

    public static FileEntry findEntry(ResourceDescriptor res) {
        if (res.GUID != -1) return findEntry(res.GUID);
        else if (res.hash != null) return findEntry(res.hash);
        return null;
    }

    public static FileEntry findEntry(long GUID) {
        if (Globals.databases.size() == 0) return null;
        FileData db = Toolkit.instance.getCurrentDB();
        if (Globals.currentWorkspace == WorkspaceType.MAP) {
            FileEntry entry = ((FileDB) db).find(GUID);
            if (entry != null)
                return entry;
        } else if (Globals.currentWorkspace == WorkspaceType.MOD) {
            FileEntry entry = ((Mod) db).find(GUID);
            if (entry != null)
                return entry;
        }
        for (FileData data: Globals.databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB) data).find(GUID);
                if (entry != null)
                    return entry;
            }
        }
        return null;
    }

    public static FileEntry findEntry(SHA1 hash) {
        if (Globals.databases.size() == 0) return null;
        FileData db = Toolkit.instance.getCurrentDB();

        FileEntry e = db.find(hash);
        if (e != null)
            return e;

        for (FileData data: Globals.databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB) data).find(hash);
                if (entry != null)
                    return entry;
            }
        }
        return null;
    }

    public static byte[] extractFile(ResourceDescriptor ptr) {
        if (ptr == null) return null;
        if (ptr.hash != null) return extractFile(ptr.hash);
        else if (ptr.GUID != -1) return extractFile(ptr.GUID);
        return null;
    }

    public static byte[] extractFile(long GUID) {
        FileData db = Toolkit.instance.getCurrentDB();


        if (currentWorkspace == WorkspaceType.MAP) {
            FileEntry entry = ((FileDB) db).find(GUID);
            if (entry != null)
                return extractFile(entry.hash);
        } else if (currentWorkspace == WorkspaceType.MOD) {
            FileEntry entry = ((Mod) db).find(GUID);
            if (entry != null)
                return entry.data;
        }

        for (FileData data: Globals.databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB) data).find(GUID);
                if (entry != null) {
                    byte[] buffer = extractFile(entry.hash);
                    if (buffer != null) return buffer;
                }

            }
        }

        System.out.println("Could not extract g" + GUID);

        return null;
    }

    public static byte[] extractFile(SHA1 hash) {
        FileData db = Toolkit.instance.getCurrentDB();
        if (currentWorkspace == WorkspaceType.PROFILE) {
            byte[] data = ((BigStreamingFart) db).extract(hash);
            if (data != null) return data;
        } else if (currentWorkspace == WorkspaceType.MOD) {
            byte[] data = ((Mod) db).extract(hash);
            if (data != null) return data;
        }
        for (FileArchive archive: Globals.archives) {
            byte[] data = archive.extract(hash);
            if (data != null) return data;
        }
        
        System.out.println("Could not extract h" + hash.toString());
        
        return null;
    }

    public static void replaceEntry(FileEntry entry, byte[] data) {
        if (Globals.currentWorkspace != WorkspaceType.PROFILE) {
            entry.resetResources();
            if (Globals.currentWorkspace != WorkspaceType.MOD) {
                if (!Globals.addFile(data))
                    return; 
            }
        }

        Toolkit.instance.getCurrentDB().edit(entry, data);
        Toolkit.instance.updateWorkspace();

        JTree tree = Toolkit.instance.getCurrentTree();
        TreePath selectionPath = tree.getSelectionPath();
        ((FileModel) tree.getModel()).reload();
        tree.setSelectionPath(selectionPath);
        
    }

    public static boolean addFile(byte[] data) {
        if (Globals.currentWorkspace == WorkspaceType.PROFILE) {
            ((BigStreamingFart) Toolkit.instance.getCurrentDB()).add(data);
            Toolkit.instance.updateWorkspace();
            return true;
        }

        FileArchive[] archives = Toolkit.instance.getSelectedArchives();
        if (archives == null) return false;

        Globals.addFile(data, archives);
        return true;
    }

    public static void addFile(byte[] data, FileArchive[] archives) {
        for (FileArchive archive: archives)
            archive.add(data);
        Toolkit.instance.updateWorkspace();
        System.out.println("Added file to queue, make sure to save your workspace!");
    }
}
