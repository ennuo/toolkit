package sync;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import configurations.Config;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.util.FileIO;
import toolkit.windows.Toolkit;

public class NetworkFileDB extends FileDB 
{
    private Depot _depot;

    public NetworkFileDB(Depot depot, byte[] fileData)
    {
        super(fileData);
        _depot = depot;
    }

    public Depot getDepot() { return _depot; }

    public CommitInfo generateChanges()
    {
        var info = new CommitInfo();
        info.id = _depot.Id;
        var files = info.files;

        // check for any additions/updates
        for (var workingRow : _depot.WorkingDatabase)
        {   
            var existingRow = _depot.Database.get(workingRow.getGUID());

            if (existingRow == null)
            {
                info.numAdditions += 1;
            }
            else if (!existingRow.getSHA1().equals(workingRow.getSHA1()) || !existingRow.getPath().equals(workingRow.getPath()))
            {
                info.numChanges += 1;
            }
            else continue;

            files.add(CommitFile.fromRow(workingRow));
        }

        // check for deletions
        for (var existingRow : _depot.Database)
        {
            var workingRow = _depot.WorkingDatabase.get(existingRow.getGUID());
            if (workingRow == null)
            {
                files.add(CommitFile.createDeleted(existingRow.getGUID()));
                info.numDeletions += 1;
            }
        }
    
        return info;
    }


    @Override public void promptSave()
    {
        var info = generateChanges();
        hasChanges = info.files.size() != 0;
        if (!hasChanges) return;

        int result = JOptionPane.showConfirmDialog(null, String.format("Depot %s has pending changes, do you want to commit them? (added=%d, removed=%d, changed=%d)", _depot.DisplayName, info.numAdditions, info.numDeletions, info.numChanges), "Pending changes", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION && SyncManager.instance.commit(info))
            hasChanges = false;
    }

    @Override
    public boolean save(File file)
    {
        if (file == null)
            throw new IllegalStateException("Can't save to non-existent file!");
        boolean success = FileIO.write(this.build(), file.getAbsolutePath());
        return success;
    }

    @Override public String getName()
    {
        return _depot.DisplayName;
    }

    @Override public boolean isRemote()
    {
        return true;
    }
}
