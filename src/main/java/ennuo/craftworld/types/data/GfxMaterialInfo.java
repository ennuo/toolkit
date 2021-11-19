package ennuo.craftworld.types.data;

import ennuo.craftworld.resources.GfxMaterial;
import ennuo.craftworld.resources.enums.MaterialFlags;
import ennuo.craftworld.resources.structs.gfxmaterial.Box;
import ennuo.craftworld.resources.structs.gfxmaterial.Wire;
import java.util.HashMap;
import org.joml.Vector2f;

public class GfxMaterialInfo {
    public class GfxTextureInfo {
        public ResourceDescriptor texture;
        public int channel;
        public Vector2f offset;
        public Vector2f scale;
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
                
                switch (wire.portTo) {
                    case 0:
                        if ((this.flags & MaterialFlags.HAS_DIFFUSE) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_DIFFUSE;
                        this.textures.put("diffuse", info);
                        break;
                    case 2:
                        if ((this.flags & MaterialFlags.HAS_SPECULAR) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_SPECULAR;
                        this.textures.put("specular", info);
                        break;
                    case 3:
                        if ((this.flags & MaterialFlags.HAS_BUMP) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_BUMP;
                        this.textures.put("bump", info);
                        break;
                    case 4:
                        if ((this.flags & MaterialFlags.HAS_GLOW) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_GLOW;
                        this.textures.put("glow", info);
                        break;
                    case 6:
                        if ((this.flags & MaterialFlags.HAS_REFLECTION) != 0) 
                            continue;
                        this.flags |= MaterialFlags.HAS_REFLECTION;
                        this.textures.put("reflection", info);
                        break;
                }
                
            }
        }
        
    }
}
