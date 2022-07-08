package workspace;

import java.nio.charset.StandardCharsets;

import cwlib.resources.RInstrument;
import cwlib.types.Resource;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;

public class KotoDemo {

    public static void extract(String name) {
        RInstrument koto = new Resource("C:/Users/Rueezus/Desktop/" + name + ".rinst").loadResource(RInstrument.class);
        byte[] data = GsonUtils.toJSON(koto).getBytes(StandardCharsets.UTF_8);
        FileIO.write(data, "C:/Users/Rueezus/Desktop/" + name + ".json");


        RInstrument resolved = GsonUtils.fromJSON(new String(data, StandardCharsets.UTF_8), RInstrument.class);
        data = GsonUtils.toJSON(resolved).getBytes(StandardCharsets.UTF_8);
        FileIO.write(data, "C:/Users/Rueezus/Desktop/" + name + ".resolved.json");
    }


    public static void main(String[] args) {
        extract("koto");
    }
}
