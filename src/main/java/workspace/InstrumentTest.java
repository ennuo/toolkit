package workspace;

import java.nio.charset.StandardCharsets;

import cwlib.enums.CompressionFlags;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RPlan;
import cwlib.structs.inventory.UserCreatedDetails;
import cwlib.structs.things.Thing;
import cwlib.structs.things.parts.PInstrument;
import cwlib.types.Resource;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.databases.FileDB;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import toolkit.utilities.ResourceSystem;

public class InstrumentTest {
    public static void main(String[] args) {
        ResourceSystem.API_MODE = true;

        Resource resource = new Resource("E:/work/sample/instrument.plan");
        RPlan plan = resource.loadResource(RPlan.class);
        Revision revision = new Revision(0x272, 0x4c44, 0x0017);

        Thing[] things = plan.getThings();

        Serializer serializer = new Serializer(0xFFFF, revision, CompressionFlags.USE_NO_COMPRESSION);
        serializer.array(things, Thing.class, true);
        plan.revision = revision;
        plan.thingData = serializer.getBuffer();
        plan.compressionFlags = CompressionFlags.USE_NO_COMPRESSION;

        plan.inventoryData.userCreatedDetails = new UserCreatedDetails("Acoustic", "Test for rebuilding instrument data");
        plan.inventoryData.icon = new ResourceDescriptor(128483, ResourceType.TEXTURE);

        plan.dependencyCache.clear();
        for (ResourceDescriptor descriptor : serializer.getDependencies())
            plan.dependencyCache.add(descriptor);
        
        byte[] output = Resource.compress(plan.build(plan.revision, plan.compressionFlags), false);

        FileIO.write(output, "E:/work/sample/instrument.rebuild.plan");
        FileIO.write(GsonUtils.toJSON(things).getBytes(StandardCharsets.UTF_8), "E:/work/sample/instrument.plan.json");

        


        






    }
}
