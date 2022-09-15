package toolkit.functions;

import cwlib.io.serializer.SerializationData;
import cwlib.singleton.ResourceSystem;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.databases.FileEntry;
import cwlib.util.Bytes;

import java.util.ArrayList;
import java.util.Arrays;

public class DependencyCallbacks {
    public static void removeDependencies() {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        byte[] data = ResourceSystem.extract(entry);

        if (data == null) return;
        if (data[0x3] != 0x62) return;

        int offset = Bytes.toIntegerBE(Arrays.copyOfRange(data, 0x8, 0xC));

        byte[] output = new byte[offset + 4];
        System.arraycopy(data, 0, output, 0, offset);
        ResourceSystem.replace(entry, output);
    }

    public static void removeMissingDependencies() {
        FileEntry entry = ResourceSystem.getSelected().getEntry();
        byte[] data = ResourceSystem.extract(entry);

        if (data == null) return;

        Resource resource = new Resource(data);

        ResourceDescriptor[] dependencies = resource.getDependencies();
        ArrayList<ResourceDescriptor> cleaned = new ArrayList<>(dependencies.length);
        for (ResourceDescriptor descriptor : dependencies) {
            if (descriptor.isHash()) {
                cleaned.add(descriptor);
                continue;
            }

            if (ResourceSystem.get(descriptor.getGUID()) != null)
                cleaned.add(descriptor);
        }
        
        ResourceSystem.replace(entry, Resource.compress(
            new SerializationData(
                resource.getStream().getBuffer(),
                resource.getRevision(),
                resource.getCompressionFlags(),
                resource.getResourceType(),
                resource.getSerializationType(),
                cleaned.toArray(ResourceDescriptor[]::new)
            )
        ));
    }
}