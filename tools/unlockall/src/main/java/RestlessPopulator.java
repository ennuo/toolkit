import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cwlib.enums.CompressionFlags;
import cwlib.enums.GameProgressionStatus;
import cwlib.enums.InventoryItemFlags;
import cwlib.enums.InventoryObjectType;
import cwlib.enums.ResourceType;
import cwlib.enums.ToolType;
import cwlib.resources.RLocalProfile;
import cwlib.resources.RPlan;
import cwlib.resources.RTranslationTable;
import cwlib.singleton.ResourceSystem;
import cwlib.singleton.ResourceSystem.ResourceLogLevel;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.profile.InventoryItem;
import cwlib.types.SerializedResource;
import cwlib.types.archives.Fart;
import cwlib.types.archives.FileArchive;
import cwlib.types.archives.SaveArchive;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;
import cwlib.util.FileIO;
import cwlib.util.Resources;

public class RestlessPopulator 
{
    private static final GUID E_AMERICAN_TRANS = new GUID(54800);
    private static final GUID E_AMERICAN_PATCH_TRANS = new GUID(69910);

    private static FileDB GameDB;
    private static List<Fart> FartManyRO = new ArrayList<>();
    private static File GameDirectory;

    private static byte[] ExtractFile(SHA1 sha1)
    {
        for (Fart fart : FartManyRO)
        {
            byte[] data = fart.extract(sha1);
            if (data != null)
                return data;
        }
        
        return null;
    }

    private static byte[] ExtractFile(FileDBRow row)
    {
        byte[] data = ExtractFile(row.getSHA1());
        if (data != null) return data;

        File looseFilePath = new File(GameDirectory, row.getPath());
        if (looseFilePath.exists())
            return FileIO.read(looseFilePath.getAbsolutePath());

        return null;
    }

