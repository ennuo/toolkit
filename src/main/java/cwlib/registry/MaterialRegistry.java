package cwlib.registry;

import cwlib.resources.RGfxMaterial;
import cwlib.types.Resource;
import cwlib.enums.MaterialFlags;
import cwlib.util.FileIO;
import cwlib.registry.GfxMaterialInfo.GfxTextureInfo;
import java.util.HashMap;

public class MaterialRegistry {
    public static class MaterialEntry {
        public byte[] material;
        public HashMap<String, Integer> ports = new HashMap<>();
    }
    
    public static byte[] MISSING_NO = FileIO.getResourceFile("materials/missingno");
    
    public static HashMap<Integer, MaterialEntry> LBP1 = new HashMap<>();
    public static HashMap<Integer, MaterialEntry> LBP2 = new HashMap<>();
    public static HashMap<Integer, MaterialEntry> LBP3PS3 = new HashMap<>();
    public static HashMap<Integer, MaterialEntry> LBP3PS4 = new HashMap<>();
    
    static {
        MaterialRegistry.registerLBP1("/materials/lbp1/diffuse.gmat", "DIFFUSE");
        MaterialRegistry.registerLBP1("/materials/lbp1/diffuse_bump.gmat", "DIFFUSE", "BUMP");
        MaterialRegistry.registerLBP1("/materials/lbp1/diffuse_bump_specular.gmat", "DIFFUSE", "SPECULAR", "BUMP");
        
        MaterialRegistry.registerLBP3("/materials/lbp3/diffuse.gmat", "DIFFUSE");
        MaterialRegistry.registerLBP3("/materials/lbp3/diffuse_bump.gmat", "DIFFUSE", "BUMP");
        MaterialRegistry.registerLBP3("/materials/lbp3/diffuse_bump_specular.gmat", "DIFFUSE", "SPECULAR", "BUMP");
    }
    
    public static byte[] convert(GfxMaterialInfo material, HashMap<Integer, MaterialEntry> registry) {
        MaterialEntry entry = (registry.containsKey(material.flags)) ? registry.get(material.flags) : registry.get(MaterialFlags.HAS_DIFFUSE);
        Resource resource = new Resource(entry.material);
        RGfxMaterial gmat = new RGfxMaterial(resource);
        for (String map : material.textures.keySet()) {
            int port = entry.ports.getOrDefault(map, -1);
            if (port == -1) continue;
            GfxTextureInfo texInfo = material.textures.get(map);
            gmat.textures[port] = texInfo.texture;
        }
        return gmat.build(resource.revision, resource.compressionFlags);
    }
    
    public static void registerLBP1(String path, String... maps) { MaterialRegistry.register(MaterialRegistry.LBP1, path, maps); } 
    public static void registerLBP2(String path, String... maps) { MaterialRegistry.register(MaterialRegistry.LBP2, path, maps); } 
    public static void registerLBP3(String path, String... maps) { MaterialRegistry.register(MaterialRegistry.LBP3PS3, path, maps); } 
    public static void registerLBP3PS4(String path, String... maps) { MaterialRegistry.register(MaterialRegistry.LBP3PS4, path, maps); } 
    
    private static void register(HashMap<Integer, MaterialEntry> registry, String path, String... maps) {
        int flags = 0;
        MaterialEntry entry = new MaterialEntry();
        entry.material = FileIO.getResourceFile(path);
        for (int i = 0; i < maps.length; ++i) {
            switch (maps[i].toUpperCase()) {
                case "DIFFUSE": {
                    flags |= MaterialFlags.HAS_DIFFUSE;
                    entry.ports.put("DIFFUSE", i);
                    break;
                }
                case "BUMP": case "NORMAL": {
                    flags |= MaterialFlags.HAS_BUMP;
                    entry.ports.put("BUMP", i);
                    entry.ports.put("NORMAL", i);
                    break;
                }
                case "SPECULAR": {
                    flags |= MaterialFlags.HAS_SPECULAR;
                    entry.ports.put("SPECULAR", i);
                    break;
                }
                case "GLOW": {
                    flags |= MaterialFlags.HAS_GLOW;
                    entry.ports.put("GLOW", i);
                    break;
                }
                case "REFLECTION": {
                    flags |= MaterialFlags.HAS_REFLECTION;
                    entry.ports.put("REFLECTION", i);
                    break;
                }
            }
        }
        registry.put(flags, entry);
    }
    
    
}
