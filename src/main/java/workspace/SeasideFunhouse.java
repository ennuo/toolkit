package workspace;

import java.nio.charset.StandardCharsets;

import cwlib.enums.CompressionFlags;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RLevel;
import cwlib.structs.things.Thing;
import cwlib.types.Resource;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;

public class SeasideFunhouse {
    public static void main(String[] args) {
        Resource resource = new Resource("C:/Users/Rueezus/Desktop/seaside.bin");
        RLevel level = resource.loadResource(RLevel.class);
        byte[] rebuild = Resource.compress(level.build(resource.getRevision(), CompressionFlags.USE_NO_COMPRESSION), false);
        FileIO.write(rebuild, "C:/Users/Rueezus/Desktop/test.bin");

        new Resource("C:/Users/Rueezus/Desktop/test.bin").loadResource(RLevel.class);

        

        byte[] json = GsonUtils.toJSON(level.world).getBytes(StandardCharsets.UTF_8);
        FileIO.write(json, "C:/Users/Rueezus/Desktop/test.json");

    }
}
