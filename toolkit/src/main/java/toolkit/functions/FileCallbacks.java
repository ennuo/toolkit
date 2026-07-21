package toolkit.functions;

import cwlib.singleton.ResourceSystem;
import cwlib.types.archives.Fart;
import cwlib.types.swing.FileData;
import sync.NetworkFileDB;
import sync.SyncManager;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

import javax.swing.*;
import java.io.File;

public class FileCallbacks
{
    public static void save()
    {
        FileData database = ResourceSystem.getSelectedDatabase();
        if (database == null && ResourceSystem.getArchives().size() == 0) return;
        System.out.println("Saving workspace...");
        if (database != null && !database.isRemote())
        {
            if (database.hasChanges())
            {
                System.out.println("Saving " + database.getType() + " at " + database.getFile());
                database.save();
            }
            else
                System.out.println(database.getType() + " has no pending changes, " +
                                   "skipping save.");
        }

        for (Fart archive : ResourceSystem.getArchives())
        {
            if (archive.shouldSave())
            {
                System.out.println("Saving FileArchive at " + archive.getFile().getAbsolutePath());
                archive.save();
            }
            else System.out.println("FileArchive has no pending changes, skipping save.");
        }
    

        if (database != null && database.isRemote())
        {
            database.promptSave();
        }

        Toolkit.INSTANCE.updateWorkspace();
    }

    public static void saveAs()
    {
        FileData database = ResourceSystem.getSelectedDatabase();

        File file = FileChooser.openFile(database.getName(), database.getType().getExtension(),
            true);
        if (file == null) return;
        database.save(file);
    }

    public static void closeTab()
    {
        int index = Toolkit.INSTANCE.fileDataTabs.getSelectedIndex();

        FileData database = ResourceSystem.getSelectedDatabase();
        database.promptSave();
        if (database instanceof NetworkFileDB remote)
            SyncManager.instance.clear(remote.getDepot().Id);

        ResourceSystem.getDatabases().remove(index);
        Toolkit.INSTANCE.fileDataTabs.removeTabAt(index);
    }
}
