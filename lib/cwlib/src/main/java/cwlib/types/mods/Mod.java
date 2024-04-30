package cwlib.types.mods;

import cwlib.enums.*;
import cwlib.ex.SerializationException;
import cwlib.io.serializer.Serializer;
import cwlib.io.streams.MemoryInputStream;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.slot.Slot;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.GUID;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.types.mods.patches.ModPatch;
import cwlib.types.mods.patches.TranslationPatch;
import cwlib.types.mods.patches.TranslationPatch.TranslationData;
import cwlib.util.Crypto;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import cwlib.util.Images;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mod extends FileDB
{
    private static final String LEGACY_PASSWORD = "purchasecollege";

    private SaveArchive archive;

    private ModInfo config = new ModInfo();
    private ArrayList<ModPatch> patches = new ArrayList<>();

    private ImageIcon icon = null;

    public Mod()
    {
        this(null, 0x01480100);
    }

    private Mod(File file, int version)
    {
        super(file, DatabaseType.MOD, version);
        Revision revision = new Revision(
            Branch.MIZUKI.getHead(),
            Branch.MIZUKI.getID(),
            Branch.MIZUKI.getRevision()
        );
        this.archive = new SaveArchive(revision, 0x4);
    }

    public Mod(File file)
    {
        super(file, DatabaseType.MOD);
        this.process(file);
    }

    private void process(File file)
    {
        try (FileSystem fileSystem = FileSystems.newFileSystem(file.toPath(),
            (java.lang.ClassLoader) null))
        {
            Path configPath = fileSystem.getPath("config.json");
            if (!Files.exists(configPath))
                throw new SerializationException("Mod is missing config file!");


            String config = FileIO.readString(configPath);

            this.config = GsonUtils.fromJSON(config, ModInfo.class);

            Path iconPath = fileSystem.getPath("icon.png");
            if (Files.exists(iconPath))
            {
                try
                {
                    byte[] image = Files.readAllBytes(iconPath);
                    InputStream input = new ByteArrayInputStream(image);
                    BufferedImage bufferedImage = ImageIO.read(input);
                    if (bufferedImage != null)
                        this.icon = Images.getImageIcon(bufferedImage);
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else System.out.println("Mod has no icon image.");

            Path databasePath = fileSystem.getPath("data.map");
            if (!Files.exists(databasePath))
                throw new SerializationException("Mod has no contents! (data.map is " +
                                                 "missing)");

            Path archivePath = fileSystem.getPath("data.farc");
            this.archive = null;
            if (Files.exists(archivePath))
                this.archive = new SaveArchive(Files.readAllBytes(archivePath));

            super.process(new MemoryInputStream(Files.readAllBytes(databasePath)));

            for (FileDBRow entry : this.entries)
            {
                if (this.archive != null && archive.exists(entry.getSHA1())) continue;
                Path filePath = fileSystem.getPath(entry.getPath());
                if (Files.exists(filePath))
                {
                    byte[] fileData = Files.readAllBytes(filePath);
                    entry.setDetails(fileData);
                    this.archive.add(fileData);
                }
            }

            Path patchesPath = fileSystem.getPath("patches.json");
            if (Files.exists(patchesPath))
            {
                String patchJSON = FileIO.readString(patchesPath);
                ModPatch[] patches = GsonUtils.fromJSON(patchJSON, ModPatch[].class);
                this.patches = new ArrayList<>(Arrays.asList(patches));
            }
        }
        catch (IOException ex)
        {
            throw new SerializationException(ex.getMessage());
        }
    }

    public static Mod fromLegacyMod(File file)
    {
        ResourceSystem.println("Mod", "Processing legacy mod file");
        MemoryInputStream stream = new MemoryInputStream(file.getAbsolutePath(),
            CompressionFlags.USE_ALL_COMPRESSION);
        Mod mod = new Mod(file, 0x01480100);

        if (!stream.str(3).equals("MOD"))
            throw new SerializationException("Resource header is not of type MOD!");

        String type = stream.str(1);
        if (type.equals("e"))
        {
            ResourceSystem.println("Mod", "Mod is encrypted! Attempting to decrypt using " +
                                          "default " +
                                          "passphrase.");
            if (!stream.bool())
            {
                stream = new MemoryInputStream(Crypto.decrypt(Mod.LEGACY_PASSWORD,
                    stream.bytes(stream.getLength() - 5)),
                    CompressionFlags.USE_ALL_COMPRESSION);
                ResourceSystem.println("Mod", "Mod has been decrypted successfully!");
                if (!stream.str(4).equals("MODb"))
                    throw new SerializationException("Mod has invalid magic!");
            }
            else
                throw new SerializationException("Custom keyed mods are no longer " +
                                                 "supported.");
        }
        else if (!type.equals("b"))
            throw new SerializationException("Mod has invalid serialization type!");

        int revision = stream.u8();

        if (revision < Revisions.LM_TOOLKIT)
            throw new SerializationException("Mod files below revision LM_TOOLKIT (0x3) are" +
                                             " not " +
                                             "supported!");
        if (revision > Revisions.LM_MAX)
            throw new SerializationException(String.format("This mod file (v%s) isn't " +
                                                           "supported " +
                                                           "with your version of Craftworld" +
                                                           " Toolkit (v%s), are you out of " +
                                                           "date?",
                revision, Revisions.LM_MAX));
        else
            ResourceSystem.println("Mod", "Mod revision is v" + revision);

        int head = Revisions.LM_OLD_HEAD;
        if (revision >= Revisions.LM_TYPES)
            head = Revisions.LM_HEAD;
        Serializer serializer = new Serializer(stream, new Revision(head));
        ModInfo config = mod.config;
        stream.i8(); // Skip compatibility
        config.version = (stream.u8()) + "." + (stream.u8());
        config.ID = stream.str();

        config.author = (revision >= Revisions.LM_TYPES) ? stream.wstr() : stream.str();

        config.title = stream.wstr();
        config.description = stream.wstr();

        int entryCount = stream.i32();
        for (int i = 0; i < entryCount; ++i)
        {
            String path = stream.str();
            int size = stream.i32();
            GUID guid = stream.guid();
            long date = (revision >= Revisions.LM_SLOTS_TIMESTAMPS) ? stream.u32() : 0;

            // account for workbench's shitty guid duping
            if (mod.exists(guid)) guid = mod.getNextGUID();

            FileDBRow entry = mod.newFileDBRow(path, guid);
            entry.setDate(date);
            entry.setSize(size);
        }

        // Items are no longer used in the current mod format
        // but we still have to parse the data since its not fixed size
        int itemCount = (revision >= Revisions.LM_TYPES) ? stream.i32() : stream.u16();
        for (int i = 0; i < itemCount; ++i)
        {
            serializer.struct(null, InventoryItemDetails.class);
            stream.u32();
            stream.u32(); // location/category
            serializer.resource(null, ResourceType.PLAN, true);
            stream.wstr();
            stream.wstr(); // translatedLocation/Category
            if (revision >= Revisions.LM_MINMAX)
            {
                stream.i32();
                stream.i32(); // min/max revisions
            }
        }

        // Slots also aren't used anymore, still have to parse data
        if (revision >= Revisions.LM_SLOTS_TIMESTAMPS)
            serializer.array(null, Slot.class);

        int LAMS_TYPE = 0x0;
        TranslationPatch patch = new TranslationPatch("Translations");
        patch.getLanguages().add("*");
        ArrayList<TranslationData> keys = patch.getKeys();

        int patchCount = stream.i32();
        for (int i = 0; i < patchCount; ++i)
        {
            int patchType = stream.u8();
            if (patchType != LAMS_TYPE)
                throw new SerializationException("Only LAMS patches are supported on " +
                                                 "legacy mods!");
            String tag = stream.str();
            stream.i32(); // lams key id
            String value = stream.wstr();
            keys.add(new TranslationData(tag, value));
        }

        if (patchCount != 0)
            mod.patches.add(patch);

        for (FileDBRow row : mod)
        {
            SHA1 sha1 = mod.archive.add(stream.bytes((int) row.getSize()));
            row.setSHA1(sha1);
        }

        byte[] imageData = stream.bytearray();
        if (imageData.length != 0)
        {
            InputStream input = new ByteArrayInputStream(imageData);
            try
            {
                BufferedImage image = ImageIO.read(input);
                if (image != null)
                    mod.icon = Images.getImageIcon(image);
            }
            catch (IOException ex)
            {
                ResourceSystem.println("Mod", "Failed to parse image data");
            }
        }

        return mod;
    }

    public ModInfo getConfig()
    {
        return this.config;
    }

    public ArrayList<ModPatch> getPatches()
    {
        return this.patches;
    }

    public ImageIcon getIcon()
    {
        return this.icon;
    }

    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    @Override
    public void add(byte[] data)
    {
        this.archive.add(data);
    }

    public FileDBRow add(String path, byte[] data)
    {
        return this.add(path, data, null);
    }

    public FileDBRow add(String path, byte[] data, GUID guid)
    {
        if (guid == null) guid = this.getNextGUID();
        FileDBRow row = this.newFileDBRow(path, guid);
        if (data != null)
        {
            row.setSHA1(this.archive.add(data));
            row.setSize(data.length);
        }
        return row;
    }

    @Override
    public byte[] extract(SHA1 sha1)
    {
        return this.archive.extract(sha1);
    }

    @Override
    public boolean save(File file)
    {
        if (file == null) return false;

        byte[] serializedDatabase = this.build();
        byte[] serializedArchive = this.archive.build(false);

        byte[] image = null;
        if (this.icon != null)
        {
            BufferedImage output = new BufferedImage(
                this.icon.getIconWidth(),
                this.icon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR
            );

            Graphics g = output.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                ImageIO.write(output, "png", baos);
                baos.flush();
                image = baos.toByteArray();
            }
            catch (IOException ex)
            {
                System.err.println("Failed to write icon.");
            }
        }

        byte[] config = GsonUtils.toJSON(this.config).getBytes(StandardCharsets.UTF_8);

        byte[] patches = null;
        if (this.patches.size() != 0)
            patches =
                GsonUtils.toJSON(this.patches.toArray(ModPatch[]::new)).getBytes(StandardCharsets.UTF_8);

        File workingZip = new File(ResourceSystem.getWorkingDirectory(), "working.mod");

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        URI uri =
            URI.create("jar:file:" + Paths.get(workingZip.getAbsolutePath()).toUri().getPath());
        try (FileSystem filesystem = FileSystems.newFileSystem(uri, env, null))
        {
            Files.write(filesystem.getPath("config.json"), config);
            if (image != null)
                Files.write(filesystem.getPath("icon.png"), image);
            if (patches != null)
                Files.write(filesystem.getPath("patches.json"), patches);
            Files.write(filesystem.getPath("data.map"), serializedDatabase);
            Files.write(filesystem.getPath("data.farc"), serializedArchive);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Mod.class.getName()).log(Level.SEVERE, null, ex);
        }

        try
        {
            Files.deleteIfExists(file.toPath());
            Files.copy(workingZip.toPath(), file.toPath());
        }
        catch (IOException ex) { System.err.println("There was an error copying mod file."); }

        try { Files.deleteIfExists(workingZip.toPath()); }
        catch (IOException ex) { System.err.println("There was an error deleting temp file."); }

        if (file.equals(this.getFile()))
            this.hasChanges = false;

        return true;
    }
}
