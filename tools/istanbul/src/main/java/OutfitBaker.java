import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import cwlib.enums.CompressionFlags;
import cwlib.enums.Part;
import cwlib.io.exports.MeshExporter;
import cwlib.resources.RMesh;
import cwlib.resources.RPlan;
import cwlib.structs.mesh.Primitive;
import cwlib.structs.things.parts.PCostume;
import cwlib.types.SerializedResource;
import cwlib.types.archives.FartManyRO;
import cwlib.types.data.Revision;
import cwlib.types.databases.FileDB;
import cwlib.util.FileIO;
import cwlib.util.Strings;

public class OutfitBaker 
{
    public static void main(String[] args) 
    {
        FileDB database = null;
        var cache = new FartManyRO();
        String planFilePath = null;

        for (int i = 0; i < args.length;)
        {
            String arg = args[i++];
            switch (arg)
            {
                case "-cache":
                case "-c":
                {
                    var file = new File(args[i++]);
                    if (!file.exists())
                    {
                        System.out.printf("Trying to add a cache that doesn't exist! (@ %s)\n", file.getAbsolutePath());
                        return;
                    }

                    try { cache.incorporate(file); }
                    catch (Exception ex)
                    {
                        System.out.printf("Failed to incorporate cache @ %s\n", file.getAbsolutePath());
                        return;
                    }

                    break;
                }
                case "-database":
                case "-d":
                {
                    var file = new File(args[i++]);
                    if (!file.exists())
                    {
                        System.out.printf("Trying to add a database that doesn't exist! (@ %s)\n", file.getAbsolutePath());
                        return;
                    }

                    FileDB local;
                    try { local = new FileDB(file); }
                    catch (Exception ex)
                    {
                        System.out.printf("Failed to process database @ %s\n", file.getAbsolutePath());
                        return;
                    }

                    if (database == null) database = local;
                    else database.patch(local);

                    break;
                }
                default:
                {
                    if (planFilePath != null)
                    {
                        System.out.println("Can't have multiple input arguments!");
                        return;
                    }

                    planFilePath = arg;
                    
                    break;
                }

            }
        }

        var row = database.get(planFilePath);
        if (row == null)
        {
            System.out.printf("Couldn't find row in database with path %s!\n", planFilePath);
            return;
        }

        RPlan plan;
        try
        {
            plan = cache.loadResource(row.getSHA1(), RPlan.class);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to parse RPlan resource!");
            return;
        }

        if (plan == null)
        {
            System.out.printf("h%s (%s) does not exist in any cache!\n", row.getSHA1(), row.getPath());
            return;
        }

        String outputModelPath = Strings.getWithoutExtension(row.getName()) + ".mol";
        String outputObjectPath = Strings.getWithoutExtension(row.getName()) + ".obj";


        // var database = new FileDB("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/output/blurayguids.map");
        // database.patch(new FileDB("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/output/brg_patch.map"));
        // database.patch(new FileDB("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/gamedata/alear/sync/alrs.map"));
        // var cache = new FartManyRO();
        // cache.incorporate(new File("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/data.farc"));
        // cache.incorporate(new File("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/patch0.farc"));
        // cache.incorporate(new File("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/patch1.farc"));
        // cache.incorporate(new File("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/patch7.farc"));
        // cache.incorporate(new File("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/gamedata/alear/sync/alrs.farc"));

        // var database = new FileDB("E:/emu/rpcs3/dev_hdd0/game/LBP1DEBUG/USRDIR/output/blurayguids.map");
        // var cache = new FartManyRO();
        // cache.incorporate(new File("E:/emu/rpcs3/dev_hdd0/game/NPUA80662/USRDIR/data.farc"));
        // cache.incorporate(new File("E:/emu/rpcs3/dev_hdd0/game/NPUA80662/USRDIR/patches/cumulative_0133.farc"))


        var costume = plan.getThings()[0].<PCostume>getPart(Part.COSTUME);
        var hidden = new HashSet<>(costume.meshPartsHidden);

        var mesh = cache.loadResource(database.get(costume.mesh).getSHA1(), RMesh.class);

        // Make sure the asymmetry morph is always applied.
        mesh.applyMorphDestructive("asymmetry_morph");

        for (var piece : costume.costumePieces)
        {
            if (piece.mesh == null) continue;

            var costume_mesh = cache.loadResource(piece.mesh.isHash() ? piece.mesh.getSHA1() : database.get(piece.mesh).getSHA1(), RMesh.class);
            if (costume_mesh == null || costume_mesh.getNumVerts() == 0) continue;

            costume_mesh.applyMorphDestructive("asymmetry_morph");
            mesh.add(costume_mesh);
        }

        // remove any hidden morphs
        var primitives = mesh.getPrimitives().stream().filter(x -> !hidden.contains(x.region)).toList();


        // remap any sackboy weave base gmats
        if (costume.material != null)
        {
            for (var primitive : primitives)
            {
                if (primitive.material != null && primitive.material.isGUID() && primitive.material.getGUID().getValue() == 9698)
                    primitive.material = costume.material;
            }
        }

        mesh.setPrimitives(new ArrayList<Primitive>(primitives));

        mesh.removeAllClusterData();

        mesh.calculateBoundBoxes(false);

        MeshExporter.OBJ.export(outputObjectPath, mesh);

        byte[] fileData = SerializedResource.compress(mesh.build(new Revision(0x132), CompressionFlags.USE_NO_COMPRESSION));
        
        FileIO.write(fileData, outputModelPath);
    }
}
