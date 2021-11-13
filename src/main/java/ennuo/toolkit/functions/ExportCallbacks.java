package ennuo.toolkit.functions;

import ennuo.craftworld.serializer.Data;
import ennuo.craftworld.resources.io.FileIO;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.resources.Mesh;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.TranslationTable;
import ennuo.craftworld.resources.io.MeshIO;
import ennuo.craftworld.resources.Plan;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.mods.Mod;
import ennuo.craftworld.utilities.Bytes;
import ennuo.toolkit.utilities.Globals;
import ennuo.toolkit.windows.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class ExportCallbacks {
    public static void exportOBJ(int channel) {
        File file = Toolkit.instance.fileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + ".obj",
            "obj",
            "Wavefront Object (.OBJ)",
            true
        );

        if (file != null)
            MeshIO.OBJ.export(file.getAbsolutePath(), Globals.lastSelected.entry.getResource("mesh"), channel);
    }
    
    public static void exportGLB() {
        File file = Toolkit.instance.fileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + ".glb",
            "glb",
            "glTF Binary (.GLB)",
            true
        );
       
        if (file != null)
            MeshIO.GLB.FromMesh(Globals.lastSelected.entry.getResource("mesh")).export(file.getAbsolutePath());
    }
    
    public static void exportAnimation() {
        File file = Toolkit.instance.fileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 5) + ".glb",
            "glb",
            "glTF Binary (.GLB)",
            true
        );
       
        String GUID = JOptionPane.showInputDialog(Toolkit.instance, "Mesh GUID", "g0");
        if (GUID == null) return;
        GUID = GUID.replaceAll("\\s", "");
        
        long integer;
        try {
            if (GUID.toLowerCase().startsWith("0x"))
                integer = Long.parseLong(GUID.substring(2), 16);
            else if (GUID.toLowerCase().startsWith("g"))
                integer = Long.parseLong(GUID.substring(1));
            else
                integer = Long.parseLong(GUID);
        } catch (NumberFormatException e) {
            System.err.println("You inputted an invalid GUID!");
            return;
        }
        
        FileEntry entry = Globals.findEntry(integer);
        
        Mesh mesh = null;
        if (entry == null) {
            System.err.println("Couldn't find model!");
        } else {
            byte[] data = Globals.extractFile(integer);
            if (data == null) System.err.println("Couldn't find data for model in any archives.");
            else
                mesh = new Mesh(Paths.get(entry.path).getFileName().toString().replaceFirst("[.][^.]+$", ""), new Resource(data));
        }
        
        if (file != null)
            MeshIO.GLB.FromAnimation(Globals.lastSelected.entry.getResource("animation"), mesh).export(file.getAbsolutePath());
    }

    public static void exportTexture(String extension) {
        File file = Toolkit.instance.fileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + "." + extension,
            extension,
            "Image",
            true
        );

        if (file == null) return;

        Texture texture = Globals.lastSelected.entry.getResource("texture");
        if (texture == null || !texture.parsed) return;

        try {
            ImageIO.write(texture.getImage(), extension, file);
        } catch (IOException ex) {
            System.err.println("There was an error exporting the image.");
            return;
        }

        System.out.println("Successfully exported textures!");
    }

    public static void exportDDS() {
        File file = Toolkit.instance.fileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + "dds",
            "DDS",
            "Image",
            true
        );

        if (file == null) return;

        Texture texture = Globals.lastSelected.entry.getResource("texture");
        if (texture == null || !texture.parsed) return;

        FileIO.write(texture.data, file.getAbsolutePath());
    }

    public static void exportTranslations() {
        TranslationTable table = new TranslationTable(new Data(Globals.lastSelected.entry.data));
        byte[] data = table.export();
        File file = Toolkit.instance.fileChooser.openFile(
        Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 5) + ".txt",
            "txt",
            "Text Document",
            true
        );
        if (file == null) return;
        FileIO.write(data, file.getAbsolutePath());
    }

    public static void exportMod(boolean hashinate) {
        FileEntry entry = Globals.lastSelected.entry;
        String name = Paths.get(Globals.lastSelected.entry.path).getFileName().toString();
        Plan item = Globals.lastSelected.entry.getResource("item");
        if (item != null)
            name = name.substring(0, name.length() - 5);
        else name = name.substring(0, name.length() - 4);

        File file = Toolkit.instance.fileChooser.openFile(name + ".mod", "mod", "Mod", true);
        if (file == null) return;

        Resource resource = new Resource(Globals.extractFile(entry.SHA1));
        Mod mod = new Mod();
        if (hashinate)
            Bytes.hashinate(mod, resource, entry);
        else Bytes.recurse(mod, resource, entry);;

        mod.config.title = name;
        
        if (file.exists()) {
            int result = JOptionPane.showConfirmDialog(null, "This mod already exists, do you want to merge them?", "Existing mod!", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                Mod oldMod = ModCallbacks.loadMod(file);
                if (oldMod != null) {
                    for (FileEntry e: oldMod.entries)
                        mod.add(e.path, e.data, e.GUID);
                }
            } else if (result != JOptionPane.NO_OPTION) return;
        }

        mod.save(file.getAbsolutePath());
    }
}
