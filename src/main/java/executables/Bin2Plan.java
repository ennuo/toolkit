package executables;

import java.io.File;
import java.util.HashMap;

import cwlib.resources.RLevel;
import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.util.FileIO;

public class Bin2Plan {
    public static void main(String[] args) {
        ResourceSystem.LOG_LEVEL = Integer.MAX_VALUE;

        if (args.length < 2 || args.length > 3) {
            System.out.println("java -jar bin2plan.java <level.bin> <folder> [children]");
            return;
        }

        boolean includeChildren = false;
        if (args.length == 3 && args[2].toUpperCase().equals("CHILDREN")) {
            System.out.println("Including children in plan generation");
            includeChildren = true;
        }

        if (!new File(args[0]).exists()) {
            System.err.println("File doesn't exist!");
            return;
        }

        File folder = new File(args[1]);
        if (!folder.exists()) folder.mkdirs();

        System.out.println(new File(args[0]).getName());

        String base = "generated";

        Resource resource = null;
        RLevel level = null;
        try {
            resource = new Resource(args[0]);
            level = resource.loadResource(RLevel.class);
        } catch (Exception ex) {
            System.out.println("There was an error processing this resource!");
            System.out.println(ex.getMessage());
        }

        HashMap<String, RPlan> plans = level.getPalettes(base, resource.getRevision(), resource.getCompressionFlags(), includeChildren);
        for (String string : plans.keySet()) {
            RPlan plan = plans.get(string);
            FileIO.write(Resource.compress(plan.build()), new File(folder, string).getAbsolutePath());
        }
    }
}
