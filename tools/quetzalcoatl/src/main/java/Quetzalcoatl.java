import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cwlib.enums.CellGcmEnumForGtf;
import cwlib.enums.CompressionFlags;
import cwlib.enums.GameProgressionStatus;
import cwlib.enums.GameShader;
import cwlib.enums.InventoryItemFlags;
import cwlib.enums.InventoryObjectType;
import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.enums.ToolType;
import cwlib.io.Resource;
import cwlib.io.serializer.SerializationData;
import cwlib.resources.RGfxMaterial;
import cwlib.resources.RLocalProfile;
import cwlib.resources.RPlan;
import cwlib.resources.RTexture;
import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;
import cwlib.singleton.ResourceSystem.ResourceLogLevel;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.profile.InventoryItem;
import cwlib.structs.texture.CellGcmTexture;
import cwlib.structs.things.Thing;
import cwlib.types.SerializedResource;
import cwlib.types.archives.Fart;
import cwlib.types.archives.FileArchive;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import cwlib.util.Resources;
import cwlib.util.gfx.CgAssembler;
import cwlib.util.gfx.GfxAssembler;

public class Quetzalcoatl 
{
    private static final Revision GameRevision = new Revision(0x1e5);
    static final int DDS_HEADER_SIZE = 0x80;


    public static byte[] RebuildGfxTexture(SerializedResource resource) 
    {
        RTexture texture = null;
        try { texture = new RTexture(resource); } 
        catch (Exception ex) 
        {
            ex.printStackTrace(); 
            return null; 
        }

        if (texture.getInfo() == null)
            return null;
        
        CellGcmTexture gcm = texture.getInfo();

        byte[] textureFileData = texture.getDDSFileData();
        if (gcm.isCubemap())
        {
            byte[] data = Arrays.copyOfRange(textureFileData, DDS_HEADER_SIZE, textureFileData.length);
            textureFileData = null;

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
                    if (textureFileData == null) textureFileData = buf;
                    else
                        textureFileData = Bytes.combine(textureFileData, buf);

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

            textureFileData = Bytes.combine(texture.getDDSHeader(), textureFileData);
        }

        if (gcm.isBumpTexture())
            textureFileData = Bytes.combine(textureFileData, "BUMP".getBytes(StandardCharsets.US_ASCII));
        else if (gcm.isVolumeTexture())
            textureFileData = Bytes.combine(textureFileData, "VLME".getBytes(StandardCharsets.US_ASCII));

        return SerializedResource.compress(new SerializationData(textureFileData));
    }

    public static byte[] RebuildGfxMaterial(SerializedResource resource) 
    {
        RGfxMaterial gfx = null;
        try { gfx = resource.loadResource(RGfxMaterial.class); } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
            return null;
        }

        gfx.flags &= ~0x10000;
        gfx.shaders = new byte[3][];

        try { CgAssembler.compile(GfxAssembler.generateShaderSource(gfx, -1, false), gfx, GameShader.LBP_STUPID_BUILD); } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
            return null;
        }

        return SerializedResource.compress(gfx.build(GameRevision, CompressionFlags.USE_NO_COMPRESSION));
    }

    public static byte[] RebuildPlanFile(SerializedResource resource) 
    {
        RPlan plan = null;
        try { plan = resource.loadResource(RPlan.class); }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }

        Thing[] things = null;
        try { things = plan.getThings(); }
        catch (Exception ex) { return null; }

        plan.compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        plan.revision = GameRevision;
        plan.setThings(things);

        return SerializedResource.compress(plan.build());
    }

    public static void main(String[] args) 
    {
        ResourceSystem.LOG_LEVEL = ResourceLogLevel.NONE;

        if (args.length != 2)
        {
            System.out.println("java -jar quetzalcoatl.jar <database> <cache>");
            return;
        }

        File databaseFilePath = new File(args[0]);
        if (!databaseFilePath.exists())
        {
            System.out.println(String.format("%s doesn't exist!", databaseFilePath.getAbsolutePath()));
            return;
        }

        File cacheFilePath = new File(args[1]);
        if (!cacheFilePath.exists())
        {
            System.out.println(String.format("%s doesn't exist!", cacheFilePath.getAbsolutePath()));
            return;
        }

        FileArchive cache;
        try
        {
            cache = new FileArchive(cacheFilePath);
        }
        catch (Exception ex)
        {
            System.out.println("An error occurred while attempting to parse the readonly cache!");
            return;
        }

        FileDB database;
        try
        {
            database = new FileDB(databaseFilePath);
        }
        catch (Exception ex)
        {
            System.out.println("An error occurred while attempting to parse the file database!");
            return;
        }

        for (FileDBRow row : database)
        {
            if (row.getSHA1().equals(SHA1.EMPTY)) continue;

            if (!cache.exists(row.getSHA1()))
            {
                System.out.printf("Hey dickhead, %s doesn't exist in the cache\n", row.getPath());
                continue;
            }

            byte[] data = cache.extract(row.getSHA1());
            if (data.length < 4 || (data[3] != 98 && data[3] != 32)) 
                continue;

            SerializedResource resource = null;
            try { resource = new SerializedResource(data); } 
            catch (Exception ex) 
            { 
                System.out.printf("Hey dickhead, %s failed to parse\n", row.getPath());
                continue;
            }

            data = null;

            switch (resource.getResourceType())
            {
                case GFX_MATERIAL:
                {
                    data = RebuildGfxMaterial(resource);
                    break;
                }
                case PLAN:
                {
                    data = RebuildPlanFile(resource);
                    break;
                }
                case TEXTURE: continue;
                case GTF_TEXTURE:
                {
                    data = RebuildGfxTexture(resource);
                    break;
                }
                default:
                {
                    if (resource.getResourceType().getCompressable() == null) 
                    {
                        System.out.printf("No compressable found for resource: %s\n", row.getPath());
                        continue;
                    }

                    try 
                    {
                        Resource root = (Resource) resource.loadResource(resource.getResourceType().getCompressable());
                        data = SerializedResource.compress(root.build(GameRevision, CompressionFlags.USE_NO_COMPRESSION));
                        break;
                    } 
                    catch (Exception ex) 
                    {
                        System.out.println("Failed to convert: " + row.getPath());
                        ex.printStackTrace();
                        continue;
                    }
                }
            }

            if (data != null)
            {
                row.setDetails(data);
                cache.add(data);
            }
        }

        cache.save();
        database.save();
    }
}
