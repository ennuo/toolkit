package ennuo.toolkit.utilities.services;

import ennuo.craftworld.resources.Animation;
import ennuo.craftworld.resources.Resource;
import ennuo.craftworld.types.FileEntry;
import javax.swing.JTree;

public class AnimationService implements ResourceService  {
    public static final int[] HEADERS = { 0x414e4d62 };

    @Override
    public void process(JTree tree, FileEntry entry, byte[] data) {
        Animation animation = entry.getResource("animation");
        if (animation == null) {
            try { animation = new Animation(new Resource(data).handle); entry.setResource("animation", animation); }
            catch (Exception e) { System.err.println("There was an error processing RAnimation file."); }
        }
    }

    @Override
    public int[] getSupportedHeaders() { return HEADERS; }
}
