import cwlib.enums.Part;
import cwlib.resources.RLevel;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.Decoration;
import cwlib.structs.things.components.decals.Decal;
import cwlib.structs.things.parts.*;
import cwlib.types.SerializedResource;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.GUID;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileDBRow;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.CRC32;

public class Descriptor
{
    private static final HashMap<Long, GUID> CRC_LOOKUP = new HashMap<>();

    public static void main(String[] args)
    {
        if (args.length < 2 || args.length > 3)
        {
            System.out.println("java -jar descriptor.jar <*.bin> <*.db> [null]");
            return;
        }

        boolean includeNull = args.length == 3 && args[2].equalsIgnoreCase("NULL");

        if (!new File(args[0]).exists())
        {
            System.err.println("Level file doesn't exist!");
            return;
        }

        if (!new File(args[1]).exists())
        {
            System.err.println("Database file doesn't exist!");
            return;
        }

        FileDB database = new FileDB(args[1]);
        System.out.println("[GatherTask] Performing CRC pre-process...");
        for (FileDBRow row : database)
        {
            CRC32 crc32 = new CRC32();
            crc32.update(row.getPath().getBytes());
            CRC_LOOKUP.put(crc32.getValue() | 0x80000000L, row.getGUID());
        }
        System.out.println("[GatherTask] Loading level...");

        RLevel level = null;
        try { level = new SerializedResource(args[0]).loadResource(RLevel.class); }
        catch (Exception ex)
        {
            System.out.println("[GatherTask] Failed to either load or parse, I'mma be real," +
                               " I " +
                               "ain't keeping track, let's see the stacktrace!");
            ex.printStackTrace();
            return;
        }

        if (level == null)
        {
            System.out.println("[GatherTask] Level was null somehow, and yet there wasn't " +
                               "an " +
                               "error...?");
            return;
        }

        HashSet<GUID> descriptors = new HashSet<>();
        PWorld world = level.worldThing.getPart(Part.WORLD);
        for (Thing thing : world.things)
        {
            if (thing == null) continue;
            if (thing.planGUID != null)
                descriptors.add(thing.planGUID);
            else if (includeNull)
                System.out.println("[GatherTask] Thing is missing PlanGUID! (ThingUID=" + thing.UID + ")");

            PDecorations decorations = thing.getPart(Part.DECORATIONS);
            if (decorations != null)
            {
                int index = 0;
                for (Decoration decoration : decorations.decorations)
                {
                    if (decoration.planGUID != null)
                        descriptors.add(decoration.planGUID);
                    else if (includeNull)
                        System.out.printf("[GatherTask] Decoration[%d]" +
                                          " missing " +
                                          "PlanGUID (ThingUID=%d)%n",
                            index, thing.UID);
                    index++;
                }
            }

            PStickers stickers = thing.getPart(Part.STICKERS);
            if (stickers != null)
            {
                int index = 0;
                for (Decal decal : stickers.decals)
                {
                    if (decal.plan != null)
                    {
                        if (decal.plan.isGUID())
                            descriptors.add(decal.plan.getGUID());
                    }
                    else if (includeNull)
                        System.out.printf("[GatherTask] Decal[%d] " +
                                          "missing PlanGUID" +
                                          " (ThingUID=%d)%n", index,
                            thing.UID);
                    index++;
                }
            }

            PGeneratedMesh generatedMesh = thing.getPart(Part.GENERATED_MESH);
            if (generatedMesh != null)
            {
                if (generatedMesh.planGUID != null)
                    descriptors.add(generatedMesh.planGUID);
                else if (includeNull)
                    System.out.println("[GatherTask] PGeneratedMesh missing PlanGUID " +
                                       "(ThingUID=" + thing.UID + ")");
            }


            PRef ref = thing.getPart(Part.REF);
            if (ref != null)
            {
                if (ref.plan == null && includeNull)
                    System.out.println("[GatherTask] PRef missing PlanGUID (ThingUID=" + thing.UID + ")");
                else if (ref.plan != null && ref.plan.isGUID())
                    descriptors.add(ref.plan.getGUID());
            }

            PGroup group = thing.getPart(Part.GROUP);
            if (group != null)
            {
                if (group.planDescriptor == null && includeNull)
                    System.out.println("[GatherTask] PGroup missing PlanGUID " +
                                       "(ThingUID=" + thing.UID + ")");
                else if (group.planDescriptor != null && group.planDescriptor.isGUID())
                    descriptors.add(group.planDescriptor.getGUID());
            }
        }

        ArrayList<GUID> missing = new ArrayList<>();
        for (GUID guid : descriptors)
        {
            if (!database.exists(guid))
            {
                if (!database.exists(CRC_LOOKUP.get(guid.getValue())))
                {
                    missing.add(guid);
                }
            }
        }

        if (missing.size() != 0)
        {
            System.out.printf("[%s] Found %d missing plan descriptors%n",
                "GatherTask", missing.size());
            System.out.println(missing);
        }
        else System.out.println("[GatherTask] No plan descriptors are missing!");
    }
}
