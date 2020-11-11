package ennuo.craftworld.resources;

import ennuo.craftworld.memory.Resource;
import java.awt.image.BufferedImage;

public class RawTexture {
    public enum TextureType {
        DXT5("DXT5", 0x88),
        DXT3("DXT3", 0x87),
        DXT1("DXT1", 0x86),
        RGAB32("ARGB32", 0x85),
        UNKNOWN("UNKNOWN", -1);
        
        public final String label;
        public final int index;
        
        public static TextureType get(int index) {
            switch (index) {
                case 0x88: return TextureType.DXT5;
                case 0x87: return TextureType.DXT3;
                case 0x86: return TextureType.DXT1;
                case 0x85: return TextureType.RGAB32;
            }
            return TextureType.UNKNOWN;
        }
        
        private TextureType(String label, int index) {
            this.label = label;
            this.index = index;
        }
    }
    
    public TextureType type;
    public BufferedImage texture;
    public short width, height;
    
    public RawTexture (Resource resource) {
        if (resource == null) {
            System.out.println("No data has been provided to the RawTexture constructor.");
            return;
        }
        
        byte[] texture = getTextureData(resource);
    }
    
    private byte[] getTextureData (Resource resource) {
        switch (resource.type) {
            case GTF_TEXTURE:
            case GXT_SIMPLE_TEXTURE: {
                type = TextureType.get(resource.data[4] & 0xFF);
                resource.seek(0xC);
                break;
            }
            case GXT_EXTENDED_TEXTURE: {
                type = TextureType.get(resource.data[0x18] & 0xFF);
                resource.seek(0x20);
                break;
            }
        }
        
        System.out.println("TextureType: " + type.label);
        
        width = resource.int16(); height = resource.int16();
        
        System.out.println("Texture Width: " + width + "px");
        System.out.println("Texture Height: " + height + "px");
        
        return resource.decompress(false);
    }
}
