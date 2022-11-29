package executables;

import java.io.File;
import java.util.ArrayList;

import cwlib.enums.Part;
import cwlib.resources.RLevel;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PWorld;
import cwlib.types.Resource;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;

public class LevelMerger {
    public static void main(String[] args) {
        ResourceSystem.DISABLE_LOGS = true;

        if (args.length < 4) {
            System.out.println("java -jar levelmerger.java <...levels> -o <output.bin>");
            return;
        }

        File output = null;

        Revision revision = null;
        byte compressionFlags = (byte) 0;

        ArrayList<RLevel> levels = new ArrayList<>();
        for (int i = 0; i < args.length; ++i) {

            if (args[i].equals("-o")) {
                output = new File(args[++i]);
                continue;
            }

            File file = new File(args[i]);
            if (!file.exists()) {
                System.err.println(String.format("%s does not exist!", file.getAbsolutePath()));
                return;
            }

            RLevel level = null;
            try { 
                Resource resource = new Resource(args[i]);

                if (revision == null) {
                    revision = resource.getRevision();
                    compressionFlags = resource.getCompressionFlags();
                }
                
                level = new Resource(args[i]).loadResource(RLevel.class); 
            }
            catch (Exception ex) {
                System.err.println("There was an error processing this resource");
                System.err.println(ex.getMessage());
                return;
            }

            levels.add(level);
        }

        if (levels.size() == 0) {
            System.err.println("Requires at least two input files!");
            return;
        }

        if (output == null) {
            System.err.println("No output file was specified!");
            return;
        }

        RLevel level = levels.get(0);

        ArrayList<Thing> things = ((PWorld)level.world.getPart(Part.WORLD)).things;
        int uid = ((PWorld)level.world.getPart(Part.WORLD)).thingUIDCounter;

        for (int i = 1; i < levels.size(); ++i) {
            PWorld world = ((PWorld)levels.get(i).world.getPart(Part.WORLD));
            for (Thing thing : world.things) {
                if (thing == null) continue;
                
                if (thing.hasPart(Part.WORLD) || thing.hasPart(Part.LEVEL_SETTINGS)) continue;

                if (thing == world.backdrop) continue;
                if (thing.parent != null && thing.parent == world.backdrop) continue;

                thing.UID = ++uid;
                things.add(thing);
            }
        }

        byte[] data = null;
        try { data = Resource.compress(level, revision, compressionFlags); } 
        catch (Exception ex) {
            System.err.println("An error occurred compressing data!");
            return;
        }

        if (FileIO.write(data, output.getAbsolutePath())) {
            System.out.println("Wrote merged file to " + output.getAbsolutePath());
        } else System.err.println("An error occurred while writing file!");

    }
}
