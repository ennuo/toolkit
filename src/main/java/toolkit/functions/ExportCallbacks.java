package toolkit.functions;

import cwlib.resources.RAnimation;
import cwlib.io.streams.MemoryInputStream;
import cwlib.util.FileIO;
import cwlib.types.Resource;
import cwlib.resources.RMesh;
import cwlib.resources.RTexture;
import cwlib.resources.RTranslationTable;
import cwlib.io.exports.MeshIO;
import cwlib.resources.RPlan;
import cwlib.resources.RStaticMesh;
import cwlib.types.FileEntry;
import cwlib.types.mods.Mod;
import cwlib.util.Bytes;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.utilities.Globals;
import toolkit.windows.Toolkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class ExportCallbacks {
    public static void exportOBJ(int channel) {
        File file = FileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + ".obj",
            "obj",
            true
        );

        if (file != null)
            MeshIO.OBJ.export(file.getAbsolutePath(), Globals.lastSelected.entry.getResource("mesh"), channel);
    }
    
    public static void exportGLB() {
        File file = FileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + ".glb",
            "glb",
            true
        );
       
        RStaticMesh staticMesh = Globals.lastSelected.entry.getResource("staticMesh");
        RMesh mesh = Globals.lastSelected.entry.getResource("mesh");
        
        if (file != null) {
            if (staticMesh != null) 
                MeshIO.GLB.FromMesh(staticMesh).export(file.getAbsolutePath());
            else if (mesh != null)
                MeshIO.GLB.FromMesh(mesh).export(file.getAbsolutePath());
            else
                System.err.println("Mesh data was missing!");
            
        }
    }
    
    public static void exportAnimation() {
        File file = FileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 5) + ".glb",
            "glb",
            true
        );
        
        if (file == null) return;
       
        String GUID = JOptionPane.showInputDialog(Toolkit.instance, "Mesh GUID", "g0");
        if (GUID == null) return;
        GUID = GUID.replaceAll("\\s", "");
        
        long integer = Strings.getLong(GUID);
        if (integer == -1) {
            System.err.println("You entered an invalid GUID!");
            return;
        }
        
        RMesh mesh = null;
        if (integer != 0) {
            FileEntry entry = Globals.findEntry(integer);
            if (entry == null) 
                System.err.println("Couldn't find model! Exporting without model!");
            else {
                byte[] data = Globals.extractFile(integer);
                if (data == null) System.err.println("Couldn't find data for model in any archives.");
                else
                    mesh = new RMesh(Paths.get(entry.path).getFileName().toString().replaceFirst("[.][^.]+$", ""), new Resource(data));
            }
            
        }
        
        RAnimation animation = Globals.lastSelected.entry.getResource("animation");
        MeshIO.GLB.FromAnimation(animation, mesh).export(file.getAbsolutePath());
    }

    public static void exportTexture(String extension) {
        File file = FileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + "." + extension,
            extension,
            true
        );

        if (file == null) return;

        RTexture texture = Globals.lastSelected.entry.getResource("texture");
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
        File file = FileChooser.openFile(
            Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 4) + "dds",
            "DDS",
            true
        );

        if (file == null) return;

        RTexture texture = Globals.lastSelected.entry.getResource("texture");
        if (texture == null || !texture.parsed) return;

        FileIO.write(texture.data, file.getAbsolutePath());
    }

    public static void exportTranslations() {
        RTranslationTable table = new RTranslationTable(new MemoryInputStream(Globals.lastSelected.entry.data));
        byte[] data = table.export();
        File file = FileChooser.openFile(
        Globals.lastSelected.header.substring(0, Globals.lastSelected.header.length() - 5) + ".txt",
            "txt",
            true
        );
        if (file == null) return;
        FileIO.write(data, file.getAbsolutePath());
    }

    public static void exportMod(boolean hashinate) {
        FileEntry entry = Globals.lastSelected.entry;
        String name = Paths.get(Globals.lastSelected.entry.path).getFileName().toString();
        RPlan item = Globals.lastSelected.entry.getResource("item");
        if (item != null)
            name = name.substring(0, name.length() - 5);
        else name = name.substring(0, name.length() - 4);

        File file = FileChooser.openFile(name + ".mod", "mod", true);
        if (file == null) return;

        Resource resource = new Resource(Globals.extractFile(entry.hash));
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
