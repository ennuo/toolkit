package toolkit.functions;

import cwlib.types.swing.FileData;
import cwlib.singleton.ResourceSystem;
import cwlib.types.archives.Fart;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

import java.io.File;
import javax.swing.JOptionPane;

public class FileCallbacks {
    public static void save() {                                         
        FileData database = ResourceSystem.getSelectedDatabase();
        if (database == null && ResourceSystem.getArchives().size() == 0) return;
        System.out.println("Saving workspace...");
        if (database != null) {
            if (database.hasChanges()) {
                System.out.println("Saving " + database.getType() + " at " + database.getFile());
                database.save();
            } else System.out.println(database.getType() + " has no pending changes, skipping save.");
        }

        for (Fart archive: ResourceSystem.getArchives()) {
            if (archive.shouldSave()) {
                System.out.println("Saving FileArchive at " + archive.getFile().getAbsolutePath());
                archive.save();
            } else System.out.println("FileArchive has no pending changes, skipping save.");
        }
        
        Toolkit.INSTANCE.updateWorkspace();
    }    
    
    public static void saveAs() {                                       
        FileData database = ResourceSystem.getSelectedDatabase();

        File file = FileChooser.openFile(database.getName(), database.getType().getExtension(), true);
        if (file == null) return;
        database.save(file);
    }
    
    public static void closeTab() {                                         
        int index = Toolkit.INSTANCE.fileDataTabs.getSelectedIndex();

        FileData database = ResourceSystem.getSelectedDatabase();
        if (database.hasChanges()) {
            int result = JOptionPane.showConfirmDialog(null, String.format("Your %s (%s) has pending changes, do you want to save?", database.getType(), database.getFile().getAbsolutePath()), "Pending changes", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) database.save(database.getFile());
        }

        ResourceSystem.getDatabases().remove(index);
        Toolkit.INSTANCE.fileDataTabs.removeTabAt(index);
    }  
}
