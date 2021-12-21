package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.Texture;
import ennuo.craftworld.types.FileEntry;
import ennuo.toolkit.windows.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JTree;

public class TextureService implements ResourceService  {
    public static final int[] HEADERS = { 0x54455820,  0x47544620, 0x47544673, 0x47544653, 0x89504e47, 0xFFD8FFE0, 0x44445320 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        Texture texture = entry.getResource("texture");
        if (texture == null) {
            try { texture = new Texture(data); entry.setResource("texture", texture); }
            catch (Exception e) { System.err.println("There was an error processing RTexture file."); }
        }
        
        if (texture == null) {
            System.out.println("Failed to create Texture instance, is this an appropriate resource?");
            return;
        }
        
        ImageIcon icon = texture.getImageIcon(320, 320);
        if (icon != null) Toolkit.instance.setImage(icon);
        else System.out.println("Failed to set icon, it's null?");
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