    public static void main(String[] args) 
    {
        ResourceSystem.LOG_LEVEL = ResourceLogLevel.NONE;

        if (args.length != 2)
        {
            System.out.println("java -jar unlockall.jar <littlefart> <game usrdir>");
            return;
        }

        File saveCachePath = new File(args[0]);
        if (!saveCachePath.exists())
        {
            System.out.println(String.format("%s doesn't exist!", saveCachePath.getAbsolutePath()));
            return;
        }

        SaveArchive save;
        try
        {
            save = new SaveArchive(saveCachePath);
        }
        catch (Exception ex)
        {
            System.out.println("An error occurred while attempting to parse the save cache!");
            return;
        }

        if (save.getKey() == null || save.getKey().getRootType() != ResourceType.LOCAL_PROFILE)
        {
            System.out.println("Save cache doesn't contain a local profile!");
            return;
        }

        RLocalProfile local;
        try
        {
            local = save.loadResource(save.getKey().getRootHash(), RLocalProfile.class);
            if (local == null)
            {
                System.out.println("Local profile root resource is missing from save!");
                return;
            }
        }
        catch (Exception ex)
        {
            System.out.println("An error occurred while attempting to parse the RLocalProfile root resource!");
            return;
        }

        GameDirectory = new File(args[1]);
        if (!GameDirectory.exists() || !GameDirectory.isDirectory())
        {
            System.out.println("Gamedata directory doesn't exist!");
            return;
        }

        File databaseFilePath = new File(GameDirectory, "output/blurayguids.map");
        if (!databaseFilePath.exists())
        {
            System.out.println("Couldn't find FileDB at " + databaseFilePath.getAbsolutePath());
            return;
        }

        try 
        {
            System.out.println("Adding FileDB @ " + databaseFilePath.getAbsolutePath()); 
            GameDB = new FileDB(databaseFilePath); 
        }
        catch (Exception ex) 
        { 
            System.out.println("An error occurred attempting to process FileDB at " + databaseFilePath.getAbsolutePath()); 
            return;
        }

        // Make sure to include the patch database if the user has any patches installed
        databaseFilePath = new File(GameDirectory, "output/brg_patch.map");
        if (databaseFilePath.exists())
        {
            try
            {
                System.out.println("Adding FileDB @ " + databaseFilePath.getAbsolutePath());

                FileDB patchDatabase = new FileDB(databaseFilePath);
                GameDB.patch(patchDatabase);
            }
            catch (Exception ex)
            {
                System.out.println("An error occurred attempting to process patch FileDB at " + databaseFilePath.getAbsolutePath());
                return;
            }
        }

        // File caches can basically be anywhere depending on how the user has modded the game, so
        // let's just walk through the directories.
        try (Stream<Path> stream = Files.walk(GameDirectory.toPath()))
        {
            List<String> files = stream
                .map(String::valueOf)
                .sorted()
                .collect(Collectors.toList());

            for (String path : files)
            {
                File file = new File(path);
                if (file.isFile() && file.getName().toLowerCase().endsWith(".farc"))
                {
                    try
                    {
                        FileArchive cache = new FileArchive(file);
                        System.out.println("Adding cache @ " + file.getAbsolutePath());
                        FartManyRO.add(cache);
                    }
                    catch (Exception ex)
                    {
                        // Ignore the exception because LBP3 has dynamic streaming caches,
                        // and I don't feel like handling that case right now.
                        continue;
                    }
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println("An error occurred while scanning for file caches!");
            return;
        }

        // Defaulting to American translations, fairly sure the game resets it anyway?
        // No idea actually.
        RTranslationTable table;
        {
            FileDBRow row = GameDB.get(E_AMERICAN_TRANS);
            if (row == null)
            {
                System.out.println("Couldn't find translation entry in file database!");
                return;
            }

            byte[] resourceData = ExtractFile(row);
            if (resourceData == null)
            {
                System.out.println("Couldn't find data source for translations!");
                return;
            }

            try
            {
                table = new RTranslationTable(resourceData);
            }
            catch (Exception ex)
            {
                System.out.println("An error occurred loading translations!");
                return;
            }

            // Patch translations are optional
            row = GameDB.get(E_AMERICAN_PATCH_TRANS);
            if (row != null)
            {
                resourceData = ExtractFile(row);
                if (resourceData != null)
                {
                    try 
                    {
                        RTranslationTable patchTable = new RTranslationTable(resourceData);
                        table.patch(patchTable);
                    }
                    catch (Exception ex)
                    {
                        System.out.println("Failed to parse patch translation table!");
                        return;
                    }
                }
            }
        }

        // Unlock all progression flags, useful if we're using an empty save
        local.lbp2GameProgressionFlags = -1;
        local.lbp1GameProgressionStatus = GameProgressionStatus.GAME_PROGRESSION_COMPLETED;
        local.lbp1GameProgressionEventHappenedBits = -1;
        local.lbp1GameProgressionEventsExplainedBits  = -1;
        local.lbp1MainMenuButtonUnlocks = -1;

        // Now that our environment is actually setup, let's try adding all the items to our inventory.
        System.out.println("Adding items, this may take a while...");
        for (FileDBRow row : GameDB)
        {
            String path = row.getPath();

            if (!path.endsWith(".plan")) continue;

            // Try to avoid most of the emitter items
            if (path.contains("base_virtual") || path.contains("levels")) continue;

            // Ignore the few LBP1 GOTY items
            if ((row.getGUID().getValue() & 0x80000000L) != 0) continue;

            // If we already have the item, obviously just skip it
            if (local.hasItem(row.getGUID())) continue;

            byte[] planData = ExtractFile(row);
            if (planData == null) continue;

            // Ignore any user generated content-esque items
            HashSet<ResourceDescriptor> descriptors = Resources.getDependencyTable(planData);
            boolean hasHashes = descriptors.stream().anyMatch(desc -> desc.isHash());
            if (hasHashes) continue;

            RPlan plan;
            try
            {
                SerializedResource resource = new SerializedResource(planData);
                if (resource.getResourceType() != ResourceType.PLAN) continue;
                plan = resource.loadResource(RPlan.class);
            }
            catch (Exception ex)
            {
                // Just ignore it, no reason to crash for this.
                continue;
            }

            // Don't bother adding plans with invalid data
            InventoryItemDetails details = plan.inventoryData;
            if (details == null) continue;
            if (details.icon == null) continue;

            // Don't want extra of the default items in your inventory
            if (details.type.isEmpty() || plan.inventoryData.toolType != ToolType.NONE) continue;
            if (details.type.contains(InventoryObjectType.PLAYER_COLOUR)) continue;
            
            // No community objects!
            if (details.type.contains(InventoryObjectType.USER_OBJECT)) continue;

            // It's probably safe to ignore any non-background without a category
            // (I'm fairly sure backgrounds don't have categories, but I'm too lazy to check)
            if (!details.type.contains(InventoryObjectType.BACKGROUND) && details.category == 0) continue;

            local.addItem(plan, new ResourceDescriptor(row.getGUID(), ResourceType.PLAN), table);
        }

        // Just as a little touch, unhide the dephysicalizer and sticker wash tool
        // if we're on LBP2.
        if (save.getGameRevision().isLBP2())
        {
            for (InventoryItem item : local.inventory)
            {
                // Shouldn't happen, but not going to risk it
                if (item.details == null) continue;

                // These tools require a patch to unhide in normal parts of the menu,
                // so we'll solve that by just hearting them.
                if (item.details.toolType == ToolType.UNPHYSICS || item.details.toolType == ToolType.STICKER_WASH)
                {
                    item.flags |= InventoryItemFlags.HEARTED;
                }
            }
        }

        System.out.println("Finished!");

        save.getKey().setRootHash(save.add(SerializedResource.compress(local.build(save.getGameRevision(), CompressionFlags.USE_ALL_COMPRESSION))));
        save.save();
    }
}
