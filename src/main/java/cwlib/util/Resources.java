package cwlib.util;

import java.util.ArrayList;
import java.util.HashSet;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.singleton.ResourceSystem;
import cwlib.types.data.GatherData;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileEntry;

/**
 * Utilities for operations on resources.
 */
public class Resources {
    /**
     * Gets the dependency table of a resource without any of the overhead
     * of the Resource class
     * @param resource Resource data
     * @return Dependency entries
     */
    public static HashSet<ResourceDescriptor> getDependencyTable(byte[] resource) {
        HashSet<ResourceDescriptor> dependencies = new HashSet<>();
        MemoryInputStream stream = new MemoryInputStream(resource);

        ResourceType type = ResourceType.fromMagic(stream.str(3));
        if (type == ResourceType.INVALID)
            throw new IllegalArgumentException("Data provided is not a serialized resource!");

        SerializationType method = SerializationType.fromValue(stream.str(1));
        // No resource tables stored in other types
        if (method != SerializationType.BINARY && method != SerializationType.ENCRYPTED_BINARY)
            return dependencies;

        // Resources below this revision don't have a dependency table,
        // although no resources exist in current builds of LittleBigPlanet
        // that have any resources with this revision anyway as support
        // was removed for resources below 0x132
        if ((stream.i32() & 0xFFFF) < 0x109) return dependencies;

        stream.seek(stream.i32(), SeekMode.Begin); // Seek to dependency table

        int count = stream.i32();
        for (int i = 0; i < count; ++i) {
            switch (stream.i8()) {
                case 1: {
                    dependencies.add(
                        new ResourceDescriptor(stream.sha1(), stream.enum32(ResourceType.class))
                    );
                    break;
                }
                case 2: {
                    dependencies.add(
                        new ResourceDescriptor(stream.guid(), stream.enum32(ResourceType.class))
                    );
                    break;
                }
                default: throw new SerializationException("Unexpected resource switch!");

            }
        }

        return dependencies;
    }

    /**
     * Does a deep search of all resources that a resource depends on, as well as gathering data.
     * @param root Resource to gather all dependencies of
     * @param descriptor Descriptor of root resource
     * @return All gathered dependency data
     */
    public static GatherData[] collect(byte[] root, ResourceDescriptor descriptor) {
        ArrayList<GatherData> entries = new ArrayList<>();
        Resources.collect(entries, root, descriptor);
        return entries.toArray(GatherData[]::new);
    }

    /**
     * Main recursive function handling the gathering of all dependency data.
     * @param entries Entry collection to add data to
     * @param root Resource to gather all dependencies of
     * @param descriptor Descriptor of root resource
     */
    private static void collect(ArrayList<GatherData> entries, byte[] root, ResourceDescriptor descriptor) {
        if (root == null) return;
        HashSet<ResourceDescriptor> dependencies = Resources.getDependencyTable(root);
        for (ResourceDescriptor dependency : dependencies) {
            // Scripts shouldn't really have dependencies, and when they do, they usually
            // refer to themselves, which will get this stuck in a loop.
            if (dependency == null || dependency.getType() == ResourceType.SCRIPT) continue;
            Resources.collect(
                entries, 
                ResourceSystem.extract(dependency), 
                dependency
            );
        }

        FileEntry entry = ResourceSystem.get(descriptor.getGUID());
        String path;

        SHA1 sha1 = descriptor.getSHA1();
        if (sha1 == null) sha1 = SHA1.fromBuffer(root);

        if (entry != null) path = entry.getPath();
        else 
            path = String.format("bundles/resources/%s/%s%s", descriptor.getType().toString().toLowerCase(), sha1, descriptor.getType().getExtension());

        entries.add(new GatherData(path, descriptor.getGUID(), sha1, root));
    }
}
