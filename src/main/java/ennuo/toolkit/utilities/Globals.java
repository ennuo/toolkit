package ennuo.toolkit.utilities;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.ResourcePtr;
import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.swing.FileData;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.types.BigProfile;
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

    public static ArrayList < FileData > databases = new ArrayList < FileData > ();
    public static ArrayList < FileArchive > archives = new ArrayList < FileArchive > ();
    public static TranslationTable LAMS;
    private static TranslationTable KEYS;

    public static ArrayList < FileNode > entries = new ArrayList < FileNode > ();
    public static FileNode lastSelected;

    public static WorkspaceType currentWorkspace = WorkspaceType.NONE;

    public static boolean canExtract() {
        if (currentWorkspace == WorkspaceType.MAP)
            return archives.size() > 0;
        else if (currentWorkspace != WorkspaceType.MAP && currentWorkspace != WorkspaceType.NONE)
            return true;
        return false;
    }

    public static FileEntry findEntry(ResourcePtr res) {
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

    public static FileEntry findEntry(byte[] sha1) {
        if (Globals.databases.size() == 0) return null;
        FileData db = Toolkit.instance.getCurrentDB();

        FileEntry e = db.find(sha1);
        if (e != null)
            return e;

        for (FileData data: Globals.databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB) data).find(sha1);
                if (entry != null)
                    return entry;
            }
        }
        return null;
    }

    public static byte[] extractFile(ResourcePtr ptr) {
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
                return extractFile(entry.SHA1);
        } else if (currentWorkspace == WorkspaceType.MOD) {
            FileEntry entry = ((Mod) db).find(GUID);
            if (entry != null)
                return entry.data;
        }

        for (FileData data: Globals.databases) {
            if (data.type.equals("FileDB")) {
                FileEntry entry = ((FileDB) data).find(GUID);
                if (entry != null) {
                    byte[] buffer = extractFile(entry.SHA1);
                    if (buffer != null) return buffer;
                }

            }
        }

        System.out.println("Could not extract g" + GUID);

        return null;
    }

    public static byte[] extractFile(byte[] sha1) {
        FileData db = Toolkit.instance.getCurrentDB();
        if (currentWorkspace == WorkspaceType.PROFILE) {
            byte[] data = ((BigProfile) db).extract(sha1);
            if (data != null) return data;
        } else if (currentWorkspace == WorkspaceType.MOD) {
            byte[] data = ((Mod) db).extract(sha1);
            if (data != null) return data;
        }
        for (FileArchive archive: Globals.archives) {
            byte[] data = archive.extract(sha1);
            if (data != null) return data;
        }
        
        System.out.println("Could not extract h" + Bytes.toHex(sha1));
        
        return null;
    }

    public static void replaceEntry(FileEntry entry, byte[] data) {
        if (Globals.currentWorkspace != WorkspaceType.PROFILE) {
            entry.resetResources();
            if (Globals.currentWorkspace != WorkspaceType.MOD)
                addFile(data);
        }

        Toolkit.instance.getCurrentDB().edit(entry, data);
        Toolkit.instance.updateWorkspace();

        JTree tree = Toolkit.instance.getCurrentTree();
        TreePath selectionPath = tree.getSelectionPath();
        ((FileModel) tree.getModel()).reload();
        tree.setSelectionPath(selectionPath);
    }

    public static void addFile(byte[] data) {
        if (Globals.currentWorkspace == WorkspaceType.PROFILE) {
            ((BigProfile) Toolkit.instance.getCurrentDB()).add(data);
            Toolkit.instance.updateWorkspace();
            return;
        }

        FileArchive[] archives = Toolkit.instance.getSelectedArchives();
        if (archives == null) return;

        addFile(data, archives);

    }

    public static void addFile(byte[] data, FileArchive[] archives) {
        for (FileArchive archive: Globals.archives)
            archive.add(data);
        Toolkit.instance.updateWorkspace();
        System.out.println("Added file to queue, make sure to save your workspace!");
    }

    public static String Translate(long key) {
        String translated = null;
        if (LAMS != null) translated = LAMS.translate(key);
        if (translated == null && KEYS != null)
            translated = KEYS.translate(key);
        return translated;
    }
}
