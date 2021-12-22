package ennuo.craftworld.types.data;

import ennuo.craftworld.registry.MaterialRegistry;
import ennuo.craftworld.registry.MaterialRegistry.MaterialEntry;
import ennuo.craftworld.resources.GfxMaterial;
import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.resources.enums.MaterialFlags;
import ennuo.craftworld.resources.enums.ResourceType;
import ennuo.craftworld.resources.structs.SHA1;
import ennuo.craftworld.resources.structs.gfxmaterial.Box;
import ennuo.craftworld.resources.structs.gfxmaterial.Wire;
import ennuo.craftworld.types.FileEntry;
import ennuo.craftworld.types.mods.Mod;
import ennuo.craftworld.utilities.Images;
import ennuo.toolkit.utilities.Globals;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import org.joml.Vector2f;

public class GfxMaterialInfo {
    public class GfxTextureInfo {
        public String path;
        public ResourceDescriptor texture;
        public int channel;
        public Vector2f offset;
        public Vector2f scale;
        
        public BufferedImage getBufferedImage() {
            byte[] data = Globals.extractFile(this.texture);
            if (data == null) return null;
            Texture texture = new Texture(data);
            if (scale.x > 1 || scale.y > 1) {
                if (scale.x < 1) scale.x = 1;
                if (scale.y < 1) scale.y = 1;
                BufferedImage scaled = Images.getTiledImage(
                        texture.cached, 
                        (int) Math.round(this.scale.x), 
                        (int) Math.round(this.scale.y)
                );
                return scaled;
            }
            return texture.cached;
        }
        
        public byte[] getTexture() { return this.getTexture(null); }
        public byte[] getTexture(BufferedImage dirt) {
            byte[] data = Globals.extractFile(this.texture);
            if (data == null) return null;
            Texture texture = new Texture(data);
            if (!texture.parsed || texture.cached == null) return null;
            BufferedImage scaled = texture.cached;
            if (scale.x > 1 || scale.y > 1) {
                if (scale.x < 1) scale.x = 1;
                if (scale.y < 1) scale.y = 1;
                scaled = Images.getTiledImage(
                        texture.cached, 
                        (int) Math.round(this.scale.x), 
                        (int) Math.round(this.scale.y)
                );
            }
            if (dirt != null)
                scaled = Images.multiply(dirt, scaled);
            return Images.toTEX(scaled);
        }
    }
    
    public int flags = 0;
    public HashMap<String, GfxTextureInfo> textures = new HashMap<>();
    
    public GfxMaterialInfo(GfxMaterial material) {
        int outputBox = material.getOutputBox();
        for (int i = 0; i < material.boxes.length; ++i) {
            Box box = material.boxes[i];
            if (box.type == Box.BoxType.TEXTURE_SAMPLE) {
                GfxTextureInfo info = new GfxTextureInfo();
                info.scale = new Vector2f(
                        Float.intBitsToFloat((int) box.params[0]),
                        Float.intBitsToFloat((int) box.params[1]) 
                );
                info.offset = new Vector2f(
                        Float.intBitsToFloat((int) box.params[2]),
                        Float.intBitsToFloat((int) box.params[3]) 
                );
                info.channel = (int) box.params[4];
                info.texture = material.textures[(int) box.params[5]];
                
                
                Wire wire = material.findWireFrom(i);
                while (wire.boxTo != outputBox)
                    wire = material.findWireFrom(wire.boxTo);
                
                FileEntry entry = Globals.findEntry(info.texture);
                if (entry != null) info.path = entry.path;
                switch (wire.portTo) {
                    case 0:
                        if ((this.flags & MaterialFlags.HAS_DIFFUSE) != 0) 
                            continue;
                        if (entry != null && entry.path.toLowerCase().contains("_dirt")) {
                            this.textures.put("DIRT", info);
                            continue;
                        }
                        this.flags |= MaterialFlags.HAS_DIFFUSE;
                        this.textures.put("DIFFUSE", info);
                        break;
                    case 2:
                        if ((this.flags & MaterialFlags.HAS_SPECULAR) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_SPECULAR;
                        this.textures.put("SPECULAR", info);
                        break;
                    case 3:
                        if ((this.flags & MaterialFlags.HAS_BUMP) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_BUMP;
                        this.textures.put("BUMP", info);
                        break;
                    case 4:
                        if ((this.flags & MaterialFlags.HAS_GLOW) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_GLOW;
                        this.textures.put("GLOW", info);
                        break;
                    case 6:
                        if ((this.flags & MaterialFlags.HAS_REFLECTION) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_REFLECTION;
                        this.textures.put("REFLECTION", info);
                        break;
                }
                
            }
        }
        
        if ((this.flags & MaterialFlags.HAS_DIFFUSE) == 0) {
            if (this.textures.containsKey("DIRT")) {
                this.textures.put("DIFFUSE", this.textures.get("DIRT"));
                this.textures.remove("DIRT");
                this.flags |= MaterialFlags.HAS_DIFFUSE;
            }
        }
        
    }
    
    public byte[] build(Mod mod, HashMap<Integer, MaterialEntry> registry) {
        
        // NOTE(Aidan): This probably can be cleaned up, but it's fine for now.
        
        if (this.textures.containsKey("DIFFUSE")) {
            byte[] diffuseData;
            BufferedImage dirtData = null;
            GfxTextureInfo diffuse = this.textures.get("DIFFUSE");
            GfxTextureInfo dirt = this.textures.get("DIRT");
            if (dirt != null)
                dirtData = dirt.getBufferedImage();
            if (dirtData != null)
                diffuseData = diffuse.getTexture(dirtData);
            else diffuseData = diffuse.getTexture();
            String path = diffuse.path;
            SHA1 sha1 = SHA1.fromBuffer(diffuseData);
            if (path == null) path = "textures/" + sha1.toString() + ".tex";
            else {
                path += "_scale_" + diffuse.scale.x + "_" + diffuse.scale.y;
                if (dirt != null)
                    path += "_dirt";
            }
            mod.add(path, diffuseData);
            diffuse.texture = new ResourceDescriptor(sha1, ResourceType.TEXTURE);
        }
        if (this.textures.containsKey("SPECULAR")) {
            GfxTextureInfo info = this.textures.get("SPECULAR");
            byte[] data = info.getTexture();
            String path = info.path;
            SHA1 sha1 = SHA1.fromBuffer(data);
            if (path == null) path = "textures/" + sha1.toString() + ".tex";
            else path += "_scale_" + info.scale.x + "_" + info.scale.y;
            mod.add(path, data);
            info.texture = new ResourceDescriptor(sha1, ResourceType.TEXTURE);
        }
        if (this.textures.containsKey("BUMP")) {
            GfxTextureInfo info = this.textures.get("BUMP");
            byte[] data = info.getTexture();
            String path = info.path;
            SHA1 sha1 = SHA1.fromBuffer(data);
            if (path == null) path = "textures/" + sha1.toString() + ".tex";
            else path += "_scale_" + info.scale.x + "_" + info.scale.y;
            mod.add(path, data);
            info.texture = new ResourceDescriptor(sha1, ResourceType.TEXTURE);
        }
        return MaterialRegistry.convert(this, registry);
    }
    
    
    
    
    
}
