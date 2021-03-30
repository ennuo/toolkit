package ennuo.toolkit.functions;

import ennuo.craftworld.memory.Bytes;
import ennuo.craftworld.memory.FileIO;
import ennuo.craftworld.memory.Output;
import ennuo.craftworld.memory.Resource;
import ennuo.craftworld.resources.structs.Slot;
import ennuo.craftworld.swing.FileModel;
import ennuo.craftworld.swing.FileNode;
import ennuo.craftworld.things.InventoryMetadata;
import ennuo.craftworld.types.BigProfile;
import ennuo.craftworld.types.FileArchive;
import ennuo.craftworld.types.FileDB;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.Mod;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.ModEditor;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class UtilityCallbacks {
    public static void newMod() {
        File file = Toolkit.instance.fileChooser.openFile("template.mod", "mod", "Mod", true);
        if (file == null) return;
        if (Toolkit.instance.confirmOverwrite(file)) {
            Mod mod = new Mod();
            new ModEditor(mod, true).setVisible(true);
            mod.save(file.getAbsolutePath());
        }
    }
        
    public static void decompressResource() {                                                   
        File file = Toolkit.instance.fileChooser.openFile("data.bin", "", "", false);
        if (file == null) return;

        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data == null) return;

        Resource resource = new Resource(data);
        byte[] decompressed = resource.decompress();

        if (decompressed == null) {
            System.err.println("Failed to decompress resource.");
            return;
        }

        File out = Toolkit.instance.fileChooser.openFile(file.getName() + ".dec", "", "", true);
        if (out != null)
            FileIO.write(decompressed, out.getAbsolutePath());
    }
    
    public static void mergeFileArchives() {         
        Toolkit toolkit = Toolkit.instance;
        File base = toolkit.fileChooser.openFile("base.farc", "farc", "Base FileArchive", false);
        if (base == null) return;
        FileArchive archive;
        int index = toolkit.isArchiveLoaded(base);
        if (index != -1) archive = Globals.archives.get(index);
        else archive = new FileArchive(base);

        if (archive == null) {
            System.err.println("Base FileArchive is null! Aborting!");
            return;
        }

        File patch = toolkit.fileChooser.openFile("patch.farc", "farc", "Patch FileArchive", false);
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
        File base = Toolkit.instance.fileChooser.openFile("blurayguids.map", "map", "FileDB", false);
        if (base == null) return;

        FileDB baseDB = new FileDB(base);
        if (baseDB == null) {
            System.err.println("Why is the FileDB null?!");
            return;
        }

        File update = Toolkit.instance.fileChooser.openFile("blurayguids.map", "map", "FileDB", false);
        if (update == null) return;

        FileDB updateDB = new FileDB(update);
        if (updateDB == null) {
            System.err.println("Why is the FileDB null?!");
            return;
        }

        Output output = new Output(updateDB.entryCount * 0x100);
        for (FileEntry entry: updateDB.entries) {
            FileEntry baseEntry = baseDB.find(entry.GUID);
            if (baseEntry == null)
                output.string("[+] " + entry.path + " " + Bytes.toHex(entry.size) + " " + Bytes.toHex(entry.hash) + " " + Bytes.toHex(entry.GUID) + '\n');
            else if (baseEntry.size != entry.size) {
                output.string("[U] " + entry.path + " " + Bytes.toHex(baseEntry.size) + " -> " + Bytes.toHex(entry.size) + " " + Bytes.toHex(baseEntry.hash) + " -> " + Bytes.toHex(entry.hash) + " " + Bytes.toHex(entry.GUID) + '\n');
            }

        }
        output.shrinkToFit();

        File out = Toolkit.instance.fileChooser.openFile("diff.txt", ".txt", "Text Document", true);
        if (out == null) return;

        FileIO.write(output.buffer, out.getAbsolutePath());
    }
    
    public static void installMod() {                                                  
        File[] files = Toolkit.instance.fileChooser.openFiles("mod", "File Mod");
        if (files == null) return;

        for (int i = 0; i < files.length; ++i) {
            File file = files[i];

            Mod mod = ModCallbacks.loadMod(file);
            if (mod != null) {

                if (Globals.currentWorkspace == Globals.WorkspaceType.PROFILE) {
                    BigProfile profile = (BigProfile) Toolkit.instance.getCurrentDB();
                    for (FileEntry entry: mod.entries)
                        profile.add(entry.data, false);
                    for (InventoryMetadata item: mod.items)
                        profile.addItem(item.resource, item);
                    for (Slot slot: mod.slots)
                        profile.addSlot(slot);
                } else if (Globals.currentWorkspace == Globals.WorkspaceType.MAP) {
                    if (mod.entries.size() == 0) return;
                    FileDB db = (FileDB) Toolkit.instance.getCurrentDB();
                    FileArchive[] archives = Toolkit.instance.getSelectedArchives();
                    if (archives == null) return;
                    for (FileEntry entry: mod.entries) {
                        if (db.add(entry))
                            db.addNode(entry);
                        Globals.addFile(entry.data, archives);
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
    
    public static void encodeInteger() {                                              
        String number = JOptionPane.showInputDialog(Toolkit.instance, "Integer", "");
        if (number == null) return;

        long integer;
        if (number.toLowerCase().startsWith("0x"))
            integer = Long.parseLong(number.substring(2), 16);
        else if (number.startsWith("g"))
            integer = Long.parseLong(number.substring(1));
        else
            integer = Long.parseLong(number);

        Output output = new Output(12, 0xFFFFFFFF);
        output.varint(integer);
        output.shrinkToFit();

        System.out.println("0x" + Bytes.toHex(integer) + " (" + integer + ")" + " -> " + "0x" + Bytes.toHex(output.buffer));
    }   
}
