package executables;

import cwlib.enums.Part;
import cwlib.resources.RLevel;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PWorld;
import cwlib.types.SerializedResource;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;

import java.io.File;
import java.util.ArrayList;

public class LevelMerger
{
    public static void main(String[] args)
    {
        if (args.length < 4)
        {
            System.out.println("java -jar levelmerger.java <...levels> -o <output.bin>");
            return;
        }

        File output = null;

        Revision revision = null;
        byte compressionFlags = (byte) 0;

        ArrayList<RLevel> levels = new ArrayList<>();
        for (int i = 0; i < args.length; ++i)
        {

            if (args[i].equals("-o"))
            {
                output = new File(args[++i]);
                continue;
            }

            File file = new File(args[i]);
            if (!file.exists())
            {
                System.err.printf("%s does not exist!%n", file.getAbsolutePath());
                return;
            }

            RLevel level = null;
            try
            {
                SerializedResource resource = new SerializedResource(args[i]);

                if (revision == null)
                {
                    revision = resource.getRevision();
                    compressionFlags = resource.getCompressionFlags();
                }

                level = new SerializedResource(args[i]).loadResource(RLevel.class);
            }
            catch (Exception ex)
            {
                System.err.println("There was an error processing this resource");
                System.err.println(ex.getMessage());
                return;
            }

            levels.add(level);
        }

        if (levels.size() == 0)
        {
            System.err.println("Requires at least two input files!");
            return;
        }

        if (output == null)
        {
            System.err.println("No output file was specified!");
            return;
        }

        RLevel level = levels.get(0);

        ArrayList<Thing> things = ((PWorld) level.worldThing.getPart(Part.WORLD)).things;
        int uid = ((PWorld) level.worldThing.getPart(Part.WORLD)).thingUIDCounter;

        for (int i = 1; i < levels.size(); ++i)
        {
            PWorld world = levels.get(i).worldThing.getPart(Part.WORLD);
            for (Thing thing : world.things)
            {
                if (thing == null) continue;

                if (thing.hasPart(Part.WORLD) || thing.hasPart(Part.LEVEL_SETTINGS))
                    continue;

                if (thing == world.backdrop) continue;
                if (thing.parent != null && thing.parent == world.backdrop) continue;

                thing.UID = ++uid;
                things.add(thing);
            }
        }

        byte[] data = null;
        try { data = SerializedResource.compress(level, revision, compressionFlags); }
        catch (Exception ex)
        {
            System.err.println("An error occurred compressing data!");
            return;
        }

        if (FileIO.write(data, output.getAbsolutePath()))
        {
            System.out.println("Wrote merged file to " + output.getAbsolutePath());
        }
        else System.err.println("An error occurred while writing file!");

    }
}
