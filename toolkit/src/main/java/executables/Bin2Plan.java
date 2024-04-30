package executables;

import cwlib.resources.RLevel;
import cwlib.resources.RPlan;
import cwlib.types.SerializedResource;
import cwlib.util.FileIO;

import java.io.File;
import java.util.HashMap;

public class Bin2Plan
{
    public static void main(String[] args)
    {
        if (args.length < 2 || args.length > 3)
        {
            System.out.println("java -jar bin2plan.java <level.bin> <folder> [children]");
            return;
        }

        boolean includeChildren = false;
        if (args.length == 3 && args[2].equalsIgnoreCase("CHILDREN"))
        {
            System.out.println("Including children in plan generation");
            includeChildren = true;
        }

        if (!new File(args[0]).exists())
        {
            System.err.println("File doesn't exist!");
            return;
        }

        File folder = new File(args[1]);
        if (!folder.exists()) folder.mkdirs();

        System.out.println(new File(args[0]).getName());

        String base = "generated";

        SerializedResource resource = null;
        RLevel level = null;
        try
        {
            resource = new SerializedResource(args[0]);
            level = resource.loadResource(RLevel.class);
        }
        catch (Exception ex)
        {
            System.out.println("There was an error processing this resource!");
            System.out.println(ex.getMessage());
        }

        HashMap<String, RPlan> plans = level.getPalettes(base, resource.getRevision(),
            resource.getCompressionFlags(), includeChildren);
        for (String string : plans.keySet())
        {
            RPlan plan = plans.get(string);
            FileIO.write(SerializedResource.compress(plan.build()),
                new File(folder, string).getAbsolutePath());
        }
    }
}
