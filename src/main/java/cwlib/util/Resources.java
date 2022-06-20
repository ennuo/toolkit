package cwlib.util;

import cwlib.enums.ResourceType;
import cwlib.enums.SerializationType;
import cwlib.types.Resource;
import cwlib.types.data.ResourceDescriptor;
import cwlib.types.databases.FileEntry;
import cwlib.types.mods.Mod;
import toolkit.utilities.ResourceSystem;

/**
 * Utilities for operations on resources.
 */
public class Resources {
    public static void recurse(Mod mod, Resource resource, FileEntry entry) {
        for (int i = 0; i < resource.dependencies.size(); ++i) {
            ResourceDescriptor res = resource.dependencies.get(i);
            if (res == null || res.type == ResourceType.SCRIPT) continue;
            byte[] data = ResourceSystem.extractFile(res);
            if (data == null) continue;
            Resource dependency = new Resource(data);
            if (dependency.method != SerializationType.BINARY)
                mod.add(entry.path, data, entry.GUID);
            else recurse(mod, new Resource(data), ResourceSystem.findEntry(res));
        }
        if (resource.method == SerializationType.BINARY)
            mod.add(entry.path, resource.compressToResource(), entry.GUID);
    }

    public static SHA1 hashinate(Mod mod, Resource resource, FileEntry entry) {
        return Bytes.hashinate(mod, resource, entry, null);
    }
    
    public static SHA1 hashinate(Mod mod, Resource resource, FileEntry entry, HashMap<Integer, MaterialEntry> registry) {
        if (resource.method == SerializationType.BINARY) {
            if (registry == null || (registry != null && resource.type != ResourceType.GFX_MATERIAL)) {
                for (int i = 0; i < resource.dependencies.size(); ++i) {
                    ResourceDescriptor res = resource.dependencies.get(i);
                    FileEntry dependencyEntry = ResourceSystem.findEntry(res);
                    if (res == null) continue;
                    if (res.type == ResourceType.SCRIPT) continue;
                    byte[] data = ResourceSystem.extractFile(res);
                    if (data == null) continue;
                    Resource dependency = new Resource(data);

                    if (dependency.method == SerializationType.BINARY)
                        resource.replaceDependency(res, new ResourceDescriptor(hashinate(mod, dependency, dependencyEntry), res.type));
                    else {
                        mod.add(dependencyEntry.path, data, dependencyEntry.GUID);
                        resource.replaceDependency(res, new ResourceDescriptor(SHA1.fromBuffer(data), res.type));
                    }
                }
            }
            if (resource.type == ResourceType.PLAN)
                RPlan.removePlanDescriptors(resource, entry.GUID);
            byte[] data = null;
            if (resource.type == ResourceType.GFX_MATERIAL && registry != null) {
                GfxMaterialInfo info = new GfxMaterialInfo(new RGfxMaterial(resource));
                data = info.build(mod, registry);
            } else data = resource.compressToResource();
            mod.add(entry.getPath(), data, entry.GUID);
            return SHA1.fromBuffer(data);
        }
        return new SHA1();
    }
}
