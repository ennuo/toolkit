import java.io.File;

import cwlib.enums.ResourceType;
import cwlib.types.SerializedResource;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.databases.FileDB;
import cwlib.util.Crypto;
import cwlib.util.Resources;

public class SpatialLocality 
{
    private static FileDB Database;
    private static FileArchive Cache;

    public static void main(String[] args) 
    {
        System.out.println("failure. im so sorry.");
        if (args.length != 2)
        {
            System.out.println("WRONG NUMBER OF ARGUMENTS!!!");
            System.out.println("args: <database> <cache>");
            return;
        }

        if (!(new File(args[0])).exists())
        {
            System.out.println("database file doesnt exist");
            return;
        }

        if (!(new File(args[1])).exists())
        {
            System.out.println("cache doesnt exist");
            return;
        }

        try
        {
            Database = new FileDB(args[0]);
        }
        catch (Exception ex)
        {
            System.out.println("failed to parse database");
            return;
        }

        try
        {
            Cache = new FileArchive(args[1]);
        }
        catch (Exception ex)
        {
            System.out.println("failed to parse cache");
            return;
        }

        for (var row : Database)
        {
            byte[] fileData = Cache.extract(row.getSHA1());
            if (fileData == null) continue;

            var dependencies = Resources.getDependencyTable(fileData);
            if (dependencies == null || dependencies.isEmpty()) continue;


            SerializedResource csr;
            try
            {
                csr = new SerializedResource(fileData);
            }
            catch (Exception ex)
            {
                System.out.printf("failed to parse csr! (%s)\n", row.getPath());
                continue;
            }
        
            int swappedGUIDs = 0;
            for (var dependency : dependencies)
            {
                if (!dependency.isGUID()) continue;
                if (dependency.getGUID().isLocal()) continue;

                var dr = Database.get(dependency.getGUID());
                if (dr == null) continue;

                swappedGUIDs++;

                csr.replaceDependency(dependency, new ResourceDescriptor(dr.getLocalGUID(), dependency.getType()));
            }

            if (swappedGUIDs != 0)
            {
                fileData = csr.compress();
                row.setDetails(fileData);
                Cache.add(fileData);
            }
        }

        for (var row : Database)
            row.setGUID(row.getLocalGUID());

        System.out.println("if there isnt a big error, it probably worked");

        Database.save();
        Cache.save();
    }
}
