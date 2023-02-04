package cwlib.util;

import java.util.ArrayList;
import java.util.HashSet;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.ex.SerializationException;
import cwlib.io.streams.MemoryInputStream;
import cwlib.io.streams.MemoryInputStream.SeekMode;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.data.GUID;
import cwlib.types.data.GatherData;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.data.Revision;
import cwlib.types.data.SHA1;
import cwlib.types.databases.FileEntry;
import java.nio.charset.StandardCharsets;

/**
 * Utilities for operations on resources.
 */
public class Resources {
    /**
     * Gets the resource type of a resource without any of the overhead
     * of the Resource class
     * @param resource Resource data
     * @return Resource type
     */
    public static ResourceType getResourceType(byte[] resource) {
        if (resource == null || resource.length < 4) return ResourceType.INVALID;
        return ResourceType.fromMagic(new String(resource, 0, 3, StandardCharsets.US_ASCII));   
    }
    
    /**
     * Gets the revision of a resource without any of the overhead
     * of the Resource class
     * @param resource Resource data
     * @return Resource revision
     */
    public static Revision getRevision(byte[] resource) {
        if (resource == null) return null;
        MemoryInputStream stream = new MemoryInputStream(resource);
        ResourceType type = ResourceType.fromMagic(stream.str(3));
        if (type == ResourceType.INVALID) return null;
        SerializationType method = SerializationType.fromValue(stream.str(1));
        if (method != SerializationType.BINARY && method != SerializationType.ENCRYPTED_BINARY)
            return null;
        int head = stream.i32();
        if (head < 0x272) return new Revision(head);
        stream.i32(); // dependency table offset
        int branch = stream.i32();
        return new Revision(head, branch);
    }
    
    
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
            return dependencies;

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
            ResourceDescriptor descriptor = null;
            byte flags = stream.i8();
            
            GUID guid = null;
            SHA1 sha1 = null;

            if ((flags & 2) != 0)
                guid = stream.guid();
            if ((flags & 1) != 0)
                sha1 = stream.sha1();
            
            descriptor = new ResourceDescriptor(guid, sha1, ResourceType.fromType(stream.i32()));
            if (descriptor.isValid())
                dependencies.add(descriptor);
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

    /**
     * Main recursive function handling the gathering of all dependency data, 
     * as well as replacing all dependencies with hashes.
     * @param root Resource to hashinate
     * @param descriptor Descriptor of root resource
     * @return All gathered dependency data
     */
    public static GatherData[] hashinate(byte[] root, ResourceDescriptor descriptor) {
        ArrayList<GatherData> entries = new ArrayList<>();
        Resources.hashinate(entries, root, descriptor);
        return entries.toArray(GatherData[]::new);
    }

    /**
     * Main recursive function handling the gathering of all dependency data, 
     * as well as replacing all dependencies with hashes.
     * @param entries Entry collection to add data to
     * @param root Resource to hashinate
     * @param descriptor Descriptor of root resource
     * @return SHA1 of resource
     */
    private static SHA1 hashinate(ArrayList<GatherData> entries, byte[] root, ResourceDescriptor descriptor) {
        if (root == null) return SHA1.EMPTY;

        if (root.length > 4) {
            ResourceType type = ResourceType.fromMagic(new String(new byte[] { root[0], root[1], root[2] }));
            SerializationType method = SerializationType.fromValue(Character.toString((char) root[3]));
            if (type != ResourceType.INVALID && method == SerializationType.BINARY) {
                Resource resource = new Resource(root);
                for (ResourceDescriptor dependency : resource.getDependencies()) {
                    // Scripts shouldn't really have dependencies, and when they do, they usually
                    // refer to themselves, which will get this stuck in a loop.
                    if (dependency == null || dependency.getType() == ResourceType.SCRIPT) continue;
                    
                    SHA1 sha1 = Resources.hashinate(
                        entries, 
                        ResourceSystem.extract(dependency), 
                        dependency
                    );

                    resource.replaceDependency(
                        dependency,
                        new ResourceDescriptor(sha1, dependency.getType())
                    );
                }
                root = resource.compress(resource.getStream().getBuffer());
            }
        }

        FileEntry entry = ResourceSystem.get(descriptor.getGUID());
        SHA1 sha1 = SHA1.fromBuffer(root);

        String path;
        if (entry != null) path = entry.getPath();
        else 
            path = String.format("bundles/resources/%s/%s%s", descriptor.getType().toString().toLowerCase(), sha1, descriptor.getType().getExtension());

        entries.add(new GatherData(path, descriptor.getGUID(), sha1, root));

        return sha1;
    }
}
