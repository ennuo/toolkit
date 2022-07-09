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
    public static void main(String[] args) {
        ResourceSystem.API_MODE = true;
        ResourceSystem.DISABLE_LOGS = true;

        {
            Resource resource = new Resource("C:/Users/Rueezus/Desktop/level.bin");
            resource.loadResource(RLevel.class);

            if (true) return;
        }


        FileArchive archive = new FileArchive("E:/PS3/dev_hdd0/game/LBP2EUDC3/USRDIR/data.farc");
        FileDB database = new FileDB("E:/PS3/dev_hdd0/game/LBP2EUDC3/USRDIR/output/blurayguids.map");
        
        
        Resource resource = new Resource(archive.extract(database.get(86643).getSHA1()));
        RPalette palette = resource.loadResource(RPalette.class);

        RLevel level = new RLevel();
        PWorld world = level.world.getPart(Part.WORLD);
        for (ResourceDescriptor descriptor : palette.planList) {
            FileEntry entry = database.get(descriptor.getGUID());

            System.out.println(entry.getPath());

            RPlan plan = archive.loadResource(entry.getSHA1(), RPlan.class);
            
            Thing[] things = plan.getThings();
            // if (entry.getPath().contains("sound_")) {
            //     PRenderMesh mesh = things[0].getPart(Part.RENDER_MESH);
            //     if (mesh.boneThings.length == 1) {
            //         mesh.boneThings = new Thing[] { mesh.boneThings[0], null, null };
            //     }
            // }
            // for (Thing thing : things) 
            //     world.things.add(thing);
            
        }

        byte[] compressed = Resource.compress(level.build(new Revision(0x3a0), CompressionFlags.USE_ALL_COMPRESSION), false);

        FileIO.write(compressed, "E:/PS3/dev_hdd0/game/LBP3DEBUG/USRDIR/gamedata/lbp3/unthemed/palettes/blank_level_large_lbp3.bin");


    }
}
