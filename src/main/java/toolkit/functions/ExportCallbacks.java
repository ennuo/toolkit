package toolkit.functions;

import cwlib.resources.RAnimation;
import cwlib.io.streams.MemoryInputStream;
import cwlib.util.FileIO;
import cwlib.types.Resource;
import cwlib.types.data.ResourceInfo;
import cwlib.resources.RMesh;
import cwlib.resources.RTexture;
import cwlib.resources.RTranslationTable;
import cwlib.enums.ResourceType;
import cwlib.io.exports.MeshIO;
import cwlib.resources.RPlan;
import cwlib.resources.RStaticMesh;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;
import cwlib.types.swing.FileNode;
import cwlib.util.Bytes;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.utilities.ResourceSystem;
import toolkit.windows.Toolkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class ExportCallbacks {
    public static void exportOBJ(int channel) {
        FileNode node = ResourceSystem.getSelected();
        ResourceInfo info = node.getEntry().getInfo();
        if (info == null || info.getType() != ResourceType.MESH || info.getResource() == null) return;
        
        String name = node.getName();
        File file = FileChooser.openFile(
            name.substring(0, name.length() - 4) + ".obj",
            "obj",
            true
        );

        if (file != null)
            MeshIO.OBJ.export(file.getAbsolutePath(), info.getResource(), channel);
    }
    
    public static void exportGLB() {
        FileNode node = ResourceSystem.getSelected();
        ResourceInfo info = node.getEntry().getInfo();
        if (info == null || info.getResource() == null) return;

        RStaticMesh backdrop = null;
        RMesh mesh = null;

        switch (info.getType()) {
            case STATIC_MESH: backdrop = info.getResource(); break;
            case MESH: mesh = info.getResource(); break;
            default: return;
        }
        
        File file = FileChooser.openFile(
            node.getName().substring(0, node.getName().length() - 4) + ".glb",
            "glb",
            true
        );
        
        if (file != null) {
            if (backdrop != null) 
                MeshIO.GLB.FromMesh(backdrop).export(file.getAbsolutePath());
            else
                MeshIO.GLB.FromMesh(mesh).export(file.getAbsolutePath());
        }
    }
    
    public static void exportAnimation() {
        File file = FileChooser.openFile(
            ResourceSystem.getSelected().getName().substring(0, ResourceSystem.getSelected().getName().length() - 5) + ".glb",
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
            FileEntry entry = ResourceSystem.get(integer);
            if (entry == null) 
                System.err.println("Couldn't find model! Exporting without model!");
            else {
                byte[] data = ResourceSystem.extract(integer);
                if (data == null) System.err.println("Couldn't find data for model in any archives.");
                else
                    mesh = new RMesh(Paths.get(entry.getPath()).getFileName().toString().replaceFirst("[.][^.]+$", ""), new Resource(data));
            }
            
        }
        
        RAnimation animation = ResourceSystem.getSelected().getEntry().getResource("animation");
        MeshIO.GLB.FromAnimation(animation, mesh).export(file.getAbsolutePath());
    }

    public static void exportTexture(String extension) {
        File file = FileChooser.openFile(
            ResourceSystem.getSelected().getName().substring(0, ResourceSystem.getSelected().getName().length() - 4) + "." + extension,
            extension,
            true
        );

        if (file == null) return;

        RTexture texture = ResourceSystem.getSelected().getEntry().getResource("texture");
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
            ResourceSystem.getSelected().getName().substring(0, ResourceSystem.getSelected().getName().length() - 4) + "dds",
            "DDS",
            true
        );

        if (file == null) return;

        RTexture texture = ResourceSystem.getSelected().getEntry().getResource("texture");
        if (texture == null || !texture.parsed) return;

        FileIO.write(texture.uncompressedData, file.getAbsolutePath());
    }

    public static void exportTranslations() {
        byte[] data = ResourceSystem.extract(ResourceSystem.getSelected().getEntry());
        if (data == null) return;
        RTranslationTable table = new RTranslationTable(data);

        String header = ResourceSystem.getSelected().getName();
        File file = FileChooser.openFile(header.substring(0, header.length() - 5), ".json", true);
        if (file == null) return;

        table.export(file.getAbsolutePath());
    }

    public static void exportMod(boolean hashinate) {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        String name = Paths.get(ResourceSystem.getSelected().getEntry().path).getFileName().toString();
        RPlan item = ResourceSystem.getSelected().getEntry().getResource("item");
        if (item != null)
            name = name.substring(0, name.length() - 5);
        else name = name.substring(0, name.length() - 4);

        File file = FileChooser.openFile(name + ".mod", "mod", true);
        if (file == null) return;

        Resource resource = new Resource(ResourceSystem.extract(entry));
        Mod mod = new Mod();
        if (hashinate)
            Bytes.hashinate(mod, resource, entry);
        else Bytes.getAllDependencies(mod, resource, entry);;

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
