package toolkit.functions;

import cwlib.resources.RAnimation;
import cwlib.resources.RFontFace;
import cwlib.util.FileIO;
import cwlib.util.Resources;
import cwlib.types.Resource;
import cwlib.types.data.GUID;
import cwlib.types.data.GatherData;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.ResourceInfo;
import cwlib.resources.RMesh;
import cwlib.resources.RTexture;
import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;
import cwlib.enums.ResourceType;
import cwlib.io.exports.MeshExporter;
import cwlib.resources.RStaticMesh;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;
import cwlib.types.swing.FileNode;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.windows.Toolkit;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
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
            MeshExporter.OBJ.export(file.getAbsolutePath(), info.getResource(), channel);
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
                MeshExporter.GLB.FromMesh(backdrop).export(file.getAbsolutePath());
            else
                MeshExporter.GLB.FromMesh(mesh).export(file.getAbsolutePath());
        }
    }
    
    public static void exportAnimation() {
        File file = FileChooser.openFile(
            ResourceSystem.getSelected().getName().substring(0, ResourceSystem.getSelected().getName().length() - 5) + ".glb",
            "glb",
            true
        );
        
        if (file == null) return;
        
        String input = JOptionPane.showInputDialog(Toolkit.INSTANCE, "Mesh", "g0");
        if (input == null) return;
        
        long guid = Strings.getLong(input);
        if (guid == -1) {
            System.err.println("You entered an invalid GUID!");
            return;
        }
        
        RMesh mesh = null;
        if (guid != 0) {
            FileEntry entry = ResourceSystem.get(guid);
            if (entry == null) 
                ResourceSystem.println("Couldn't find model! Exporting without model!");
            else {
                byte[] data = ResourceSystem.extract(guid);
                if (data == null) System.err.println("Couldn't find data for model in any archives.");
                else {
                    // old code for getting mesh name, might need it later?
                    // entry.getPath()).getFileName().toString().replaceFirst("[.][^.]+$", "")
                    try {

                    } catch (Exception ex) {
                        
                    }


                    mesh = new Resource(data).loadResource(RMesh.class);
                }
            }
            
        }
        
        RAnimation animation = ResourceSystem.getSelected().getEntry().getInfo().getResource();
        MeshExporter.GLB.FromAnimation(animation, mesh).export(file.getAbsolutePath());
    }

    public static void exportTexture(String extension) {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        if (entry == null) return;

        File file = FileChooser.openFile(
            ResourceSystem.getSelected().getName().substring(0, ResourceSystem.getSelected().getName().length() - 4) + "." + extension,
            extension,
            true
        );

        if (file == null) return;

        ResourceInfo info = entry.getInfo();
        if (info == null || (info.getType() != ResourceType.TEXTURE && info.getType() != ResourceType.GTF_TEXTURE)) 
            return;

        RTexture texture = info.getResource();
        if (texture == null) return;

        try { ImageIO.write(texture.getImage(), extension, file); } 
        catch (IOException ex) {
            ResourceSystem.println("There was an error exporting the image.");
            return;
        }

        ResourceSystem.println("Successfully exported selected textures!");
    }

    public static void exportDDS() {
        FileEntry selected = ResourceSystem.getSelected().getEntry();

        File file = FileChooser.openFile(
            selected.getName().substring(0, selected.getName().lastIndexOf(".")) + ".dds",
            "dds",
            true
        );

        if (file == null) return;

        ResourceInfo info = selected.getInfo();
        if (info == null) return;

        RTexture texture = selected.getInfo().getResource();
        if (texture == null) return;
        
        FileIO.write(texture.getData(), file.getAbsolutePath());
    }

    public static void exportFont(ActionEvent e) {
        FileEntry selected = ResourceSystem.getSelected().getEntry();

        File file = FileChooser.openFile(
            selected.getName().substring(0, selected.getName().lastIndexOf(".")) + ".png",
            "png",
            true
        );

        if (file == null) return;

        ResourceInfo info = selected.getInfo();
        if (info == null) return;

        RFontFace font = selected.getInfo().getResource();
        if (font == null) return;

        font.export(file.getAbsolutePath());
    }

    public static void exportTranslations() {
        byte[] data = ResourceSystem.extract(ResourceSystem.getSelected().getEntry());
        if (data == null) return;
        RTranslationTable table = new RTranslationTable(data);

        FileNode selected = ResourceSystem.getSelected();
        File file = FileChooser.openFile(
            selected.getName().substring(0, selected.getName().lastIndexOf(".")) + ".json",
            "json",
            true
        );
        
        if (file == null) return;

        table.export(file.getAbsolutePath());
    }

    public static void exportMod(boolean hashinate) {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        String name = entry.getName();
        name = name.substring(0, name.lastIndexOf("."));

//        RPlan item = entry.getInfo().getResource();
//        if (item == null) return;

        File file = FileChooser.openFile(name + ".mod", "mod", true);
        if (file == null) return;

        byte[] data = ResourceSystem.extract(entry);
        Mod mod = new Mod();

        GatherData[] gatherables = null;
        if (hashinate)
            gatherables = Resources.hashinate(data, new ResourceDescriptor((GUID) entry.getKey(), ResourceType.PLAN));
        else
            gatherables = Resources.collect(data, new ResourceDescriptor((GUID) entry.getKey(), ResourceType.PLAN));

        for (GatherData gatherable : gatherables) {
            if (mod.get(gatherable.getGUID()) == null)
                mod.add(gatherable.getPath(), gatherable.getData(), gatherable.getGUID());
        }

        mod.getConfig().title = name;
        
        // TODO: Re-add merging mods
        
        // if (file.exists()) {
        //     int result = JOptionPane.showConfirmDialog(null, "This mod already exists, do you want to merge them?", "Existing mod!", JOptionPane.YES_NO_CANCEL_OPTION);
        //     if (result == JOptionPane.YES_OPTION) {
        //         Mod oldMod = ModCallbacks.loadMod(file);
        //         if (oldMod != null) {
        //             for (FileDBRow row: oldMod)
        //                 mod.add(e.path, e.data, e.GUID);
        //         }
        //     } else if (result != JOptionPane.NO_OPTION) return;
        // }

        mod.save(file);
    }
}
