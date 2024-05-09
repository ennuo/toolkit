package toolkit.functions;

import cwlib.enums.CellGcmEnumForGtf;
import cwlib.enums.CostumePieceCategory;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.gl.jobs.DecalBaker;
import cwlib.io.exports.MeshExporter;
import cwlib.resources.*;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.decals.Decal;
import cwlib.structs.things.parts.PStickers;
import cwlib.types.SerializedResource;
import cwlib.types.data.GUID;
import cwlib.types.data.GatherData;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.ResourceInfo;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;
import cwlib.types.swing.FileNode;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import cwlib.util.Resources;
import cwlib.util.Strings;
import toolkit.utilities.FileChooser;
import toolkit.utilities.SlowOp;
import toolkit.windows.Toolkit;
import toolkit.windows.utilities.SlowOpGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ExportCallbacks
{
    public static void exportOBJ(int channel)
    {
        FileNode node = ResourceSystem.getSelected();
        ResourceInfo info = node.getEntry().getInfo();
        if (info == null || info.getType() != ResourceType.MESH || info.getResource() == null)
            return;

        String name = node.getName();
        File file = FileChooser.openFile(
            name.substring(0, name.length() - 4) + ".obj",
            "obj",
            true
        );

        if (file != null)
            MeshExporter.OBJ.export(file.getAbsolutePath(), info.getResource(), channel);
    }

    public static void exportDecalMaps(ActionEvent e)
    {
        FileNode node = ResourceSystem.getSelected();
        ResourceInfo info = node.getEntry().getInfo();
        if (info == null || info.getResource() == null) return;

        String folder = FileChooser.openDirectory();
        if (folder == null) return;

        RPlan plan = info.getResource();

        SlowOpGUI.performSlowOperation(Toolkit.INSTANCE, "Decal Exporter", "Exporting Decals",
            -1, new SlowOp()
            {
                @Override
                public int run(SlowOpGUI state)
                {

                    ArrayList<Thing> thingsWithDecals = new ArrayList<>();


                    try
                    {
                        Thing[] things = plan.getThings();
                        for (Thing thing : things)
                        {
                            if (thing == null || !thing.hasPart(Part.STICKERS))
                                continue;
                            thingsWithDecals.add(thing);
                        }
                    }
                    catch (Exception ex)
                    {
                        state.setErrorMessage("Failed to parse thing data!");
                        return -1;
                    }

                    state.setMaxProgress(thingsWithDecals.size());
                    int current = 0;
                    int max = thingsWithDecals.size();
                    for (Thing thing : thingsWithDecals)
                    {
                        if (state.wantQuit()) return -1;

                        PStickers stickers = thing.getPart(Part.STICKERS);

                        state.setMessage(String.format("(%d/%d) Exporting %d decals" +
                                                       " from thing %d",
                            current + 1, max, stickers.decals.length,
                            (long) thing.UID));
                        {
                            DecalBaker baker = new DecalBaker(stickers.decals);
                            byte[] pngData = baker.BakeToPNG();
                            File file = new File(folder, String.format("thing_" +
                                                                       "%d_decals" +
                                                                       ".png",
                                (long) thing.UID));
                            FileIO.write(pngData, file.getAbsolutePath());
                        }

                        for (int i = 0; i < stickers.costumeDecals.length; ++i)
                        {
                            Decal[] costumeDecals = stickers.costumeDecals[i];
                            String name = CostumePieceCategory.getNameFromIndex(i);

                            if (costumeDecals.length == 0) continue;
                            state.setMessage(String.format("(%d/%d) Exporting %d " +
                                                           "decals from thing " +
                                                           "%d's %s", current + 1
                                , max, costumeDecals.length,
                                (long) thing.UID, name));
                            DecalBaker baker = new DecalBaker(costumeDecals);
                            byte[] pngData = baker.BakeToPNG();
                            File file = new File(folder, String.format("thing_" +
                                                                       "%d_costumepiece_" +
                                                                       "%s_decals" +
                                                                       ".png",
                                (long) thing.UID, name));
                            FileIO.write(pngData, file.getAbsolutePath());
                        }

                        state.setProgress(current++);
                    }

                    return 0;
                }
            });
    }

    public static void exportGLB()
    {
        FileNode node = ResourceSystem.getSelected();
        ResourceInfo info = node.getEntry().getInfo();
        if (info == null || info.getResource() == null) return;

        RStaticMesh backdrop = null;
        RMesh mesh = null;

        switch (info.getType())
        {
            case STATIC_MESH:
                backdrop = info.getResource();
                break;
            case MESH:
                mesh = info.getResource();
                break;
            default:
                return;
        }

        File file = FileChooser.openFile(
            node.getName().substring(0, node.getName().length() - 4) + ".glb",
            "glb",
            true
        );

        if (file != null)
        {
            if (backdrop != null)
                MeshExporter.GLB.FromMesh(backdrop).export(file.getAbsolutePath());
            else
                MeshExporter.GLB.FromMesh(mesh).export(file.getAbsolutePath());
        }
    }

    public static void exportAnimation()
    {
        File file = FileChooser.openFile(
            ResourceSystem.getSelected().getName().substring(0,
                ResourceSystem.getSelected().getName().length() - 5) + ".glb",
            "glb",
            true
        );

        if (file == null) return;

        String input = JOptionPane.showInputDialog(Toolkit.INSTANCE, "Mesh", "g0");
        if (input == null) return;

        long guid = Strings.getLong(input);
        if (guid == -1)
        {
            System.err.println("You entered an invalid GUID!");
            return;
        }

        RMesh mesh = null;
        if (guid != 0)
        {
            FileEntry entry = ResourceSystem.get(guid);
            if (entry == null)
                ResourceSystem.println("Couldn't find model! Exporting without model!");
            else
            {
                byte[] data = ResourceSystem.extract(guid);
                if (data == null)
                    System.err.println("Couldn't find data for model in any archives.");
                else
                {
                    // old code for getting mesh name, might need it later?
                    // entry.getPath()).getFileName().toString().replaceFirst("[.][^
                    // .]+$", "")
                    try
                    {

                    }
                    catch (Exception ex)
                    {

                    }


                    mesh = new SerializedResource(data).loadResource(RMesh.class);
                }
            }

        }

        RAnimation animation = ResourceSystem.getSelected().getEntry().getInfo().getResource();
        MeshExporter.GLB.FromAnimation(animation, mesh).export(file.getAbsolutePath());
    }

    public static void exportTexture(String extension)
    {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        if (entry == null) return;

        File file = FileChooser.openFile(
            ResourceSystem.getSelected().getName().substring(0,
                ResourceSystem.getSelected().getName().length() - 4) + "." + extension,
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
        catch (IOException ex)
        {
            ResourceSystem.println("There was an error exporting the image.");
            return;
        }

        ResourceSystem.println("Successfully exported selected textures!");
    }

    public static void exportDDS()
    {
        FileEntry selected = ResourceSystem.getSelected().getEntry();

        File file = FileChooser.openFile(
            Strings.setExtension(selected.getName(), "dds"),
            "dds",
            true
        );

        if (file == null) return;

        ResourceInfo info = selected.getInfo();
        if (info == null) return;

        RTexture texture = selected.getInfo().getResource();
        if (texture == null) return;

        byte[] output = texture.getDDSFileData();

        // Each face of cubemaps are aligned to 128 byte boundaries
        CellGcmTexture gcm = texture.getInfo();
        if (gcm != null && gcm.isCubemap())
        {
            byte[] data = Arrays.copyOfRange(output, 0x80, output.length);
            output = null;

            int depth = 16;
            if (gcm.getFormat() == CellGcmEnumForGtf.DXT1)
                depth = 8;

            int offset = 0;
            for (int n = 0; n < 6; ++n)
            {
                int w = gcm.getWidth();
                int h = gcm.getHeight();
                int m = 0;

                while (m < gcm.getMipCount())
                {
                    int size = ((w + 3) / 4) * ((h + 3) / 4) * depth;

                    byte[] buf = Arrays.copyOfRange(data, offset, offset + size);
                    if (output == null) output = buf;
                    else
                        output = Bytes.combine(output, buf);

                    offset += size;

                    w >>>= 1;
                    h >>>= 1;

                    if (w == 0 && h == 0) break;
                    if (w == 0) w = 1;
                    if (h == 0) h = 1;
                    ++m;
                }

                if (((offset % 128) != 0))
                {
                    offset += (128 - (offset % 128));
                }
            }

            output = Bytes.combine(texture.getDDSHeader(), output);
        }

        FileIO.write(output, file.getAbsolutePath());
    }

    public static void exportFont(ActionEvent e)
    {
        FileEntry selected = ResourceSystem.getSelected().getEntry();

        File file = FileChooser.openFile(
            Strings.setExtension(selected.getName(), "png"),
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

    public static void exportTranslations()
    {
        byte[] data = ResourceSystem.extract(ResourceSystem.getSelected().getEntry());
        if (data == null) return;
        RTranslationTable table = new RTranslationTable(data);

        FileNode selected = ResourceSystem.getSelected();
        File file = FileChooser.openFile(
            Strings.setExtension(selected.getName(), "json"),
            "json",
            true
        );

        if (file == null) return;

        table.export(file.getAbsolutePath());
    }

    public static void exportMod(boolean hashinate)
    {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        String name = Strings.getWithoutExtension(entry.getName());

//        RPlan item = entry.getInfo().getResource();
//        if (item == null) return;

        File file = FileChooser.openFile(name + ".mod", "mod", true);
        if (file == null) return;

        byte[] data = ResourceSystem.extract(entry);
        Mod mod = new Mod();

        GatherData[] gatherables = null;
        if (hashinate)
            gatherables = Resources.hashinate(data,
                new ResourceDescriptor((GUID) entry.getKey(),
                    ResourceType.PLAN));
        else
            gatherables = Resources.collect(data,
                new ResourceDescriptor((GUID) entry.getKey(),
                    ResourceType.PLAN));

        for (GatherData gatherable : gatherables)
        {
            if (mod.get(gatherable.getGUID()) == null)
                mod.add(gatherable.getPath(), gatherable.getData(), gatherable.getGUID());
        }

        mod.getConfig().title = name;

        // TODO: Re-add merging mods

        // if (file.exists()) {
        //     int result = JOptionPane.showConfirmDialog(null, "This mod already exists, do you
        //     want to merge them?", "Existing mod!", JOptionPane.YES_NO_CANCEL_OPTION);
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
