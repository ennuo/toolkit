package executables;


import java.io.File;
import java.nio.file.Path;

import cwlib.enums.CompressionFlags;
import cwlib.enums.ResourceType;
import cwlib.io.Compressable;
import cwlib.types.Resource;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;
import cwlib.util.GsonUtils;
import toolkit.utilities.ResourceSystem;

public class Jsoninator {
    public static class WrappedResource {
        public Revision revision;
        public ResourceType type;
        public Object resource;

        public WrappedResource(){};
        public WrappedResource(Resource resource) {
            this.revision = resource.getRevision();
            this.type = resource.getResourceType();
            this.resource = resource.loadResource(this.type.getCompressable());
        }
    }
    
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

            
            int version = wrapper.revision.getVersion();
            byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
            if (version >= 0x297 || (version == 0x272 && (wrapper.revision.getBranchID() == 0x4c44) && ((wrapper.revision.getBranchRevision() & 0xffff) > 1)))
                compressionFlags = CompressionFlags.USE_ALL_COMPRESSION;

            byte[] compressed = Resource.compress(((Compressable)wrapper.resource).build(wrapper.revision, compressionFlags));

            FileIO.write(compressed, output.getAbsolutePath());

            return;
        }

        System.out.println("[MODE] RESOURCE -> JSON");

        WrappedResource wrapper = new WrappedResource(new Resource(input.getAbsolutePath()));
        FileIO.write(GsonUtils.toJSON(wrapper).getBytes(), output.getAbsolutePath());
    }
}
