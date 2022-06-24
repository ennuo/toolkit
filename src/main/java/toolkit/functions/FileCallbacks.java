package toolkit.functions;

import cwlib.types.swing.FileData;
import cwlib.types.archives.Fart;
import toolkit.utilities.FileChooser;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import static toolkit.windows.Toolkit.trees;

import java.io.File;
import javax.swing.JOptionPane;

public class FileCallbacks {
    public static void save() {                                         
        FileData db = Toolkit.instance.getCurrentDB();
        if (db == null && ResourceSystem.getArchives().size() == 0) return;
        System.out.println("Saving workspace...");
        if (db != null) {
            if (db.hasChanges()) {
                System.out.println("Saving " + db.getType() + " at " + db.getFile());
                db.save();
            } else System.out.println(db.getType() + " has no pending changes, skipping save.");
        }

        for (Fart archive: ResourceSystem.getArchives()) {
            if (archive.shouldSave()) {
                System.out.println("Saving FileArchive at " + archive.getFile().getAbsolutePath());
                archive.save();
            } else System.out.println("FileArchive has no pending changes, skipping save.");
        }
        
        Toolkit.instance.updateWorkspace();
    }    
    
    public static void saveAs() {                                       
        FileData db = Toolkit.instance.getCurrentDB();

        File file = FileChooser.openFile(db.getName(), db.getType().getExtension(), true);
        if (file == null) return;
        db.save(file);
    }
    
    public static void closeTab() {                                         
        int index = Toolkit.instance.fileDataTabs.getSelectedIndex();

        FileData data = Toolkit.instance.getCurrentDB();
        if (data.hasChanges()) {
            int result = JOptionPane.showConfirmDialog(null, String.format("Your %s (%s) has pending changes, do you want to save?", data.getType(), data.getFile().getAbsolutePath()), "Pending changes", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) data.save(data.getFile());
        }

        ResourceSystem.getDatabases().remove(index);
        trees.remove(index);
        Toolkit.instance.fileDataTabs.removeTabAt(index);
    }  
}
