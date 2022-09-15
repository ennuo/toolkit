package toolkit.functions;

import cwlib.util.Bytes;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;
import toolkit.windows.managers.ModManager;
import cwlib.util.FileIO;
import cwlib.io.streams.MemoryOutputStream;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.archives.Fart;
import cwlib.types.archives.FileArchive;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
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
        Toolkit toolkit = Toolkit.instance;
        File base = FileChooser.openFile("base.farc", "farc", false);
        if (base == null) return;
        Fart archive;
        int index = toolkit.isArchiveLoaded(base);
        if (index != -1) archive = ResourceSystem.getArchives().get(index);
        else archive = new FileArchive(base);

        if (archive == null) {
            System.err.println("Base FileArchive is null! Aborting!");
            return;
        }

        File patch = FileChooser.openFile("patch.farc", "farc", false);
        if (patch == null) return;
        FileArchive pArchive = new FileArchive(patch);

        if (pArchive == null) {
            System.err.println("Patch FileArchive is null! Aborting!");
            return;
        }

        toolkit.resourceService.submit(() -> {
            toolkit.progressBar.setVisible(true);
            toolkit.progressBar.setMaximum(pArchive.entries.size());
            toolkit.progressBar.setValue(0);
            int count = 0;
            for (FileEntry entry: pArchive.entries) {
                toolkit.progressBar.setValue(count + 1);

                byte[] data = pArchive.extract(entry);

                int querySize = ((data.length * 10) + archive.queueSize + archive.hashTable.length + 8 + (archive.entries.size() * 0x1C)) * 2;
                if (querySize < 0 || querySize >= Integer.MAX_VALUE) {
                    System.out.println("Ran out of memory, flushing current changes...");
                    archive.save(toolkit.progressBar);
                    toolkit.progressBar.setMaximum(pArchive.entries.size());
                    toolkit.progressBar.setValue(count + 1);
                }

                archive.add(pArchive.extract(entry));
                count++;
            }

            archive.save(toolkit.progressBar);

            toolkit.progressBar.setVisible(false);
            toolkit.progressBar.setMaximum(0); toolkit.progressBar.setValue(0);
        });

        JOptionPane.showMessageDialog(toolkit, "Please wait..");
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

        for (int i = 0; i < files.length; ++i) {
            File file = files[i];

            Mod mod = ModCallbacks.loadMod(file);
            if (mod != null) {

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
        }

        Toolkit.instance.getCurrentDB().shouldSave = true;
        Toolkit.instance.updateWorkspace();

        JTree tree = Toolkit.instance.getCurrentTree();
        TreePath[] treePath = tree.getSelectionPaths();

        FileModel m = (FileModel) tree.getModel();
        m.reload((FileNode) m.getRoot());

        tree.setSelectionPaths(treePath);
    }
}
