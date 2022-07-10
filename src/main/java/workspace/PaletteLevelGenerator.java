package workspace;

import java.nio.charset.StandardCharsets;

import cwlib.enums.CompressionFlags;
import cwlib.enums.Part;
import cwlib.resources.RLevel;
import cwlib.resources.RPalette;
import cwlib.resources.RPlan;
import cwlib.resources.RTranslationTable;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PScript;
import cwlib.structs.things.parts.PWorld;
import cwlib.types.Resource;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.databases.FileDB;
import cwlib.types.databases.FileEntry;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import toolkit.utilities.ResourceSystem;

public class PaletteLevelGenerator {

    public static RPlan[] loadPlans(String... paths) {
        RPlan[] plans = new RPlan[paths.length];
        for (int i = 0; i < paths.length; ++i) {
            String path = paths[i];
            plans[i] = new Resource(path).loadResource(RPlan.class);
        }
        return plans;
    }

    public static void main(String[] args) {
        if (true) {
            Resource resource = new Resource("C:/Users/Rueezus/Desktop/Unnamed Level.bin");
            resource.loadResource(RLevel.class);

            return;
        }

        FileArchive archive = new FileArchive("E:/PS3/dev_hdd0/game/LBP2BMOVE/USRDIR/patches/patch_0100.farc");
        FileDB database = new FileDB("E:/PS3/dev_hdd0/game/LBP2BMOVE/USRDIR/output/brg_patch.map");
        
        
        Resource resource = new Resource(archive.extract(database.get(175362).getSHA1()));
        RPalette palette = resource.loadResource(RPalette.class);

        RLevel level = new RLevel();
        PWorld world = level.world.getPart(Part.WORLD);
        for (ResourceDescriptor descriptor : palette.planList) {
            FileEntry entry = database.get(descriptor.getGUID());

            RPlan plan = archive.loadResource(entry.getSHA1(), RPlan.class);
            
            Thing[] things = plan.getThings();
            for (Thing thing : things) 
                world.things.add(thing);
            
        }

        RPlan[] plans = loadPlans(
            "C:/Users/Rueezus/Downloads/stick_move_loose_1.plan",
            "C:/Users/Rueezus/Downloads/stick_move_startled.plan",
            "C:/Users/Rueezus/Downloads/stick_move_dots.plan",
            "C:/Users/Rueezus/Downloads/stick_move_open01.plan",
            "C:/Users/Rueezus/Downloads/stick_move_loose.plan"
        );

        for (RPlan plan : plans) {
            Thing[] things = plan.getThings();
            for (Thing thing : things) 
                world.things.add(thing);
        }

        byte[] compressed = Resource.compress(level.build(new Revision(0x3db), CompressionFlags.USE_NO_COMPRESSION), false);

        FileIO.write(compressed, "E:/PS3/dev_hdd0/game/LBP3DEBUG/USRDIR/gamedata/lbp3/unthemed/palettes/blank_level_large_lbp3.bin");


    }
}
