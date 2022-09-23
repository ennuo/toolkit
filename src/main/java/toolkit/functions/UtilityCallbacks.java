package toolkit.functions;

import cwlib.util.Bytes;
import toolkit.utilities.FileChooser;
import toolkit.utilities.SlowOp;
import toolkit.windows.Toolkit;
import toolkit.windows.managers.ModManager;
import toolkit.windows.utilities.SlowOpGUI;
import cwlib.util.FileIO;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.archives.Fart;
import cwlib.types.archives.Fat;
import cwlib.types.archives.FileArchive;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.swing.FileData;
import cwlib.types.swing.FileModel;
import cwlib.types.swing.FileNode;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;
import cwlib.types.save.BigSave;

import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class UtilityCallbacks {
    public static void newMod() {
        File file = FileChooser.openFile("template.mod", "mod", true);
        if (file == null) return;
        if (Toolkit.instance.confirmOverwrite(file)) {
            Mod mod = new Mod();
            new ModManager(mod, true).setVisible(true);
            mod.save(file);
            mod = ModCallbacks.loadMod(file);
            if (mod != null) {
                Toolkit.instance.addTab(mod);
                Toolkit.instance.updateWorkspace();
            }
        }
    }
        
    public static void decompressResource() {                                                   
        File file = FileChooser.openFile("data.bin", null, false);
        if (file == null) return;

        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data == null) return;

        Resource resource = new Resource(data);
        
        File out = FileChooser.openFile(file.getName() + ".dec", null, true);
        if (out != null)
            FileIO.write(resource.getStream().getBuffer(), out.getAbsolutePath());
    }
    
    public static void mergeFileArchives() {         
        File file = FileChooser.openFile("base.farc", "farc", false);
        if (file == null) return;
         
        Fart cache;
        int index = Toolkit.instance.isArchiveLoaded(file);
        if (index != -1) cache = ResourceSystem.getArchives().get(index);
        else cache = new FileArchive(file);

        file = FileChooser.openFile("patch.farc", "farc", false);
        if (file == null) return;
        
        FileArchive patch = new FileArchive(file);
        
        // Flush archives at 256MBs, maybe make this configurable, or auto-calculate some
        // appropriate value based on lower end systems.
        final int CACHE_SIZE = 268_435_456;
        
        SlowOpGUI.performSlowOperation(Toolkit.instance, "Merging Archives", patch.getEntryCount(), new SlowOp() {
            private int current = 0;
            
            @Override public int run(SlowOpGUI state) {
                for (Fat fat : patch) {
                    if (state.wantQuit()) return -1;
                    
                    // Save if we have too much stored in memory currently.
                    if (cache.getQueueSize() >= CACHE_SIZE)
                        cache.save();
                    
                    cache.add(patch.extract(fat.getSHA1()));
                    
                    current++;
                }
                
                cache.save();
                
                return 0;
            }
            
            @Override public int getProgress() { return this.current; }
        });
    }
    
    public static void generateFileDBDiff() {                                             
        File baseFile = FileChooser.openFile("blurayguids.map", "map", false);
        if (baseFile == null) return;
        
        FileDB base = null;
        try { base = new FileDB(baseFile); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    Toolkit.instance,
                    String.format("Failed to load base database (%s). Are you sure it's valid?", baseFile.getName()),
                    "An error occurred",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        File updateFile = FileChooser.openFile("blurayguids.map", "map", false);
        if (updateFile == null) return;

        FileDB update = null;
        try { update = new FileDB(updateFile); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    Toolkit.instance,
                    String.format("Failed to load patch database (%s). Are you sure it's valid?", updateFile.getName()),
                    "An error occurred",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        StringBuilder builder = new StringBuilder(update.getEntryCount() * 255);
        for (FileDBRow row : update) {
            FileDBRow existing = base.get(row.getGUID());
            if (existing == null)
                builder.append(String.format("[+] path=%s size=%s sha1=%s guid=%s\n", row.getPath(), row.getSize(), row.getSHA1(), row.getGUID()));
            else {
                builder.append(String.format(
                        "[~] path=%s->%s size=%s->%s sha1=%s->%s guid=%s->%s\n",
                        existing.getPath(), row.getPath(),
                        existing.getSize(), row.getSize(),
                        existing.getSHA1(), row.getSHA1(),
                        existing.getGUID(), row.getGUID()
                ));
            }
        }
        

        File destination = FileChooser.openFile("diff.txt", "txt", true);
        if (destination == null) return;

        FileIO.write(builder.toString().getBytes(), destination.getAbsolutePath());
    }
    
    public static void installMod() {                                                  
        File[] files = FileChooser.openFiles("mod");
        if (files == null) return;

        FileData database = ResourceSystem.getSelectedDatabase();

        for (int i = 0; i < files.length; ++i) {
            Mod mod = ModCallbacks.loadMod(files[i]);
            if (mod == null) continue;


            if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.PROFILE) {
                BigSave profile = (BigSave) Toolkit.instance.getCurrentDB();
                for (FileEntry entry: mod.entries)
                    profile.add(entry.data, true);
            } else if (ResourceSystem.getDatabaseType() == Globals.ResourceSystem.MAP) {
                if (mod.entries.size() == 0) return;
                FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
                FileArchive[] archives = Toolkit.instance.getSelectedArchives();
                if (archives == null) return;
                for (FileEntry entry: mod.entries) {
                    if (db.add(entry))
                        db.addNode(entry);
                    ResourceSystem.add(entry.data, archives);
                }
            }
        }

        database.setHasChanges();
        ResourceSystem.reloadModel(database);
    }
}
