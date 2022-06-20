package toolkit.functions;

import cwlib.types.swing.FileData;
import cwlib.types.FileArchive;
import toolkit.utilities.FileChooser;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import static toolkit.windows.Toolkit.trees;

import java.io.File;
import javax.swing.JOptionPane;

public class FileCallbacks {
    public static void save() {                                         
        FileData db = Toolkit.instance.getCurrentDB();
        if (db == null && ResourceSystem.archives.size() == 0) return;
        System.out.println("Saving workspace...");
        if (db != null) {
            if (db.shouldSave) {
                System.out.println("Saving " + db.type + " at " + db.path);
                db.save(db.path);
            } else System.out.println(db.type + " has no pending changes, skipping save.");
        }

        for (FileArchive archive: ResourceSystem.archives) {
            if (archive.shouldSave) {
                System.out.println("Saving FileArchive at " + archive.file.getAbsolutePath());
                archive.save();
            } else System.out.println("FileArchive has no pending changes, skipping save.");
        }
        
        Toolkit.instance.updateWorkspace();
    }    
    
    public static void saveAs() {                                       
        String ext = "", type = "";
        if (ResourceSystem.currentWorkspace == Globals.ResourceSystem.PROFILE) {
            type = "Big Profile";
        } else if (ResourceSystem.currentWorkspace == Globals.ResourceSystem.MOD) {
            ext = "mod";
            type = "Mod";
        } else {
            ext = "map";
            type = "FileDB";
        }

        FileData db = Toolkit.instance.getCurrentDB();

        File file = FileChooser.openFile(db.name, ext, true);
        if (file == null) return;
        db.save(file.getAbsolutePath());
    }
    
    public static void closeTab() {                                         
        int index = Toolkit.instance.fileDataTabs.getSelectedIndex();

        FileData data = Toolkit.instance.getCurrentDB();
        if (data.shouldSave) {
            int result = JOptionPane.showConfirmDialog(null, String.format("Your %s (%s) has pending changes, do you want to save?", data.type, data.path), "Pending changes", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) data.save(data.path);
        }

        ResourceSystem.databases.remove(index);
        trees.remove(index);
        Toolkit.instance.fileDataTabs.removeTabAt(index);
    }  
}
