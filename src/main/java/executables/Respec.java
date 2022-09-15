package executables;

import java.io.File;

import cwlib.enums.CompressionFlags;
import cwlib.enums.PartHistory;
import cwlib.io.Compressable;
import cwlib.io.serializer.Serializer;
import cwlib.resources.RPlan;
import cwlib.singleton.ResourceSystem;
import cwlib.structs.things.Thing;
import cwlib.types.Resource;
import cwlib.types.data.Revision;
import cwlib.util.FileIO;
import cwlib.util.Strings;

public class Respec {
    public static void main(String[] args) {
        ResourceSystem.LOG_LEVEL = Integer.MAX_VALUE;

        if (args.length < 3 || args.length > 4) {
            System.out.println("java -jar reroller.java <resource> <output> <revision> <descriptor?>");
            return;
        }

        int head = (int) Strings.getLong(args[2]);
        int branchDescriptor = 0;
        if (args.length == 4)
            branchDescriptor = (int) Strings.getLong(args[3]);
        
        if (head <= 0x3e2)
            Thing.MAX_PARTS_REVISION = PartHistory.CONTROLINATOR;
        if (head <= 0x33a)
            Thing.MAX_PARTS_REVISION = PartHistory.MATERIAL_OVERRIDE;
        if (head <= 0x2c3)
            Thing.MAX_PARTS_REVISION = PartHistory.MATERIAL_TWEAK;
        if (head <= 0x272)
            Thing.MAX_PARTS_REVISION = PartHistory.GROUP;

        byte compressionFlags = CompressionFlags.USE_NO_COMPRESSION;
        if (head >= 0x297 || (head == 0x272 && (branchDescriptor >> 0x10 == 0x4c44) && ((branchDescriptor & 0xffff) > 1)))
            compressionFlags = CompressionFlags.USE_ALL_COMPRESSION;
        
        Revision revision = new Revision(head, branchDescriptor);

        if (!new File(args[0]).exists()) {
            System.err.println("File doesn't exist!");
            return;
        }

        Compressable compressable = null;
        try {
            Resource resource = new Resource(args[0]);
            Serializer serializer = resource.getSerializer();
            Object struct = serializer.struct(null, resource.getResourceType().getCompressable());
            if (struct instanceof RPlan) {
                RPlan plan = (RPlan) struct;
                Thing[] things = null;
                try { things = plan.getThings(); } 
                catch (Exception ex) {
                    System.out.println("There was an error processing the thing data of this RPlan!");
                    System.out.println(ex.getMessage());
                    return;
                }
                plan.revision = revision;
                plan.compressionFlags = compressionFlags;
                plan.setThings(things);
            }
            compressable = (Compressable) struct;
        } catch (Exception ex) {
            System.out.println("There was an error processing this resource!");
            System.out.println(ex.getMessage());
        }

        FileIO.write(Resource.compress(compressable.build(revision, compressionFlags)), args[1]);
    }
}
