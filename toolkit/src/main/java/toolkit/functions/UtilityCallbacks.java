package toolkit.functions;

import cwlib.enums.DatabaseType;
import cwlib.enums.GameShader;
import cwlib.enums.GameTextureType;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.enums.Revisions;
import cwlib.enums.SerializationType;
import cwlib.io.serializer.SerializationData;
import cwlib.resources.RGfxMaterial;
import cwlib.resources.RLevel;
import cwlib.resources.RPalette;
import cwlib.resources.RPlan;
import cwlib.resources.RTexture;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.structs.things.parts.PWorld;
import cwlib.types.SerializedResource;
import cwlib.types.archives.Fart;
import cwlib.types.archives.Fat;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.mods.Mod;
import cwlib.types.swing.FileData;
import cwlib.util.Bytes;
import cwlib.util.DDS;
import cwlib.util.FileIO;
import cwlib.util.gfx.CgAssembler;
import cwlib.util.gfx.GfxAssembler;
import executables.gfx.dialogues.ErrorDialogue;
import scelib.Gxm;
import toolkit.utilities.FileChooser;
import toolkit.utilities.SlowOp;
import toolkit.windows.Toolkit;
import toolkit.windows.managers.ModManager;
import toolkit.windows.utilities.SlowOpGUI;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class UtilityCallbacks
{
    public static void convertMaterial(GameShader target)
    {
        var entry = ResourceSystem.getSelected().getEntry();
        var info = entry.getInfo();
        if (info == null || info.getType() != ResourceType.GFX_MATERIAL)
            return;

        RGfxMaterial material = info.getResource();
        var source = GameShader.fromMaterial(material, info.getRevision());

        if (source == target) return;

        material.shaders = new byte[target.getShaderCount()][];
        try
        {
            material.useVitaShaderSource = target == GameShader.VITA;
            String cgShaderSource = GfxAssembler.generateShaderSource(material, -1, false);
            CgAssembler.compile(cgShaderSource, material, target);
        }
        catch (Exception ex)
        {
            new ErrorDialogue(Toolkit.INSTANCE, true, "An error occurred while compiling BRDF shader.",
                ex.getMessage());
            return;
        }

        var revision = target.getRevision();
        if (target == GameShader.LBP1 && material.shouldSaveCustomData())
            revision.setCustomBranchDescription(Revisions.ALEAR_BR1, Revisions.ALEAR_BR1_MAX);

        byte[] resource = SerializedResource.compress(material.build(
            revision,
            revision.getDefaultCompressionFlags()
        ));

        ResourceSystem.replace(entry, resource);
    }
    
    public static void convertTextureType(GameTextureType target)
    {
        var entry = ResourceSystem.getSelected().getEntry();
        var info = entry.getInfo();
        if (info == null || info.getTextureType() == GameTextureType.INVALID || info.getResource() == null)
            return;


        GameTextureType source = info.getTextureType();
        if (source == target) return;

        RTexture texture = info.getResource();
        byte[] textureData = texture.getDDSFileData();
        
        var gcm = texture.getInfo();
        if (source == GameTextureType.COMPRESSED)
            gcm = new CellGcmTexture(textureData, texture.noSRGB);

        if (target == GameTextureType.PNG || target == GameTextureType.JPEG)
        {
            ResourceSystem.println("Not supported!");
            return;
        }

        if (target == GameTextureType.COMPRESSED)
        {
            // LBP1 compressed textures store BUMP or VLME at the end of the DDS file
            // rather than any flags.
            textureData = Bytes.combine(textureData, (gcm.isBumpTexture() ? "BUMP" : "\0\0\0\0").getBytes());
            textureData = SerializedResource.compress(new SerializationData(textureData));
        }
        else if (target != GameTextureType.DDS)
        {
            if (target == GameTextureType.GXT)
            {
                textureData = Gxm.convert(textureData);
                textureData = Arrays.copyOfRange(textureData, 0x40, textureData.length);
                gcm.setMethod(SerializationType.GTF_SWIZZLED);
            }
            else 
            {
                gcm.setMethod(SerializationType.COMPRESSED_TEXTURE);
                textureData = Arrays.copyOfRange(textureData, 0x80, textureData.length);
                if (!gcm.getFormat().isDXT())
                    textureData = DDS.convertSwizzleGtf(gcm, textureData, false);
            }

            textureData = SerializedResource.compress(new SerializationData(textureData, gcm));
        }

        ResourceSystem.replace(entry, textureData);
    }

    public static void paletteToLevel(ActionEvent event)
    {
        var entry = ResourceSystem.getSelected().getEntry();
        var info = entry.getInfo();
        if (info == null || info.getType() != ResourceType.PALETTE || info.getResource() == null)
            return;

        var revision = info.getRevision();
        RPalette palette = info.getResource();

        var level = new RLevel();
        var world = level.worldThing.<PWorld>getPart(Part.WORLD);

        for (var descriptor : palette.planList)
        {
            byte[] planData = ResourceSystem.extract(descriptor);
            if (planData == null)
            {
                System.out.printf("Skipping %s because resource was not found in caches\n", descriptor);
                continue;
            }

            var plan = new SerializedResource(planData).loadResource(RPlan.class);
            for (var thing : plan.getThings())
                world.things.add(thing);
        }

        world.things.removeIf(thing -> thing == null);
        world.thingUIDCounter = world.things.stream().max(Comparator.comparingInt(x -> x.UID)).get().UID;
        
        var set = new HashSet<Integer>();
        for (var thing : world.things)
        {
            if (!set.add(thing.UID))
                thing.UID = ++world.thingUIDCounter;
        }
        
        world.things.sort((a, z) -> a.UID - z.UID);
        
        byte[] fileData = SerializedResource.compress(level.build(revision, revision.getDefaultCompressionFlags()));
        
        File file = FileChooser.openFile(ResourceSystem.getSelected().getName().replace(".pal", ".bin"), ".bin", true);
        if (file == null) return;
        FileIO.write(fileData, file.getAbsolutePath());
    }

    public static void newMod()
    {
        File file = FileChooser.openFile("template.mod", "mod", true);
        if (file == null) return;
        if (Toolkit.INSTANCE.confirmOverwrite(file))
        {
            Mod mod = new Mod();
            new ModManager(mod, true).setVisible(true);
            mod.save(file);
            mod = ModCallbacks.loadMod(file);
            if (mod != null)
            {
                Toolkit.INSTANCE.addTab(mod);
                Toolkit.INSTANCE.updateWorkspace();
            }
        }
    }

    public static void decompressResource()
    {
        File file = FileChooser.openFile("data.bin", null, false);
        if (file == null) return;

        byte[] data = FileIO.read(file.getAbsolutePath());
        if (data == null)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Failed to read file, is it " +
                                                            "protected?", "Decompressor",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        SerializedResource resource = null;
        try { resource = new SerializedResource(data); }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(Toolkit.INSTANCE, "Failed to deserialize resource!",
                "Decompressor", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File out = FileChooser.openFile(file.getName() + ".dec", null, true);
        if (out != null)
            FileIO.write(resource.getStream().getBuffer(), out.getAbsolutePath());
    }

    public static void mergeFileArchives()
    {
        File file = FileChooser.openFile("base.farc", "farc", false);
        if (file == null) return;

        Fart cache;
        int index = Toolkit.INSTANCE.isArchiveLoaded(file);
        if (index != -1) cache = ResourceSystem.getArchives().get(index);
        else cache = new FileArchive(file);

        file = FileChooser.openFile("patch.farc", "farc", false);
        if (file == null) return;

        FileArchive patch = new FileArchive(file);

        // Flush archives at 256MBs, maybe make this configurable, or auto-calculate some
        // appropriate value based on lower end systems.
        final int CACHE_SIZE = 268_435_456;

        SlowOpGUI.performSlowOperation(Toolkit.INSTANCE, "Archive Merger", "Merging Archives",
            patch.getEntryCount(), new SlowOp()
            {
                @Override
                public int run(SlowOpGUI state)
                {
                    int current = 0;
                    for (Fat fat : patch)
                    {
                        if (state.wantQuit()) return -1;

                        // Save if we have too much stored in memory currently.
                        if (cache.getQueueSize() >= CACHE_SIZE)
                            cache.save();

                        SHA1 sha1 = fat.getSHA1();
                        if (!cache.exists(sha1))
                            cache.add(patch.extract(sha1));

                        state.setProgress(current++);
                    }

                    cache.save();

                    return 0;
                }
            });
    }

    public static void generateFileDBDiff()
    {
        File baseFile = FileChooser.openFile("blurayguids.map", "map", false);
        if (baseFile == null) return;

        FileDB base = null;
        try { base = new FileDB(baseFile); }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(
                Toolkit.INSTANCE,
                String.format("Failed to load base database (%s). Are you sure it's " +
                              "valid?",
                    baseFile.getName()),
                "An error occurred",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        File updateFile = FileChooser.openFile("blurayguids.map", "map", false);
        if (updateFile == null) return;

        FileDB update = null;
        try { update = new FileDB(updateFile); }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(
                Toolkit.INSTANCE,
                String.format("Failed to load patch database (%s). Are you sure it's " +
                              "valid?",
                    updateFile.getName()),
                "An error occurred",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        StringBuilder builder = new StringBuilder(update.getEntryCount() * 255);
        for (FileDBRow row : update)
        {
            FileDBRow existing = base.get(row.getGUID());
            if (existing == null)
                builder.append(String.format("[+] path=%s size=%s sha1=%s guid=%s\n",
                    row.getPath(), row.getSize(), row.getSHA1(), row.getGUID()));
            else
            {

                if (row.getPath().equals(existing.getPath()))
                {
                    if (row.getSHA1().equals(existing.getSHA1())) continue;
                }


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

    public static void installMod()
    {
        File[] files = FileChooser.openFiles("mod");
        if (files == null || files.length == 0) return;

        FileData database = ResourceSystem.getSelectedDatabase();
        Fart[] archives = null;
        if (database.getType().equals(DatabaseType.FILE_DATABASE))
        {
            archives = Toolkit.INSTANCE.getSelectedArchives();
            if (archives == null) return;
        }

        for (int i = 0; i < files.length; ++i)
        {
            Mod mod = ModCallbacks.loadMod(files[i]);
            if (mod == null) continue;

            if (database.getType().containsData())
            {
                for (FileDBRow row : mod)
                {
                    byte[] data = mod.extract(row.getSHA1());
                    if (data != null) database.add(data);
                }
                continue;
            }

            FileDB gameDB = (FileDB) database;
            for (FileDBRow row : mod)
            {
                if (gameDB.exists(row.getGUID()))
                    gameDB.get(row.getGUID()).setDetails(row);
                else
                    gameDB.newFileDBRow(row);

                byte[] data = mod.extract(row.getSHA1());
                if (data == null) continue;

                ResourceSystem.add(data, archives);
            }
        }

        database.setHasChanges();
        ResourceSystem.reloadModel(database);
    }
}
