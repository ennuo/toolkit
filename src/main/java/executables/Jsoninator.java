package executables;

import java.io.File;
import java.nio.file.Path;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.data.WrappedResource;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;

public class Jsoninator {
    
    
    public static void main(String[] args) {
        ResourceSystem.LOG_LEVEL = Integer.MAX_VALUE;
        if (args.length != 2) {
            System.out.println("java -jar jsoninator.java <input> <output>");
            return;
        }
        
        File input = new File(args[0]);
        File output = new File(args[1]);

        if (!input.exists()) {
            System.err.println("Input file doesn't exist!");
            return;
        }

        if (input.getAbsolutePath().toLowerCase().endsWith(".json")) {
            System.out.println("[MODE] JSON -> RESOURCE");

            WrappedResource wrapper = GsonUtils.fromJSON(
                FileIO.readString(Path.of(input.getAbsolutePath())),
                WrappedResource.class
            );

            FileIO.write(wrapper.build(), output.getAbsolutePath());

            return;
        }

        System.out.println("[MODE] RESOURCE -> JSON");

        Resource resource = new Resource(input.getAbsolutePath());
        GsonUtils.REVISION = resource.getRevision();
        WrappedResource wrapper = new WrappedResource(resource);
        FileIO.write(GsonUtils.toJSON(wrapper).getBytes(), output.getAbsolutePath());
    }
}
