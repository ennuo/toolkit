package workspace;

import java.nio.charset.StandardCharsets;

import cwlib.enums.CompressionFlags;
import cwlib.enums.Part;
import cwlib.enums.ResourceType;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RPlan;
import cwlib.structs.inventory.InventoryItemDetails;
import cwlib.structs.inventory.UserCreatedDetails;
import cwlib.structs.things.Thing;
import cwlib.structs.things.components.EggLink;
import cwlib.structs.things.components.GlobalThingDescriptor;
import cwlib.structs.things.parts.PAnimation;
import cwlib.structs.things.parts.PBody;
import cwlib.structs.things.parts.PGameplayData;
import cwlib.structs.things.parts.PInstrument;
import cwlib.structs.things.parts.PPos;
import cwlib.structs.things.parts.PRenderMesh;
import cwlib.structs.things.parts.PShape;
import cwlib.structs.things.parts.PTrigger;
import cwlib.types.Resource;
import cwlib.types.archives.FileArchive;
import cwlib.types.data.GUID;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileDB;
import cwlib.util.Bytes;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import toolkit.utilities.ResourceSystem;

public class PaletteDemo {
    public static void main(String[] args) {
        ResourceSystem.API_MODE = true;

        Resource resource = new Resource("E:/work/sample/bubble.plan");
        RPlan plan = resource.loadResource(RPlan.class);
        Revision revision = new Revision(0x132);

        Thing[] things = plan.getThings();

        PGameplayData gd = things[0].getPart(Part.GAMEPLAY_DATA);
        gd.eggLink = new EggLink();
        gd.eggLink.item = new GlobalThingDescriptor();
        gd.eggLink.item.levelDesc = new ResourceDescriptor(11327, ResourceType.LEVEL);
        gd.eggLink.item.UID = 372;

        Serializer serializer = new Serializer(0xFFFF, revision, CompressionFlags.USE_NO_COMPRESSION);
        serializer.array(things, Thing.class, true);
        plan.revision = revision;
        plan.thingData = serializer.getBuffer();
        plan.compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        
        plan.inventoryData.userCreatedDetails = new UserCreatedDetails("Bubble", "Test for building bubble palette data");
        plan.inventoryData.icon = new ResourceDescriptor(new GUID(39476),  ResourceType.TEXTURE);

        plan.dependencyCache.clear();
        for (ResourceDescriptor descriptor : serializer.getDependencies())
            plan.dependencyCache.add(descriptor);
        
        byte[] output = Resource.compress(plan.build(plan.revision, plan.compressionFlags), false);

        byte[] json = GsonUtils.toJSON(things).getBytes(StandardCharsets.UTF_8);
        FileIO.write(json, "E:/work/sample/bubble.plan.json");


        FileIO.write(output, "E:/PS3/dev_hdd0/game/LBP3DEBUG/USRDIR/item/test.plan");
    }
}
